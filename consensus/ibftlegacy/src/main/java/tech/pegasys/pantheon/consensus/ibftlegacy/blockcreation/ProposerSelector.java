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
package tech.pegasys.pantheon.consensus.ibftlegacy.blockcreation;

import static com.google.common.base.Preconditions.checkArgument;

import tech.pegasys.pantheon.consensus.common.ValidatorProvider;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftBlockHashing;
import tech.pegasys.pantheon.consensus.ibftlegacy.IbftExtraData;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for determining which member of the validator pool should propose the next block
 * (i.e. send the Preprepare message).
 *
 * <p>It does this by extracting the previous block's proposer from the ProposerSeal (stored in the
 * Blocks ExtraData) then iterating through the validator list (stored in {@link
 * ValidatorProvider}), such that each new round for the given height is serviced by a different
 * validator.
 */
public class ProposerSelector {

  private static final Logger LOG = LogManager.getLogger();

  private final Blockchain blockchain;

  /** Provides the current list of validators */
  private final ValidatorProvider validators;

  /**
   * If set, will cause the proposer to change on successful addition of a block. Otherwise, the
   * previously successful proposer will propose the next block as well.
   */
  private final Boolean changeEachBlock;

  public ProposerSelector(
      final Blockchain blockchain,
      final ValidatorProvider validators,
      final boolean changeEachBlock) {
    this.blockchain = blockchain;
    this.validators = validators;
    this.changeEachBlock = changeEachBlock;
  }

  /**
   * Determines which validator should be acting as the proposer for a given sequence/round.
   *
   * @param roundIdentifier Identifies the chain height and proposal attempt number.
   * @return The address of the node which is to propose a block for the provided Round.
   */
  public Address selectProposerForRound(final ConsensusRoundIdentifier roundIdentifier) {

    checkArgument(roundIdentifier.getRoundNumber() >= 0);
    checkArgument(roundIdentifier.getSequenceNumber() > 0);

    final long prevBlockNumber = roundIdentifier.getSequenceNumber() - 1;
    final Address prevBlockProposer = getProposerOfBlock(prevBlockNumber);

    if (!validators.getCurrentValidators().contains(prevBlockProposer)) {
      return handleMissingProposer(prevBlockProposer, roundIdentifier);
    } else {
      return handleWithExistingProposer(prevBlockProposer, roundIdentifier);
    }
  }

  /**
   * If the proposer of the previous block is missing, the validator with an Address above the
   * previous will become the next validator for the first round of the next block.
   *
   * <p>And validators will change from there.
   */
  private Address handleMissingProposer(
      final Address prevBlockProposer, final ConsensusRoundIdentifier roundIdentifier) {
    final NavigableSet<Address> validatorSet = new TreeSet<>(validators.getCurrentValidators());
    final SortedSet<Address> latterValidators = validatorSet.tailSet(prevBlockProposer, false);
    final Address nextProposer;
    if (latterValidators.isEmpty()) {
      // i.e. prevBlockProposer was at the end of the validator list, so the right validator for
      // the start of this round is the first.
      nextProposer = validatorSet.first();
    } else {
      // Else, use the first validator after the dropped entry.
      nextProposer = latterValidators.first();
    }
    return calculateRoundSpecificValidator(nextProposer, roundIdentifier.getRoundNumber());
  }

  /**
   * If the previous Proposer is still a validator - determine what offset should be applied for the
   * given round - factoring in a proposer change on the new block.
   *
   * @param prevBlockProposer
   * @param roundIdentifier
   * @return
   */
  private Address handleWithExistingProposer(
      final Address prevBlockProposer, final ConsensusRoundIdentifier roundIdentifier) {
    int indexOffsetFromPrevBlock = roundIdentifier.getRoundNumber();
    if (changeEachBlock) {
      indexOffsetFromPrevBlock += 1;
    }
    return calculateRoundSpecificValidator(prevBlockProposer, indexOffsetFromPrevBlock);
  }

  /**
   * Given Round 0 of the given height should start from given proposer (baseProposer) - determine
   * which validator should be used given the indexOffset.
   *
   * @param baseProposer
   * @param indexOffset
   * @return
   */
  private Address calculateRoundSpecificValidator(
      final Address baseProposer, final int indexOffset) {
    final List<Address> currentValidatorList = new ArrayList<>(validators.getCurrentValidators());
    final int prevProposerIndex = currentValidatorList.indexOf(baseProposer);
    final int roundValidatorIndex = (prevProposerIndex + indexOffset) % currentValidatorList.size();
    return currentValidatorList.get(roundValidatorIndex);
  }

  /**
   * Determines the proposer of an existing block, based on the proposer signature in the extra
   * data.
   *
   * @param blockNumber The index of the block in the chain being queried.
   * @return The unique identifier fo the node which proposed the block number in question.
   */
  private Address getProposerOfBlock(final long blockNumber) {
    final Optional<BlockHeader> maybeBlockHeader = blockchain.getBlockHeader(blockNumber);
    if (maybeBlockHeader.isPresent()) {
      final BlockHeader blockHeader = maybeBlockHeader.get();
      final IbftExtraData extraData = IbftExtraData.decode(blockHeader.getExtraData());
      return IbftBlockHashing.recoverProposerAddress(blockHeader, extraData);
    } else {
      LOG.trace("Unable to determine proposer for requested block");
      throw new RuntimeException("Unable to determine past proposer");
    }
  }
}
