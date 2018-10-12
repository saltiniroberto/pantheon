package tech.pegasys.pantheon.consensus.clique.blockcreation;

import tech.pegasys.pantheon.consensus.clique.VoteTallyCache;
import tech.pegasys.pantheon.consensus.common.ValidatorProvider;
import tech.pegasys.pantheon.ethereum.blockcreation.DefaultBlockScheduler;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.util.time.Clock;

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
    int countSigners = validators.getCurrentValidators().size();
    int maxDelay = ((countSigners / 2) + 1) * OUT_OF_TURN_DELAY_MULTIPLIER_MILLIS;
    Random r = new Random();
    return r.nextInt(maxDelay + 1);
  }
}
