package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.common.ValidatorVote;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Optional;

public enum Ibft2VoteType implements ValidatorVote {
  ADD((byte) 0xFF),
  DROP((byte) 0x00);

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

  @Override
  public boolean isAddVote() {
    return this.equals(ADD);
  }

  @Override
  public boolean isDropVote() {
    return this.equals(DROP);
  }
}
