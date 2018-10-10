package net.consensys.pantheon.consensus.ibft.ibftmessagedecoded;

import net.consensys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import net.consensys.pantheon.consensus.ibft.IbftBlockHashing;
import net.consensys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import net.consensys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import net.consensys.pantheon.crypto.SECP256K1;
import net.consensys.pantheon.crypto.SECP256K1.KeyPair;
import net.consensys.pantheon.crypto.SECP256K1.Signature;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.Block;
import net.consensys.pantheon.ethereum.core.Util;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.util.bytes.BytesValue;

// NOTE: Implementation of all methods of this class is still pending. This class was added to show
// how a PreparedCertificate is encoded and decoded inside a RoundChange message
public class IbftPrePrepareMessageDecoded extends AbstractIbftInRoundMessageDecoded {
    private final Block block;

  private static final int TYPE = IbftSubProtocol.NotificationType.PREPREPARE.getValue();
  private final Signature signature;

  // Used to avoid running another serialisation when the message will be written out.
  // The message data (round, sequence, getDigest) must be serialised everytime that a message is
  // created in order to either compute the getSender address (if the message is read from rlp)  or
  // create the getSignature if the message is created by "us"
  // Alternative approach: decode each field on demand (e.g. when getSender() is called) and cache the
  // value
  private final BytesValue cachedRlpEncodedIbftMessage;

  public IbftPrePrepareMessageDecoded(
          final ConsensusRoundIdentifier roundIdentifier, final Block block, final KeyPair nodeKeys) {
    super(roundIdentifier, Util.publicKeyToAddress(nodeKeys.getPublicKey()));
    this.block = block;

    cachedRlpEncodedIbftMessage = encodeIbftData(roundIdentifier, block);
    signature = sign(TYPE, cachedRlpEncodedIbftMessage, nodeKeys);
  }

  /** Constructor used only by the {@link #readFrom(RLPInput)} method */
  private IbftPrePrepareMessageDecoded(
          final ConsensusRoundIdentifier roundIdentifier,
          final Block block,
          final Address sender,
          final Signature signature,
          final BytesValue encodedIbftMessage) {
    super(roundIdentifier, sender);
    this.block = block;

    this.signature = signature;
    cachedRlpEncodedIbftMessage = encodedIbftMessage;
  }

  public Block getBlock() {
    return block;
  }

  public static IbftPrePrepareMessageDecoded readFrom(final RLPInput rlpInput) {
    final Address sender;
    final ConsensusRoundIdentifier roundIdentifier;
    final Block block;

    rlpInput.enterList();
    RLPInput ibftMessageData = readIbftMessageData(rlpInput);
    Signature signature = readIbftMessageSignature(rlpInput);
    rlpInput.leaveList();

    sender = recoverSender(TYPE, ibftMessageData, signature);

    ibftMessageData.enterList();
    roundIdentifier = ConsensusRoundIdentifier.readFrom(ibftMessageData);
    block = Block.readFrom(ibftMessageData,IbftBlockHashing::calculateHashOfIbftBlockOnChain);
    ibftMessageData.leaveList();

    return new IbftPrePrepareMessageDecoded(
            roundIdentifier, block, sender, signature, ibftMessageData.raw());
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
          final ConsensusRoundIdentifier roundIdentifier, final Block block) {
    // RLP encode of the message data content (round identifier and getDigest)
    BytesValueRLPOutput ibftMessage = new BytesValueRLPOutput();
    ibftMessage.startList();
    roundIdentifier.writeTo(ibftMessage);
    block.writeTo(ibftMessage);
    ibftMessage.endList();

    return ibftMessage.encoded();
  }
}
