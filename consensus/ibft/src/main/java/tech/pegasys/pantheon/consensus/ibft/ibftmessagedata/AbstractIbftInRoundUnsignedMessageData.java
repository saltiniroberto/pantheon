package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;

public abstract class AbstractIbftInRoundUnsignedMessageData
    extends AbstractIbftUnsignedMessageData {
  protected final ConsensusRoundIdentifier roundIdentifier;

  protected AbstractIbftInRoundUnsignedMessageData(final ConsensusRoundIdentifier roundIdentifier) {
    this.roundIdentifier = roundIdentifier;
  }

  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundIdentifier;
  }
}
