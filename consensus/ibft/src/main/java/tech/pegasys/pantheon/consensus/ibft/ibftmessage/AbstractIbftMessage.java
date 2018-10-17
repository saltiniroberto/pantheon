package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import io.netty.buffer.ByteBuf;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.wire.AbstractMessageData;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;

public abstract class AbstractIbftMessage extends AbstractMessageData {
  protected AbstractIbftMessage(final ByteBuf data) {
    super(data);
  }

  public abstract IbftSignedMessageData decode();

  protected static ByteBuf writeMessageToByteBuf(IbftSignedMessageData ibftSignedMessageData)
  {
    // RLP encode the message data content (round identifier and getDigest)
    BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    ibftSignedMessageData.writeTo(rlpEncode);

    final ByteBuf data = NetworkMemoryPool.allocate(rlpEncode.encodedSize());
    data.writeBytes(rlpEncode.encoded().extractArray());

    return data;

  }
}
