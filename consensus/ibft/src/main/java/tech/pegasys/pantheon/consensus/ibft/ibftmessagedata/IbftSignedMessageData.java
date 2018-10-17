package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

public class IbftSignedMessageData<M extends AbstractIbftUnsignedMessageData> {

  protected final Address sender;
  protected final Signature signature;
  protected final M ibftUnsignedMessageData;

  public IbftSignedMessageData(
      final M ibftUnsignedMessageData, final Address sender, final Signature signature) {
    this.ibftUnsignedMessageData = ibftUnsignedMessageData;
    this.sender = sender;
    this.signature = signature;
  }

  public Address getSender() {
    return sender;
  }

  public Signature getSignature() {
    return signature;
  }

  public M getUnsignedMessageData() {
    return ibftUnsignedMessageData;
  }

  public void writeTo(final RLPOutput output) {

    output.startList();
    ibftUnsignedMessageData.writeTo(output);
    output.writeBytesValue(getSignature().encodedBytes());
    output.endList();
  }

  public static IbftSignedMessageData<IbftPrePrepareUnsignedMessageData>
      readIbftSignedPrePrepareMessageDataFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftPrePrepareUnsignedMessageData unsignedMessageData =
        IbftPrePrepareUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static IbftSignedMessageData<IbftPrepareUnsignedMessageData>
      readIbftSignedPrepareMessageDataFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftPrepareUnsignedMessageData unsignedMessageData =
        IbftPrepareUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  public static IbftSignedMessageData<IbftRoundChangeUnsignedMessageData>
      readIbftSignedRoundChangeMessageDataFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftRoundChangeUnsignedMessageData unsignedMessageData =
        IbftRoundChangeUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    return from(unsignedMessageData, signature);
  }

  protected static <M extends AbstractIbftUnsignedMessageData> IbftSignedMessageData<M> from(
      M unsignedMessageData, Signature signature) {

    final Address sender = recoverSender(unsignedMessageData, signature);

    return new IbftSignedMessageData<>(unsignedMessageData, sender, signature);
  }

  protected static Signature readSignature(final RLPInput signedMessage) {
    return signedMessage.readBytesValue(Signature::decode);
  }

  protected static Address recoverSender(
      final AbstractIbftUnsignedMessageData unsignedMessageData, final Signature signature) {

    return Util.signatureToAddress(
        signature, IbftMessageFactory.hashForSignature(unsignedMessageData));
  }
}
