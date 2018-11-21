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
package tech.pegasys.pantheon.consensus.clique;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.consensus.common.VoteType.ADD;
import static tech.pegasys.pantheon.consensus.common.VoteType.DROP;

import tech.pegasys.pantheon.consensus.common.ValidatorVote;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.AddressHelpers;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class CliqueVotingBlockInterfaceTest {

  private static final KeyPair proposerKeys = KeyPair.generate();
  private static final Address proposerAddress =
      Util.publicKeyToAddress(proposerKeys.getPublicKey());
  private static final List<Address> validatorList = singletonList(proposerAddress);

  private final CliqueVotingBlockInterface blockInterface = new CliqueVotingBlockInterface();

  private final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

  private final BlockHeader header =
      TestHelpers.createCliqueSignedBlockHeader(headerBuilder, proposerKeys, validatorList);

  private final BlockHeaderBuilder builder =
      BlockHeaderBuilder.fromHeader(headerBuilder.buildHeader())
          .blockHashFunction(MainnetBlockHashFunction::createHash);

  @Test
  public void headerWithZeroCoinbaseReturnsAnEmptyVote() {
    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();
    headerBuilder.coinbase(AddressHelpers.ofValue(0));

    assertThat(blockInterface.extractVoteFromHeader(headerBuilder.buildHeader())).isEmpty();
  }

  @Test
  public void headerWithNonceOfZeroReportsDropVote() {
    headerBuilder.coinbase(AddressHelpers.ofValue(1)).nonce(0L);

    final Optional<ValidatorVote> extractedVote = blockInterface.extractVoteFromHeader(header);

    assertThat(extractedVote)
        .contains(new ValidatorVote(DROP, proposerAddress, header.getCoinbase()));
  }

  @Test
  public void headerWithNonceOfMaxLongReportsAddVote() {
    headerBuilder.coinbase(AddressHelpers.ofValue(2)).nonce(0xFFFFFFFFFFFFFFFFL);

    final BlockHeader header =
        TestHelpers.createCliqueSignedBlockHeader(headerBuilder, proposerKeys, validatorList);
    final Optional<ValidatorVote> extractedVote = blockInterface.extractVoteFromHeader(header);

    assertThat(extractedVote)
        .contains(new ValidatorVote(ADD, proposerAddress, header.getCoinbase()));
  }

  @Test
  public void blendingAddVoteToHeaderResultsInHeaderWithNonceOfMaxLong() {

    final ValidatorVote vote =
        new ValidatorVote(ADD, AddressHelpers.ofValue(1), AddressHelpers.ofValue(2));
    final BlockHeaderBuilder builderWithVote =
        CliqueVotingBlockInterface.insertVoteToHeaderBuilder(builder, Optional.of(vote));

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(vote.getRecipient());
    assertThat(header.getNonce()).isEqualTo(0xFFFFFFFFFFFFFFFFL);
  }

  @Test
  public void blendingDropVoteToHeaderResultsInHeaderWithNonceOfZero() {

    final ValidatorVote vote =
        new ValidatorVote(DROP, AddressHelpers.ofValue(1), AddressHelpers.ofValue(2));
    final BlockHeaderBuilder builderWithVote =
        CliqueVotingBlockInterface.insertVoteToHeaderBuilder(builder, Optional.of(vote));

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(vote.getRecipient());
    assertThat(header.getNonce()).isEqualTo(0x0L);
  }

  @Test
  public void nonVoteBlendedIntoHeaderResultsInACoinbaseOfZero() {
    final BlockHeaderBuilder builderWithVote =
        CliqueVotingBlockInterface.insertVoteToHeaderBuilder(builder, Optional.empty());

    final BlockHeader header = builderWithVote.buildBlockHeader();

    assertThat(header.getCoinbase()).isEqualTo(AddressHelpers.ofValue(0));
    assertThat(header.getNonce()).isEqualTo(0x0L);
  }

  @Test
  public void extractsValidatorsFromHeader() {
    final BlockHeader header =
        TestHelpers.createCliqueSignedBlockHeader(headerBuilder, proposerKeys, validatorList);
    final List<Address> extractedValidators = blockInterface.validatorsInBlock(header);

    assertThat(extractedValidators).isEqualTo(validatorList);
  }
}
