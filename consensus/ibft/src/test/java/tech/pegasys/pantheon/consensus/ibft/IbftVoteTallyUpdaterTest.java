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
package tech.pegasys.pantheon.consensus.ibft;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.common.EpochManager;
import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class IbftVoteTallyUpdaterTest {

  private static final long EPOCH_LENGTH = 30_000;
  public static final Signature INVALID_SEAL =
      Signature.create(BigInteger.ONE, BigInteger.ONE, (byte) 0);
  private final VoteTally voteTally = mock(VoteTally.class);
  private final MutableBlockchain blockchain = mock(MutableBlockchain.class);
  private final KeyPair proposerKeyPair = KeyPair.generate();
  private final Address proposerAddress =
      Address.extract(Hash.hash(proposerKeyPair.getPublicKey().getEncodedBytes()));
  private final Address subject = Address.fromHexString("007f4a23ca00cd043d25c2888c1aa5688f81a344");
  private final Address validator1 =
      Address.fromHexString("00dae27b350bae20c5652124af5d8b5cba001ec1");

  private final IbftVoteTallyUpdater updater =
      new IbftVoteTallyUpdater(new EpochManager(EPOCH_LENGTH));

  @Test
  public void voteTallyUpdatedWithVoteFromBlock() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

    headerBuilder.number(1);
    addExtraData(headerBuilder, Optional.of(Vote.authVote(subject)));
    final BlockHeader header = headerBuilder.buildHeader();

    updater.updateForBlock(header, voteTally);

    verify(voteTally).addVote(proposerAddress, subject, IbftVoteType.ADD);
  }

  @Test
  public void voteTallyNotUpdatedWhenBlockHasNoVote() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(1);
    addExtraData(headerBuilder, Optional.empty());
    final BlockHeader header = headerBuilder.buildHeader();

    updater.updateForBlock(header, voteTally);

    verifyZeroInteractions(voteTally);
  }

  @Test
  public void voteTallyNotUpdatedWhenRecipientIsAddressZero() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(1);
    addExtraData(
        headerBuilder,
        Optional.of(
            Vote.authVote(Address.fromHexString("0000000000000000000000000000000000000000"))));
    final BlockHeader header = headerBuilder.buildHeader();

    updater.updateForBlock(header, voteTally);

    verifyZeroInteractions(voteTally);
  }

  @Test
  public void outstandingVotesDiscardedWhenEpochReached() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(EPOCH_LENGTH);
    addExtraData(
        headerBuilder,
        Optional.of(
            Vote.authVote(Address.fromHexString("0000000000000000000000000000000000000000"))));
    final BlockHeader header = headerBuilder.buildHeader();

    updater.updateForBlock(header, voteTally);

    verify(voteTally).discardOutstandingVotes();
    verifyNoMoreInteractions(voteTally);
  }

  @Test
  public void buildVoteTallyByExtractingValidatorsFromGenesisBlock() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(0);
    addExtraData(
        headerBuilder,
        Optional.of(
            Vote.authVote(Address.fromHexString("0000000000000000000000000000000000000000"))),
        asList(subject, validator1));
    final BlockHeader header = headerBuilder.buildHeader();

    when(blockchain.getChainHeadBlockNumber()).thenReturn(0L);
    when(blockchain.getBlockHeader(0)).thenReturn(Optional.of(header));

    final VoteTally voteTally = updater.buildVoteTallyFromBlockchain(blockchain);
    assertThat(voteTally.getCurrentValidators()).containsExactly(subject, validator1);
  }

  @Test
  public void buildVoteTallyByExtractingValidatorsFromEpochBlock() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(EPOCH_LENGTH);
    addExtraData(
        headerBuilder,
        Optional.of(
            Vote.authVote(Address.fromHexString("0000000000000000000000000000000000000000"))),
        asList(subject, validator1));
    final BlockHeader header = headerBuilder.buildHeader();

    when(blockchain.getChainHeadBlockNumber()).thenReturn(EPOCH_LENGTH);
    when(blockchain.getBlockHeader(EPOCH_LENGTH)).thenReturn(Optional.of(header));

    final VoteTally voteTally = updater.buildVoteTallyFromBlockchain(blockchain);
    assertThat(voteTally.getCurrentValidators()).containsExactly(subject, validator1);
  }

  @Test
  public void addVotesFromBlocksAfterMostRecentEpoch() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.number(EPOCH_LENGTH);
    addExtraData(
        headerBuilder,
        Optional.of(
            Vote.authVote(Address.fromHexString("0000000000000000000000000000000000000000"))),
        singletonList(validator1));
    final BlockHeader epochHeader = headerBuilder.buildHeader();

    headerBuilder.number(EPOCH_LENGTH + 1);
    addExtraData(headerBuilder, Optional.of(Vote.authVote(subject)), singletonList(validator1));
    final BlockHeader voteBlockHeader = headerBuilder.buildHeader();

    when(blockchain.getChainHeadBlockNumber()).thenReturn(EPOCH_LENGTH + 1);
    when(blockchain.getBlockHeader(EPOCH_LENGTH)).thenReturn(Optional.of(epochHeader));
    when(blockchain.getBlockHeader(EPOCH_LENGTH + 1)).thenReturn(Optional.of(voteBlockHeader));

    final VoteTally voteTally = updater.buildVoteTallyFromBlockchain(blockchain);
    assertThat(voteTally.getCurrentValidators()).containsExactly(subject, validator1);
  }

  private void addExtraData(final BlockHeaderTestFixture builder, final Optional<Vote> vote) {
    addExtraData(builder, vote, singletonList(proposerAddress));
  }

  private void addExtraData(
      final BlockHeaderTestFixture builder,
      final Optional<Vote> vote,
      final List<Address> validators) {

    final IbftExtraData ibftExtraData =
        IbftExtraDataFixture.createExtraData(
            builder.buildHeader(),
            BytesValue.wrap(new byte[IbftExtraData.EXTRA_VANITY_LENGTH]),
            vote,
            validators,
            singletonList(proposerKeyPair),
            0xDEADBEEF);

    builder.extraData(ibftExtraData.encode());
    builder.coinbase(proposerAddress);
  }
}
