package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftMessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftRoundChangeUnsignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import io.netty.buffer.ByteBuf;

public class IbftRoundChangeMessage extends AbstractIbftMessage {

  private static final int MESSAGE_CODE = IbftV2.ROUND_CHANGE_MSG;

  private IbftRoundChangeMessage(final ByteBuf data) {
    super(data);
  }

  public static IbftRoundChangeMessage fromMessage(final MessageData message) {
    if (message instanceof IbftRoundChangeMessage) {
      message.retain();
      return (IbftRoundChangeMessage) message;
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      throw new IllegalArgumentException(
          String.format("Message has code %d and thus is not a RoundChangeMessage", code));
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return new IbftRoundChangeMessage(data);
  }

  // NOTE: Alternative interface: IbftPrepareSignedMessageData decode(final MessageData message).
  // This
  // would avoid having to call the constructor and allocate memory for a ByteBuf when all that is
  // required is to decode the message into an IbftPrepareSignedMessageData class
  @Override
  public IbftSignedMessageData<IbftRoundChangeUnsignedMessageData> decode() {
    return IbftMessageFactory.readSignedIbftRoundChangeMessageFrom(
        RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftRoundChangeMessage create(
      final IbftSignedMessageData<IbftRoundChangeUnsignedMessageData> ibftPrepareMessageDecoded) {

    return new IbftRoundChangeMessage(writeMessageToByteBuf(ibftPrepareMessageDecoded));
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
