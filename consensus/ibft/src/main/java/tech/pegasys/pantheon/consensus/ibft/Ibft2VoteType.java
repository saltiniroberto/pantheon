package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.ethereum.rlp.RLPException;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

public enum Ibft2VoteType {
  ADD((byte) 0xFF),
  DROP((byte) 0x00);

  private final byte voteValue;

  Ibft2VoteType(final byte voteValue) {
    this.voteValue = voteValue;
  }

  public byte getVoteValue() {
    return voteValue;
  }

  public static Ibft2VoteType readFrom(final RLPInput rlpInput) {
    byte encodedByteValue = rlpInput.readByte();
    for (final Ibft2VoteType voteType : values()) {
      if (voteType.voteValue == encodedByteValue) {
        return voteType;
      }
    }

    throw new RLPException("Invalid Ibft2VoteType RLP encoding");
  }

  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.writeByte(voteValue);
  }
}
