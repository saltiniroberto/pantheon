package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.ethereum.core.BlockHashFunction;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;

/**
 * Looks up the correct {@link BlockHashFunction} to use based on a {@link ProtocolSchedule} to
 * ensure that the correct hash is created given the block number.
 */
public class ScheduleBasedBlockHashFunction<C> implements BlockHashFunction {

  private final ProtocolSchedule<C> protocolSchedule;

  private ScheduleBasedBlockHashFunction(final ProtocolSchedule<C> protocolSchedule) {
    this.protocolSchedule = protocolSchedule;
  }

  public static <C> BlockHashFunction create(final ProtocolSchedule<C> protocolSchedule) {
    return new ScheduleBasedBlockHashFunction<>(protocolSchedule);
  }

  @Override
  public Hash apply(final BlockHeader header) {
    return protocolSchedule
        .getByBlockNumber(header.getNumber())
        .getBlockHashFunction()
        .apply(header);
  }
}
