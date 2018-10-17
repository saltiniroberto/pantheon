package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftevent.IbftMessageEvent;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;

/** Static helper functions for producing and working with IbftEvent objects */
public class IbftEvents {

  public static IbftEvent fromMessage(final IbftSignedMessageData ibftMessageDecoded) {
    return new IbftMessageEvent(ibftMessageDecoded);
  }

  public enum Type {
    IBFT_MESSAGE,
    ROUND_EXPIRY
  }
}
