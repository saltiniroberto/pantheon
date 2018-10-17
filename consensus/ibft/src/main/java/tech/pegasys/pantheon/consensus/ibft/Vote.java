package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Optional;

import com.google.common.base.Objects;

public class Vote {
  private final Address recipient;
  private final Ibft2VoteType voteType;

  private Vote(final Address recipient, final Ibft2VoteType voteType) {
    this.recipient = recipient;
    this.voteType = voteType;
  }

  public static Vote authVote(final Address address) {
    return new Vote(address, Ibft2VoteType.ADD);
  }

  public static Vote dropVote(final Address address) {
    return new Vote(address, Ibft2VoteType.DROP);
  }

  public Address getRecipient() {
    return recipient;
  }

  public boolean isAuth() {
    return voteType.equals(Ibft2VoteType.ADD);
  }

  public boolean isDrop() {
    return voteType.equals(Ibft2VoteType.DROP);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Vote vote1 = (Vote) o;
    return recipient.equals(vote1.recipient) && voteType.equals(vote1.voteType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(recipient, voteType);
  }

  public Ibft2VoteType getVoteType() {
    return voteType;
  }

  public static void writTo(final Optional<Vote> optionalVote, final RLPOutput rlpOutput) {
    if (optionalVote.isPresent()) {
      rlpOutput.startList();
      rlpOutput.writeBytesValue(optionalVote.get().recipient);
      optionalVote.get().voteType.writeTo(rlpOutput);
      rlpOutput.endList();
    } else {
      rlpOutput.writeNull();
    }
  }

  public static Optional<Vote> readFrom(final RLPInput rlpInput) {
    if (!rlpInput.nextIsNull()) {
      rlpInput.enterList();
      final Address recipient = Address.readFrom(rlpInput);
      final Ibft2VoteType vote = Ibft2VoteType.readFrom(rlpInput);
      rlpInput.leaveList();

      return Optional.of(new Vote(recipient, vote));
    } else {
      rlpInput.skipNext();
      return Optional.empty();
    }
  }
}
