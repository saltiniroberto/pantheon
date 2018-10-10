package net.consensys.pantheon.consensus.ibft.ibftmessagedata;

import net.consensys.pantheon.consensus.ibft.ibftmessagedecoded.IbftPrePrepareMessageDecoded;
import net.consensys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import net.consensys.pantheon.ethereum.p2p.NetworkMemoryPool;
import net.consensys.pantheon.ethereum.p2p.api.MessageData;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import net.consensys.pantheon.ethereum.rlp.RLP;
import net.consensys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import io.netty.buffer.ByteBuf;

public class IbftPrePrepareMessage extends AbstractIbftMessageData {

  private static final int CODE = IbftSubProtocol.NotificationType.PREPREPARE.getValue();

  protected IbftPrePrepareMessage(final ByteBuf data) {
    super(data);
  }

  public static Optional<AbstractIbftMessageData> fromMessage(final MessageData message) {
    if (message instanceof IbftPrePrepareMessage) {
      message.retain();
      return Optional.of((IbftPrePrepareMessage) message);
    }
    final int code = message.getCode();
    if (code != CODE) {
      return Optional.empty();
    }

    final ByteBuf data = NetworkMemoryPool.allocate(message.getSize());
    message.writeTo(data);
    return Optional.of(new IbftPrePrepareMessage(data));
  }

  @Override
  public IbftPrePrepareMessageDecoded decode() {
    return IbftPrePrepareMessageDecoded.readFrom(RLP.input(BytesValue.wrapBuffer(data)));
  }

  public static IbftPrePrepareMessage create(
      final IbftPrePrepareMessageDecoded ibftPrePrepareMessageDecoded) {

    // RLP encode the message data content (round identifier and getDigest)
    BytesValueRLPOutput rlpEncode = new BytesValueRLPOutput();
    ibftPrePrepareMessageDecoded.writeTo(rlpEncode);

    final ByteBuf data = NetworkMemoryPool.allocate(rlpEncode.encodedSize());
    data.writeBytes(rlpEncode.encoded().extractArray());

    return new IbftPrePrepareMessage(data);
  }

  @Override
  public int getCode() {
    return CODE;
  }
}
