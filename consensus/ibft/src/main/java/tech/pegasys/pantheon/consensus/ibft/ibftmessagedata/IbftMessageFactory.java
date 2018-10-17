package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.Optional;

public class IbftMessageFactory {
  private final KeyPair valdiatorKayPair;

  public IbftMessageFactory(KeyPair valdiatorKayPair) {
    this.valdiatorKayPair = valdiatorKayPair;
  }

  public IbftSignedMessageData<IbftPrepareUnsignedMessageData> createIbftPrepareMessageData(
      ConsensusRoundIdentifier roundIdentifier, Hash digest) {

    IbftPrepareUnsignedMessageData prepareUnsignedMessageData =
        new IbftPrepareUnsignedMessageData(roundIdentifier, digest);

    final Signature signature = sign(prepareUnsignedMessageData, valdiatorKayPair);

    return new IbftSignedMessageData<>(
        new IbftPrepareUnsignedMessageData(roundIdentifier, digest),
        Util.publicKeyToAddress(valdiatorKayPair.getPublicKey()),
        signature);
  }

  public IbftSignedMessageData<IbftRoundChangeUnsignedMessageData> createIbftRoundChangeMessageData(
      ConsensusRoundIdentifier roundIdentifier,
      Optional<IbftPreparedCertificate> preparedCertificate) {

    IbftRoundChangeUnsignedMessageData prepareUnsignedMessageData =
        new IbftRoundChangeUnsignedMessageData(roundIdentifier, preparedCertificate);

    final Signature signature = sign(prepareUnsignedMessageData, valdiatorKayPair);

    return new IbftSignedMessageData<>(
        new IbftRoundChangeUnsignedMessageData(roundIdentifier, preparedCertificate),
        Util.publicKeyToAddress(valdiatorKayPair.getPublicKey()),
        signature);
  }

  public static IbftSignedMessageData<IbftPrepareUnsignedMessageData>
      readSignedIbftPrepareMessageFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftPrepareUnsignedMessageData unsignedMessageData =
        IbftPrepareUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    final Address sender = recoverSender(unsignedMessageData, signature);

    return new IbftSignedMessageData<>(unsignedMessageData, sender, signature);
  }

  public static IbftSignedMessageData<IbftPrePrepareUnsignedMessageData>
      readSignedIbftPrePrepareMessageFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftPrePrepareUnsignedMessageData unsignedMessageData =
        IbftPrePrepareUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    final Address sender = recoverSender(unsignedMessageData, signature);

    return new IbftSignedMessageData<>(unsignedMessageData, sender, signature);
  }

  public static IbftSignedMessageData<IbftRoundChangeUnsignedMessageData>
      readSignedIbftRoundChangeMessageFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final IbftRoundChangeUnsignedMessageData unsignedMessageData =
        IbftRoundChangeUnsignedMessageData.readFrom(rlpInput);
    final Signature signature = readSignature(rlpInput);
    rlpInput.leaveList();

    final Address sender = recoverSender(unsignedMessageData, signature);

    return new IbftSignedMessageData<>(unsignedMessageData, sender, signature);
  }

  protected static Hash hashForSignature(AbstractIbftUnsignedMessageData unsignedMessageData) {
    return Hash.hash(
        BytesValues.concatenate(
            BytesValues.ofUnsignedByte(unsignedMessageData.getMessageType()),
            unsignedMessageData.encoded()));
  }

  protected static Signature sign(
      final AbstractIbftUnsignedMessageData unsignedMessageData, final KeyPair nodeKeys) {

    return SECP256K1.sign(hashForSignature(unsignedMessageData), nodeKeys);
  }

  protected static Address recoverSender(
      final AbstractIbftUnsignedMessageData unsignedMessageData, final Signature signature) {

    return Util.signatureToAddress(signature, hashForSignature(unsignedMessageData));
  }

  protected static Signature readSignature(final RLPInput signedMessage) {
    return signedMessage.readBytesValue(Signature::decode);
  }
}
