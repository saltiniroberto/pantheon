package net.consensys.pantheon.consensus.ibft.ibftevent;

import net.consensys.pantheon.consensus.ibft.IbftEvent;
import net.consensys.pantheon.consensus.ibft.IbftEvents.Type;
import net.consensys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;

/** Event indicating a round timer has expired */
public final class IbftMessageEvent implements IbftEvent {
  private final AbstractIbftMessageDecoded message;

  public IbftMessageEvent(final AbstractIbftMessageDecoded message) {
    this.message = message;
  }

  @Override
  public Type getType() {
    return Type.IBFT_MESSAGE;
  }

  public AbstractIbftMessageDecoded ibftMessage() {
    return message;
  }
}
