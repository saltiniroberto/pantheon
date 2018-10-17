package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftV2;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

public class IbftPrepareUnsignedMessageData extends AbstractIbftInRoundUnsignedMessageData {
  private static final int TYPE = IbftV2.PREPARE.getValue();
  private final Hash digest;

  /** Constructor used when a validator wants to send a message */
  public IbftPrepareUnsignedMessageData(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    super(roundIdentifier);
    this.digest = digest;
  }

  public static IbftPrepareUnsignedMessageData readFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final ConsensusRoundIdentifier roundIdentifier = ConsensusRoundIdentifier.readFrom(rlpInput);
    final Hash digest = readDigest(rlpInput);
    rlpInput.leaveList();

    return new IbftPrepareUnsignedMessageData(roundIdentifier, digest);
  }

  @Override
  public void writeTo(final RLPOutput rlpOutput) {

    rlpOutput.startList();
    roundIdentifier.writeTo(rlpOutput);
    rlpOutput.writeBytesValue(digest);
    rlpOutput.endList();
  }

  @Override
  public int getMessageType() {
    return TYPE;
  }

  public Hash getDigest() {
    return digest;
  }
}
