package net.consensys.pantheon.consensus.ibft.ibftmessagedecoded;

import net.consensys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import net.consensys.pantheon.ethereum.core.Address;

public abstract class AbstractIbftInRoundMessageDecoded extends AbstractIbftMessageDecoded {
  protected final ConsensusRoundIdentifier roundIdentifier;

  protected AbstractIbftInRoundMessageDecoded(
      final ConsensusRoundIdentifier roundIdentifier, final Address sender) {
    super(sender);
    this.roundIdentifier = roundIdentifier;
  }

  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundIdentifier;
  }
}
