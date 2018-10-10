package net.consensys.pantheon.consensus.ibft.ibftmessagedecoded;

import net.consensys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import net.consensys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import net.consensys.pantheon.crypto.SECP256K1.KeyPair;
import net.consensys.pantheon.crypto.SECP256K1.Signature;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.core.Util;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.util.bytes.BytesValue;

public class IbftPrepareMessageDecoded extends AbstractIbftInRoundMessageDecoded {
  private static final int TYPE = IbftSubProtocol.NotificationType.PREPARE.getValue();
  private final Hash digest;
  private final Signature signature;

  // Used to avoid running another serialisation when the message will be written out.
  // The message data (round, sequence, getDigest) must be serialised everytime that a message is
  // created in order to either compute the getSender address (if the message is read from rlp)  or
  // create the getSignature if the message is created by "us"
  // Alternative approach: decode each field on demand (e.g. when getSender() is called) and cache
  // the
  // value
  private final BytesValue cachedRlpEncodedIbftMessage;

  /** Constructor used when a validator wants to send a message */
  public IbftPrepareMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest, final KeyPair nodeKeys) {
    super(roundIdentifier, Util.publicKeyToAddress(nodeKeys.getPublicKey()));
    this.digest = digest;

    cachedRlpEncodedIbftMessage = encodeIbftData(roundIdentifier, digest);
    signature = sign(TYPE, cachedRlpEncodedIbftMessage, nodeKeys);
  }

  /** Constructor used only by the {@link #readFrom(RLPInput)} method */
  private IbftPrepareMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier,
      final Hash digest,
      final Address sender,
      final Signature signature,
      final BytesValue encodedIbftMessage) {
    super(roundIdentifier, sender);
    this.digest = digest;

    this.signature = signature;
    cachedRlpEncodedIbftMessage = encodedIbftMessage;
  }

  public Hash getDigest() {
    return digest;
  }

  public static IbftPrepareMessageDecoded readFrom(final RLPInput rlpInput) {
    final Address sender;
    final ConsensusRoundIdentifier roundIdentifier;
    final Hash digest;

    rlpInput.enterList();
    RLPInput ibftMessageData = readIbftMessageData(rlpInput);
    Signature signature = readIbftMessageSignature(rlpInput);
    rlpInput.leaveList();

    sender = recoverSender(TYPE, ibftMessageData, signature);

    ibftMessageData.enterList();
    roundIdentifier = ConsensusRoundIdentifier.readFrom(ibftMessageData);
    digest = readIbftMessageDigest(ibftMessageData);
    ibftMessageData.leaveList();

    return new IbftPrepareMessageDecoded(
        roundIdentifier, digest, sender, signature, ibftMessageData.raw());
  }

  @Override
  protected Signature getSignature() {
    return signature;
  }

  @Override
  protected BytesValue getRlpEncodedMessage() {
    return cachedRlpEncodedIbftMessage;
  }

  private static BytesValue encodeIbftData(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    // RLP encode of the message data content (round identifier and getDigest)
    BytesValueRLPOutput ibftMessage = new BytesValueRLPOutput();
    ibftMessage.startList();
    roundIdentifier.writeTo(ibftMessage);
    ibftMessage.writeBytesValue(digest);
    ibftMessage.endList();

    return ibftMessage.encoded();
  }
}
