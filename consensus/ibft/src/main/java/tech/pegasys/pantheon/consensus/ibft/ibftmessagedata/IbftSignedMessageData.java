package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
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

  public void writeTo(final RLPOutput output) {

    output.startList();
    ibftUnsignedMessageData.writeTo(output);
    output.writeBytesValue(getSignature().encodedBytes());
    output.endList();
  }
}
