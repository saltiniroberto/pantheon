package tech.pegasys.pantheon.consensus.ibft;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPException;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.google.common.collect.Lists;
import org.junit.Test;

public class Ibft2ExtraDataTest {

  @Test
  public void correctlyCodedRoundConstitutesValidContent() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    final int round = 0x00FEDCBA;
    final byte[] roundAsByteArray = new byte[] {(byte) 0x00, (byte) 0xFE, (byte) 0xDC, (byte) 0xBA};
    final List<Signature> committerSeals = Lists.newArrayList();

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    // This is to verify that the decoding works correctly when the round is encoded as 4 bytes
    encoder.writeBytesValue(voteRecipient);
    encoder.writeNull();
    encoder.writeBytesValue(BytesValue.wrap(roundAsByteArray));
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);
    final BytesValue bufferToInject = BytesValue.wrap(vanity_data, encoder.encoded());

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVoteRecipient()).isEqualTo(voteRecipient);
    assertThat(extraData.getVote().isPresent()).isEqualTo(false);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test(expected = RLPException.class)
  public void incorrectlyEncodedRound() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    final int round = 0x00FEDCBA;
    final byte[] roundAsByteArray = new byte[] {(byte) 0xFE, (byte) 0xDC, (byte) 0xBA};
    final List<Signature> committerSeals = Lists.newArrayList();

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    // This is to verify that the decoding throws an expection when the round number is not encoded
    // in 4 byte format
    encoder.writeBytesValue(voteRecipient);
    encoder.writeNull();
    encoder.writeBytesValue(BytesValue.wrap(roundAsByteArray));
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);
    final BytesValue bufferToInject = BytesValue.wrap(vanity_data, encoder.encoded());

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVoteRecipient()).isEqualTo(voteRecipient);
    assertThat(extraData.getVote().isPresent()).isEqualTo(false);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void nullVoteAndListConstituteValidContent() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    encoder.writeNull();
    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);
    final BytesValue bufferToInject = BytesValue.wrap(vanity_data, encoder.encoded());

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVoteRecipient()).isEqualTo(voteRecipient);
    assertThat(extraData.getVote().isPresent()).isEqualTo(false);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void emptyVoteAndListIsEncodedCorrectly() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    Optional<Ibft2VoteType> vote = Optional.empty();
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, voteRecipient, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test
  public void emptyListsNonNullVoteConstituteValidContent() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    final Ibft2VoteType vote = Ibft2VoteType.ADD;
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    vote.writeTo(encoder);
    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);
    final BytesValue bufferToInject = BytesValue.wrap(vanity_data, encoder.encoded());

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVoteRecipient()).isEqualTo(voteRecipient);
    assertThat(extraData.getVote().get()).isEqualTo(vote);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void emptyListsNonEmptyVoteIsEncodedCorrectly() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    Optional<Ibft2VoteType> vote = Optional.of(Ibft2VoteType.ADD);
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, voteRecipient, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test
  public void fullyPopulatedDataProducesCorrectlyFormedExtraDataObject() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Address voteRecipient = Address.fromHexString("1");
    final Ibft2VoteType vote = Ibft2VoteType.ADD;
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList(); // This is required to create a "root node" for all RLP'd data
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    vote.writeTo(encoder);
    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    // Create randomised vanity data.
    final byte[] vanity_bytes = new byte[32];
    new Random().nextBytes(vanity_bytes);
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);
    final BytesValue bufferToInject = BytesValue.wrap(vanity_data, encoder.encoded());

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVoteRecipient()).isEqualTo(voteRecipient);
    assertThat(extraData.getVote().get()).isEqualTo(vote);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void fullyPopulatedDateIsEncodedCorrectly() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Address voteRecipient = Address.fromHexString("1");
    Optional<Ibft2VoteType> vote = Optional.of(Ibft2VoteType.ADD);
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, voteRecipient, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test(expected = RLPException.class)
  public void incorrectlyStructuredRlpThrowsException() {
    final List<Address> validators = Lists.newArrayList();
    final Address voteRecipient = Address.fromHexString("1");
    final Ibft2VoteType vote = Ibft2VoteType.ADD;
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    vote.writeTo(encoder);
    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.writeLong(1);
    encoder.endList();

    final BytesValue bufferToInject =
        BytesValue.wrap(BytesValue.wrap(new byte[32]), encoder.encoded());

    Ibft2ExtraData.decode(bufferToInject);
  }

  @Test(expected = RLPException.class)
  public void incorrectlySizedVanityDataThrowsException() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Address voteRecipient = Address.fromHexString("1");
    final Ibft2VoteType vote = Ibft2VoteType.ADD;
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));
    encoder.writeBytesValue(voteRecipient);
    vote.writeTo(encoder);
    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject =
        BytesValue.wrap(BytesValue.wrap(new byte[31]), encoder.encoded());

    Ibft2ExtraData.decode(bufferToInject);
  }
}
