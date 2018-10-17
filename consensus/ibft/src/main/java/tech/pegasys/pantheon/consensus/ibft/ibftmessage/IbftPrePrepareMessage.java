package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import io.netty.buffer.ByteBuf;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftMessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrePrepareUnsignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

public class IbftPrePrepareMessage extends AbstractIbftMessage {

  private static final int MESSAGE_CODE = IbftSubProtocol.NotificationType.PREPREPARE.getValue();

  private IbftPrePrepareMessage(final ByteBuf data) {
    super(data);
  }

  public static Optional<IbftPrePrepareMessage> fromMessage(final MessageData message) {
    if (message instanceof IbftPrePrepareMessage) {
      message.retain();
      return Optional.of((IbftPrePrepareMessage) message);
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      return Optional.empty();
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return Optional.of(new IbftPrePrepareMessage(data));
  }

  // NOTE: Alternative interface: IbftPrepareSignedMessageData decode(final MessageData message).
  // This
  // would avoid having to call the constructor and allocate memory for a ByteBuf when all that is
  // required is
  // to decode the message into an IbftPrePrepareSignedMessageData class

  @Override
  public IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> decode() {
    return IbftMessageFactory.readSignedIbftPrePrepareMessageFrom(RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftPrePrepareMessage create(
          final IbftSignedMessageData<IbftPrePrepareUnsignedMessageData>  ibftPrepareMessageDecoded) {

    return new IbftPrePrepareMessage(writeMessageToByteBuf(ibftPrepareMessageDecoded));
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
