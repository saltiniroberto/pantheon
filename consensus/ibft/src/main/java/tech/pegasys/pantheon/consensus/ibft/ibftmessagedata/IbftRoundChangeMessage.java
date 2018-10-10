package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.IbftRoundChangeMessageDecoded;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.ethereum.p2p.NetworkMemoryPool;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import io.netty.buffer.ByteBuf;

public class IbftRoundChangeMessage extends AbstractIbftMessageData {

  private static final int CODE = IbftSubProtocol.NotificationType.ROUND_CHANGE.getValue();

  protected IbftRoundChangeMessage(final ByteBuf data) {
    super(data);
  }

  public static Optional<AbstractIbftMessageData> fromMessage(final MessageData message) {
    if (message instanceof IbftRoundChangeMessage) {
      message.retain();
      return Optional.of((IbftRoundChangeMessage) message);
    }
    final int code = message.getCode();
    if (code != CODE) {
      return Optional.empty();
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return Optional.of(new IbftRoundChangeMessage(data));
  }

  // NOTE: Alternatvie interface: IbftPrepareMessageDecoded decode(final MessageData message). This
  // would avoid
  // having to call the constructor and allocate memory for a ByteBuf when all that is required is
  // to decode the message into an IbftRoundChangeMessageDecoded class
  @Override
  public IbftRoundChangeMessageDecoded decode() {
    return IbftRoundChangeMessageDecoded.readFrom(RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftRoundChangeMessage create(
      final IbftRoundChangeMessageDecoded ibftRoundChangeMessageDecoded) {

    // RLP encode the message data content (round identifier and getDigest)
    BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    ibftRoundChangeMessageDecoded.writeTo(rlpEncode);

    final ByteBuf data = NetworkMemoryPool.allocate(rlpEncode.encodedSize());
    data.writeBytes(rlpEncode.encoded().extractArray());

    return new IbftRoundChangeMessage(data);
  }

  @Override
  public int getCode() {
    return CODE;
  }
}