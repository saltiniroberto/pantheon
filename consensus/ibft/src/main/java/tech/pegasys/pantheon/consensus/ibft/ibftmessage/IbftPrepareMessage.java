package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrepareUnsignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import io.netty.buffer.ByteBuf;

public class IbftPrepareMessage extends AbstractIbftMessage {

  private static final int MESSAGE_CODE = IbftV2.PREPARE.getValue();

  private IbftPrepareMessage(final ByteBuf data) {
    super(data);
  }

  public static IbftPrepareMessage fromMessage(final MessageData message) {
    if (message instanceof IbftPrepareMessage) {
      message.retain();
      return (IbftPrepareMessage) message;
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      throw new IllegalArgumentException(
          String.format("Message has code %d and thus is not a PrepareMessage", code));
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return new IbftPrepareMessage(data);
  }

  @Override
  public IbftSignedMessageData<IbftPrepareUnsignedMessageData> decode() {
    return IbftSignedMessageData
        .<IbftPrepareUnsignedMessageData>readIbftSignedPrepareMessageDataFrom(
            RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftPrepareMessage create(
      final IbftSignedMessageData<IbftPrepareUnsignedMessageData> ibftPrepareMessageDecoded) {

    return new IbftPrepareMessage(writeMessageToByteBuf(ibftPrepareMessageDecoded));
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
