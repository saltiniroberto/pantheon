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
package tech.pegasys.pantheon.consensus.clique.blockcreation;

import tech.pegasys.pantheon.consensus.clique.VoteTallyCache;
import tech.pegasys.pantheon.consensus.common.ValidatorProvider;
import tech.pegasys.pantheon.ethereum.blockcreation.DefaultBlockScheduler;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.time.Clock;
import java.util.Random;

import com.google.common.annotations.VisibleForTesting;

public class CliqueBlockScheduler extends DefaultBlockScheduler {

  private final int OUT_OF_TURN_DELAY_MULTIPLIER_MILLIS = 500;

  private final VoteTallyCache voteTallyCache;
  private final Address localNodeAddress;

  public CliqueBlockScheduler(
      final Clock clock,
      final VoteTallyCache voteTallyCache,
      final Address localNodeAddress,
      final long secondsBetweenBlocks) {
    super(secondsBetweenBlocks, 0L, clock);
    this.voteTallyCache = voteTallyCache;
    this.localNodeAddress = localNodeAddress;
  }

  @Override
  @VisibleForTesting
  public BlockCreationTimeResult getNextTimestamp(final BlockHeader parentHeader) {
    final BlockCreationTimeResult result = super.getNextTimestamp(parentHeader);

    final long milliSecondsUntilNextBlock =
        result.getMillisecondsUntilValid() + calculateTurnBasedDelay(parentHeader);

    return new BlockCreationTimeResult(
        result.getTimestampForHeader(), Math.max(0, milliSecondsUntilNextBlock));
  }

  private int calculateTurnBasedDelay(final BlockHeader parentHeader) {
    final CliqueProposerSelector proposerSelector = new CliqueProposerSelector(voteTallyCache);
    final Address nextProposer = proposerSelector.selectProposerForNextBlock(parentHeader);

    if (nextProposer.equals(localNodeAddress)) {
      return 0;
    }
    return calculatorOutOfTurnDelay(voteTallyCache.getVoteTallyAtBlock(parentHeader));
  }

  private int calculatorOutOfTurnDelay(final ValidatorProvider validators) {
    final int countSigners = validators.getCurrentValidators().size();
    final int maxDelay = ((countSigners / 2) + 1) * OUT_OF_TURN_DELAY_MULTIPLIER_MILLIS;
    final Random r = new Random();
    return r.nextInt(maxDelay + 1);
  }
}
