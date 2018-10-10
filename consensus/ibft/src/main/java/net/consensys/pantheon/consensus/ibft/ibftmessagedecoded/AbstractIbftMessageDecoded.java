package net.consensys.pantheon.consensus.ibft.ibftmessagedecoded;

import net.consensys.pantheon.crypto.SECP256K1;
import net.consensys.pantheon.crypto.SECP256K1.KeyPair;
import net.consensys.pantheon.crypto.SECP256K1.Signature;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.core.Util;
import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.ethereum.rlp.RLPOutput;
import net.consensys.pantheon.util.bytes.BytesValue;
import net.consensys.pantheon.util.bytes.BytesValues;

public abstract class AbstractIbftMessageDecoded {

  protected final Address sender;

  protected AbstractIbftMessageDecoded(final Address sender) {
    this.sender = sender;
  }

  public Address getSender() {
    return sender;
  }

  public void writeTo(final RLPOutput output) {

    output.startList();
    output.writeRLP(getRlpEncodedMessage());
    output.writeBytesValue(getSignature().encodedBytes());
    output.endList();
  }

  protected abstract Signature getSignature();

  protected abstract BytesValue getRlpEncodedMessage();

  protected static Signature sign(
      final int ibftMessageCode, final BytesValue encodedIbftMessageData, final KeyPair nodeKeys) {
    // hashes the concatenation of the ibft mesage code and the ibftMessageData
    Hash hashMessageTypeAndData =
        Hash.hash(
            BytesValues.concatenate(
                BytesValues.ofUnsignedByte(ibftMessageCode), encodedIbftMessageData));

    return SECP256K1.sign(hashMessageTypeAndData, nodeKeys);
  }

  protected static Address recoverSender(
      final int ibftMessageCode, final RLPInput ibftMessageData, final Signature signature) {
    // hashes the concatenation of the ibft mesage code and the ibftMessageData
    Hash ibftMessageDataHash =
        Hash.hash(
            BytesValues.concatenate(
                BytesValues.ofUnsignedByte(ibftMessageCode), ibftMessageData.raw()));

    return Util.signatureToAddress(signature, ibftMessageDataHash);
  }

  protected static RLPInput readIbftMessageData(final RLPInput signedMessage) {
    return signedMessage.readAsRlp();
  }

  protected static Signature readIbftMessageSignature(final RLPInput signedMessage) {
    return signedMessage.readBytesValue(Signature::decode);
  }

  protected static Hash readIbftMessageDigest(final RLPInput ibftMessageData) {
    return Hash.wrap(ibftMessageData.readBytes32());
  }
}
