package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.Optional;

public class IbftMessageFactory {
  private final KeyPair validatorKayPair;

  public IbftMessageFactory(KeyPair validatorKayPair) {
    this.validatorKayPair = validatorKayPair;
  }

  public IbftSignedMessageData<IbftPrepareUnsignedMessageData> createIbftSignedPrepareMessageData(
      ConsensusRoundIdentifier roundIdentifier, Hash digest) {

    IbftPrepareUnsignedMessageData prepareUnsignedMessageData =
        new IbftPrepareUnsignedMessageData(roundIdentifier, digest);

    return createSignedMessage(prepareUnsignedMessageData);
  }

  public IbftSignedMessageData<IbftRoundChangeUnsignedMessageData>
      createIbftSignedRoundChangeMessageData(
          ConsensusRoundIdentifier roundIdentifier,
          Optional<IbftPreparedCertificate> preparedCertificate) {

    IbftRoundChangeUnsignedMessageData prepareUnsignedMessageData =
        new IbftRoundChangeUnsignedMessageData(roundIdentifier, preparedCertificate);

    return createSignedMessage(prepareUnsignedMessageData);
  }

  private <M extends AbstractIbftUnsignedMessageData> IbftSignedMessageData<M> createSignedMessage(
      M ibftUnsignedMessage) {
    final Signature signature = sign(ibftUnsignedMessage, validatorKayPair);

    return new IbftSignedMessageData<>(
        ibftUnsignedMessage, Util.publicKeyToAddress(validatorKayPair.getPublicKey()), signature);
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
}
