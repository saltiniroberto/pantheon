package net.consensys.pantheon.consensus.ibft.ibftmessagedata;

import net.consensys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;
import net.consensys.pantheon.ethereum.p2p.wire.AbstractMessageData;

import io.netty.buffer.ByteBuf;

public abstract class AbstractIbftMessageData extends AbstractMessageData {
  protected AbstractIbftMessageData(final ByteBuf data) {
    super(data);
  }

  public abstract AbstractIbftMessageDecoded decode();
}
