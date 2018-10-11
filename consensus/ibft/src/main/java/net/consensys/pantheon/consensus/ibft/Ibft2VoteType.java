package net.consensys.pantheon.consensus.ibft;

import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Optional;

public enum Ibft2VoteType {
  ADD((byte) 0x00),
  DROP((byte) 0xFF);

  private final byte voteValue;

  Ibft2VoteType(final byte voteValue) {
    this.voteValue = voteValue;
  }

  public byte getNonceValue() {
    return voteValue;
  }

  public static Optional<Ibft2VoteType> readFrom(final RLPInput rlpInput) {
    if (rlpInput.nextIsNull()) {
      rlpInput.skipNext();
      return Optional.empty();
    }

    byte encodedByteValue = rlpInput.readByte();
    for (final Ibft2VoteType voteType : values()) {
      if (voteType.voteValue == encodedByteValue) {
        return Optional.of(voteType);
      }
    }
    return Optional.empty();
  }

  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.writeByte(voteValue);
  }
}
