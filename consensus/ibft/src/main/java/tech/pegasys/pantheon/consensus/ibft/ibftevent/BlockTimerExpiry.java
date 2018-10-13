package tech.pegasys.pantheon.consensus.ibft.ibftevent;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftEvent;
import tech.pegasys.pantheon.consensus.ibft.IbftEvents.Type;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/** Event indicating a block timer has expired */
public final class BlockTimerExpiry implements IbftEvent {
  private final ConsensusRoundIdentifier roundIdentifier;

  /**
   * Constructor for a BlockTimerExpiry event
   *
   * @param roundIdentifier The roundIdentifier that the expired timer belonged to
   */
  public BlockTimerExpiry(final ConsensusRoundIdentifier roundIdentifier) {
    this.roundIdentifier = roundIdentifier;
  }

  @Override
  public Type getType() {
    return Type.BLOCK_TIMER_EXPIRY;
  }

  public ConsensusRoundIdentifier getRoundIndentifier() {
    return roundIdentifier;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("Round Identifier", roundIdentifier).toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BlockTimerExpiry that = (BlockTimerExpiry) o;
    return Objects.equals(roundIdentifier, that.roundIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roundIdentifier);
  }
}
