package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import com.google.common.base.Objects;

public class Vote {
  private final Address recipient;
  private final Ibft2VoteType vote;

  private Vote(final Address recipient, final Ibft2VoteType vote) {
    this.recipient = recipient;
    this.vote = vote;
  }

  public static Vote authVote(final Address address) {
    return new Vote(address, Ibft2VoteType.ADD);
  }

  public static Vote dropVote(final Address address) {
    return new Vote(address, Ibft2VoteType.DROP);
  }

  public static Vote noVote() {
    return new Vote(Address.fromHexString("0"), Ibft2VoteType.DROP);
  }

  public Address getRecipient() {
    return recipient;
  }

  public boolean isAuth() {
    return vote.equals(Ibft2VoteType.ADD);
  }

  public boolean isDrop() {
    return vote.equals(Ibft2VoteType.DROP) && !recipient.equals(Address.fromHexString("0"));
  }

  public boolean isNoVote() {
    return vote.equals(Ibft2VoteType.DROP) && recipient.equals(Address.fromHexString("0"));
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
    return recipient.equals(vote1.recipient) && vote.equals(vote1.vote);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(recipient, vote);
  }

  public Ibft2VoteType getVote() {
    return vote;
  }

  public void writTo(final RLPOutput rlpOutput) {
    rlpOutput.writeBytesValue(recipient);
    vote.writeTo(rlpOutput);
  }

  public static Vote readFrom(final RLPInput rlpInput) {
    final Address recipient = Address.readFrom(rlpInput);
    final Ibft2VoteType vote = Ibft2VoteType.readFrom(rlpInput);

    return new Vote(recipient, vote);
  }
}
