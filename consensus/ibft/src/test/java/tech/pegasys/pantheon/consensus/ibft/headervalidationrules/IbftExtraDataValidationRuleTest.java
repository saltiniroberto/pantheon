/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.headervalidationrules;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.Vote;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import org.junit.Test;

public class IbftExtraDataValidationRuleTest {

  private BlockHeader createProposedBlockHeader(
      final KeyPair proposerKeyPair,
      final List<Address> validators,
      final List<KeyPair> committerKeyPairs,
      boolean useDifferentRoundNumbersForCommittedSeals) {
    final int BASE_ROUND_NUMBER = 5;
    final BlockHeaderTestFixture builder = new BlockHeaderTestFixture();
    builder.number(1); // must NOT be block 0, as that should not contain seals at all
    builder.coinbase(Util.publicKeyToAddress(proposerKeyPair.getPublicKey()));
    final BlockHeader headerForCommitters = builder.buildHeader();

    final IbftExtraData ibftExtraDataNoCommittedSeals =
        new IbftExtraData(
            BytesValue.wrap(new byte[IbftExtraData.EXTRA_VANITY_LENGTH]),
            emptyList(),
            Optional.of(Vote.authVote(Address.ECREC)),
            BASE_ROUND_NUMBER,
            validators);

    // if useDifferentRoundNumbersForCommittedSeals is true then each committed seal will be
    // calculated for an extraData field with a different round number
    List<Signature> commitSeals =
        IntStream.range(0, committerKeyPairs.size())
            .mapToObj(
                i -> {
                  final int round =
                      useDifferentRoundNumbersForCommittedSeals
                          ? ibftExtraDataNoCommittedSeals.getRound() + i
                          : ibftExtraDataNoCommittedSeals.getRound();

                  IbftExtraData extraDataForCommittedSealCalculation =
                      new IbftExtraData(
                          ibftExtraDataNoCommittedSeals.getVanityData(),
                          emptyList(),
                          ibftExtraDataNoCommittedSeals.getVote(),
                          round,
                          ibftExtraDataNoCommittedSeals.getValidators());

                  final Hash headerHashForCommitters =
                      IbftBlockHashing.calculateDataHashForCommittedSeal(
                          headerForCommitters, extraDataForCommittedSealCalculation);

                  return SECP256K1.sign(headerHashForCommitters, committerKeyPairs.get(i));
                })
            .collect(Collectors.toList());

    IbftExtraData extraDataWithCommitSeals =
        new IbftExtraData(
            ibftExtraDataNoCommittedSeals.getVanityData(),
            commitSeals,
            ibftExtraDataNoCommittedSeals.getVote(),
            ibftExtraDataNoCommittedSeals.getRound(),
            ibftExtraDataNoCommittedSeals.getValidators());

    builder.extraData(extraDataWithCommitSeals.encode());
    return builder.buildHeader();
  }

  @Test
  public void correctlyConstructedHeaderPassesValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress = Util.publicKeyToAddress(proposerKeyPair.getPublicKey());

