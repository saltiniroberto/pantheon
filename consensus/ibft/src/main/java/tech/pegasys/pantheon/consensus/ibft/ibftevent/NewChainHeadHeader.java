package tech.pegasys.pantheon.consensus.ibft.ibftevent;

import tech.pegasys.pantheon.consensus.ibft.IbftEvent;
import tech.pegasys.pantheon.consensus.ibft.IbftEvents.Type;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/** Event indicating that new chain head header has been received */
public final class NewChainHeadHeader implements IbftEvent {
  private final BlockHeader newChainHeadHeader;

  /**
   * Constructor for a RoundExpiry event
   *
   * @param newChainHeadHeader The header of the current blockchain head
   */
  public NewChainHeadHeader(final BlockHeader newChainHeadHeader) {
    this.newChainHeadHeader = newChainHeadHeader;
  }

  @Override
  public Type getType() {
    return Type.NEW_CHAIN_HEAD_HEADER;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("New Chain Head Header", newChainHeadHeader)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NewChainHeadHeader that = (NewChainHeadHeader) o;
    return Objects.equals(newChainHeadHeader, that.newChainHeadHeader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(newChainHeadHeader);
  }

  public BlockHeader getNewChainHeadHeader() {
    return newChainHeadHeader;
  }
}
