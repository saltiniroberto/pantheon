package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

// NOTE: Implementation of all methods of this class is still pending. This class was added to show
// how a PreparedCertificate is encoded and decoded inside a RoundChange message
public class IbftPrePrepareUnsignedMessageData extends AbstractIbftInRoundUnsignedMessageData {

  public IbftPrePrepareUnsignedMessageData(
      final ConsensusRoundIdentifier roundIdentifier, final Block block) {
    super(roundIdentifier);
  }

  public Block getBlock() {
    return null;
  }

  @Override
  public void writeTo(RLPOutput rlpOutput) {}

  @Override
  public int getMessageType() {
    return 0;
  }

  public static  IbftPrePrepareUnsignedMessageData readFrom(final RLPInput rlpInput)
  {
    return null;
  }
}