    final List<Address> validators = singletonList(proposerAddress);
    final VoteTally voteTally = new VoteTally(validators);
    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));

    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair, validators, singletonList(proposerKeyPair), false);

    assertThat(extraDataValidationRule.validate(header, null, context)).isTrue();
  }

  @Test
  public void insufficientCommitSealsFailsValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators = singletonList(proposerAddress);
    final VoteTally voteTally = new VoteTally(validators);
    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));

    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    final BlockHeader header =
        createProposedBlockHeader(proposerKeyPair, validators, emptyList(), false);

    // Note that no committer seals are in the header's IBFT extra data.
    final IbftExtraData headerExtraData = IbftExtraData.decode(header.getExtraData());
    assertThat(headerExtraData.getSeals().size()).isEqualTo(0);

    assertThat(extraDataValidationRule.validate(header, null, context)).isFalse();
  }

  @Test
  public void outOfOrderValidatorListFailsValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators =
        Lists.newArrayList(
            AddressHelpers.calculateAddressWithRespectTo(proposerAddress, 1), proposerAddress);

    final VoteTally voteTally = new VoteTally(validators);
    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));

    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair, validators, singletonList(proposerKeyPair), false);

    assertThat(extraDataValidationRule.validate(header, null, context)).isFalse();
  }

  @Test
  public void proposerNotInValidatorListFailsValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators =
        Lists.newArrayList(
            AddressHelpers.calculateAddressWithRespectTo(proposerAddress, 1), proposerAddress);

    final VoteTally voteTally = new VoteTally(validators);
    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));

    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair, validators, singletonList(proposerKeyPair), false);

    assertThat(extraDataValidationRule.validate(header, null, context)).isFalse();
  }

  @Test
  public void mismatchingReportedValidatorsVsLocallyStoredListFailsValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators = Lists.newArrayList(proposerAddress);

    final VoteTally voteTally = new VoteTally(validators);
    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));

    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    // Add another validator to the list reported in the IbftExtraData (note, as the
    validators.add(AddressHelpers.calculateAddressWithRespectTo(proposerAddress, 1));
    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair, validators, singletonList(proposerKeyPair), false);

    assertThat(extraDataValidationRule.validate(header, null, context)).isFalse();
  }

  @Test
  public void committerNotInValidatorListFailsValidation() {
    final KeyPair proposerKeyPair = KeyPair.generate();
    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators = singletonList(proposerAddress);
    final VoteTally voteTally = new VoteTally(validators);
    // Insert an extraData block with committer seals.
    final KeyPair nonValidatorKeyPair = KeyPair.generate();

    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair, validators, singletonList(nonValidatorKeyPair), false);

    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));
    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    assertThat(extraDataValidationRule.validate(header, null, context)).isFalse();
  }

  @Test
  public void ratioOfCommittersToValidatorsAffectValidation() {
    assertThat(subExecution(4, 4, false)).isEqualTo(true);
    assertThat(subExecution(4, 3, false)).isEqualTo(true);
    assertThat(subExecution(4, 2, false)).isEqualTo(false);

    assertThat(subExecution(5, 4, false)).isEqualTo(true);
    assertThat(subExecution(5, 3, false)).isEqualTo(false);
    assertThat(subExecution(5, 2, false)).isEqualTo(false);

    assertThat(subExecution(6, 4, false)).isEqualTo(true);
    assertThat(subExecution(6, 3, false)).isEqualTo(false);
    assertThat(subExecution(6, 2, false)).isEqualTo(false);

    assertThat(subExecution(7, 5, false)).isEqualTo(true);
    assertThat(subExecution(7, 4, false)).isEqualTo(false);

    assertThat(subExecution(8, 6, false)).isEqualTo(true);
    assertThat(subExecution(8, 5, false)).isEqualTo(false);
    assertThat(subExecution(8, 4, false)).isEqualTo(false);

    assertThat(subExecution(9, 6, false)).isEqualTo(true);
    assertThat(subExecution(9, 5, false)).isEqualTo(false);
    assertThat(subExecution(9, 4, false)).isEqualTo(false);

    assertThat(subExecution(10, 7, false)).isEqualTo(true);
    assertThat(subExecution(10, 6, false)).isEqualTo(false);

    assertThat(subExecution(12, 8, false)).isEqualTo(true);
    assertThat(subExecution(12, 7, false)).isEqualTo(false);
    assertThat(subExecution(12, 6, false)).isEqualTo(false);
  }

  @Test
  public void validationFailsIfCommittedSealsAreForDifferentRounds() {
    assertThat(subExecution(2, 2, true)).isEqualTo(false);
    assertThat(subExecution(4, 4, true)).isEqualTo(false);
  }

  private boolean subExecution(
      final int validatorCount,
      final int committerCount,
      boolean useDifferentRoundNumbersForCommittedSeals) {
    final KeyPair proposerKeyPair = KeyPair.generate();

    final Address proposerAddress =
        Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));

    final List<Address> validators = Lists.newArrayList();
    final List<KeyPair> committerKeys = Lists.newArrayList();
    validators.add(proposerAddress);
    committerKeys.add(proposerKeyPair);
    for (int i = 0; i < validatorCount - 1; i++) { // need -1 to account for proposer
      final KeyPair committerKeyPair = KeyPair.generate();
      committerKeys.add(committerKeyPair);
      validators.add(Address.extract(Hash.hash(committerKeyPair.getPublicKey().getEncodedBytes())));
    }

    Collections.sort(validators);
    final VoteTally voteTally = new VoteTally(validators);
    BlockHeader header =
        createProposedBlockHeader(
            proposerKeyPair,
            validators,
            committerKeys.subList(0, committerCount),
            useDifferentRoundNumbersForCommittedSeals);

    final ProtocolContext<IbftContext> context =
        new ProtocolContext<>(null, null, new IbftContext(voteTally, null));
    final IbftExtraDataValidationRule extraDataValidationRule =
        new IbftExtraDataValidationRule(true);

    return extraDataValidationRule.validate(header, null, context);
  }
}
