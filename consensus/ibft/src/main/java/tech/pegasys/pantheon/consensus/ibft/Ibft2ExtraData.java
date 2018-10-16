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

/**
 * Represents the data structure stored in the extraData field of the BlockHeader used when
 * operating under an IBFT 2.0 consensus mechanism.
 */
public class Ibft2ExtraData {

  public static final int EXTRA_VANITY_LENGTH = 32;

  private final BytesValue vanityData;
  private final List<Signature> seals;
  private final Vote vote;
  private final int round;
  private final List<Address> validators;

  public Ibft2ExtraData(
      final BytesValue vanityData,
      final List<Signature> seals,
      final Vote vote,
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
  }

  public static Ibft2ExtraData decode(final BytesValue input) {
    checkArgument(
        input.size() > EXTRA_VANITY_LENGTH,
        "Invalid BytesValue supplied - too short to produce a valid IBFT Extra Data object.");

    final RLPInput rlpInput = new BytesValueRLPInput(input, false);

    rlpInput.enterList(); // This accounts for the "root node" which contains IBFT data items.
    final BytesValue vanityData = rlpInput.readBytesValue();
    final List<Address> validators = rlpInput.readList(Address::readFrom);
    final Vote vote = Vote.readFrom(rlpInput);
    final int round = rlpInput.readInt();
    final List<Signature> seals = rlpInput.readList(rlp -> Signature.decode(rlp.readBytesValue()));
    rlpInput.leaveList();

    return new Ibft2ExtraData(vanityData, seals, vote, round, validators);
  }

  public BytesValue encode() {
    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanityData);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    vote.writTo(encoder);
    encoder.writeInt(round);
    encoder.writeList(seals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    return encoder.encoded();
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

  public Vote getVote() {
    return vote;
  }

  public int getRound() {
    return round;
  }
}