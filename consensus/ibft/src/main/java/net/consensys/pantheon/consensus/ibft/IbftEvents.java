package net.consensys.pantheon.consensus.ibft;

import net.consensys.pantheon.consensus.ibft.ibftevent.IbftMessageEvent;
import net.consensys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;

/** Static helper functions for producing and working with IbftEvent objects */
public class IbftEvents {

  public static IbftEvent fromMessage(final AbstractIbftMessageDecoded ibftMessageDecoded) {
    return new IbftMessageEvent(ibftMessageDecoded);
  }

  public enum Type {
    IBFT_MESSAGE,
    ROUND_EXPIRY
  }
}
