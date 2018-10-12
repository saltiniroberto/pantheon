package tech.pegasys.pantheon.consensus.ibft;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPInput;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.Optional;

/**
 * Represents the data structure stored in the extraData field of the BlockHeader used when
 * operating under an IBFT 2.0 consensus mechanism.
 */
public class Ibft2ExtraData {

  public static final int EXTRA_VANITY_LENGTH = 32;

  private final BytesValue vanityData;
  private final List<Signature> seals;
  private final Address voteRecipient;
  private final Optional<Ibft2VoteType> vote;
  private final int round;
  private final List<Address> validators;

  public Ibft2ExtraData(
      final BytesValue vanityData,
      final List<Signature> seals,
      final Address voteRecipient,
      final Optional<Ibft2VoteType> vote,
      final int round,
      final List<Address> validators) {

    checkNotNull(vanityData);
    checkNotNull(seals);
    checkNotNull(validators);

    this.vanityData = vanityData;
    this.seals = seals;
    this.round = round;
    this.validators = validators;
    this.vote = vote;
    this.voteRecipient = voteRecipient;
  }

  public static Ibft2ExtraData decode(final BytesValue input) {
    checkArgument(
        input.size() > EXTRA_VANITY_LENGTH,
        "Invalid BytesValue supplied - too short to produce a valid IBFT Extra Data object.");

    final BytesValue vanityData = input.slice(0, EXTRA_VANITY_LENGTH);

    final BytesValue rlpData = input.slice(EXTRA_VANITY_LENGTH);
    final RLPInput rlpInput = new BytesValueRLPInput(rlpData, false);

    rlpInput.enterList(); // This accounts for the "root node" which contains IBFT data items.
    final List<Address> validators = rlpInput.readList(Address::readFrom);
    final Address voteRecipient = Address.readFrom(rlpInput);
    final Optional<Ibft2VoteType> vote = Ibft2VoteType.readFrom(rlpInput);
    final int round = rlpInput.readInt();
    final List<Signature> seals = rlpInput.readList(rlp -> Signature.decode(rlp.readBytesValue()));
    rlpInput.leaveList();

    return new Ibft2ExtraData(vanityData, seals, voteRecipient, vote, round, validators);
  }

  public BytesValue encode() {
    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    if (vote.isPresent()) {
      vote.get().writeTo(encoder);
    } else {
      encoder.writeNull();
    }
    encoder.writeInt(round);
    encoder.writeList(seals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    return BytesValue.wrap(vanityData, encoder.encoded());
  }

  public BytesValue encodeWithoutCommitSeals() {
    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    if (vote.isPresent()) {
      vote.get().writeTo(encoder);
    } else {
      encoder.writeNull();
    }
    encoder.writeInt(round);
    encoder.startList();
    encoder.endList();
    encoder.endList();

    return BytesValue.wrap(vanityData, encoder.encoded());
  }

  public BytesValue encodeWithoutCommitSealsAndWithRoundEqualToZero() {
    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    if (vote.isPresent()) {
      vote.get().writeTo(encoder);
    } else {
      encoder.writeNull();
    }
    encoder.writeInt(0);
    encoder.startList();
    encoder.endList();
    encoder.endList();

    return BytesValue.wrap(vanityData, encoder.encoded());
  }

  // Accessors
  public BytesValue getVanityData() {
    return vanityData;
  }

  public List<Signature> getSeals() {
    return seals;
  }

  public List<Address> getValidators() {
    return validators;
  }

  public Address getVoteRecipient() {
    return voteRecipient;
  }

  public Optional<Ibft2VoteType> getVote() {
    return vote;
  }

  public int getRound() {
    return round;
  }
}
