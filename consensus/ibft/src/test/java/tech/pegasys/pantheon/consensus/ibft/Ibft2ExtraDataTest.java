/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final byte[] roundAsByteArray = new byte[] {(byte) 0x00, (byte) 0xFE, (byte) 0xDC, (byte) 0xBA};
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encoded vote
    encoder.startList();
    encoder.writeBytesValue(vote.get().getRecipient());
    vote.get().getVoteType().writeTo(encoder);
    encoder.endList();

    // This is to verify that the decoding works correctly when the round is encoded as 4 bytes
    encoder.writeBytesValue(BytesValue.wrap(roundAsByteArray));
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVote()).isEqualTo(vote);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void incorrectlyEncodedRound() {
    final List<Address> validators = Lists.newArrayList();
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final byte[] roundAsByteArray = new byte[] {(byte) 0xFE, (byte) 0xDC, (byte) 0xBA};
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encoded vote
    encoder.startList();
    encoder.writeBytesValue(vote.get().getRecipient());
    vote.get().getVoteType().writeTo(encoder);
    encoder.endList();

    // This is to verify that the decoding throws an exception when the round number is not encoded
    // in 4 byte format
    encoder.writeBytesValue(BytesValue.wrap(roundAsByteArray));
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    assertThatThrownBy(() -> Ibft2ExtraData.decode(bufferToInject))
        .isInstanceOf(RLPException.class);
  }

  @Test
  public void nullVoteAndListConstituteValidContent() {
    final List<Address> validators = Lists.newArrayList();
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encode an empty vote
    encoder.writeNull();

    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVote().isPresent()).isEqualTo(false);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void emptyVoteAndListIsEncodedCorrectly() {
    final List<Address> validators = Lists.newArrayList();
    Optional<Vote> vote = Optional.empty();
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test
  public void emptyListConstituteValidContent() {
    final List<Address> validators = Lists.newArrayList();
    final Optional<Vote> vote = Optional.of(Vote.dropVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encoded vote
    encoder.startList();
    encoder.writeBytesValue(vote.get().getRecipient());
    vote.get().getVoteType().writeTo(encoder);
    encoder.endList();

    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVote()).isEqualTo(vote);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void emptyListsAreEncodedCorrectly() {
    final List<Address> validators = Lists.newArrayList();
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test
  public void fullyPopulatedDataProducesCorrectlyFormedExtraDataObject() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    // Create randomised vanity data.
    final byte[] vanity_bytes = new byte[32];
    new Random().nextBytes(vanity_bytes);
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList(); // This is required to create a "root node" for all RLP'd data
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encoded vote
    encoder.startList();
    encoder.writeBytesValue(vote.get().getRecipient());
    vote.get().getVoteType().writeTo(encoder);
    encoder.endList();

    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    final Ibft2ExtraData extraData = Ibft2ExtraData.decode(bufferToInject);

    assertThat(extraData.getVanityData()).isEqualTo(vanity_data);
    assertThat(extraData.getVote()).isEqualTo(vote);
    assertThat(extraData.getRound()).isEqualTo(round);
    assertThat(extraData.getSeals()).isEqualTo(committerSeals);
    assertThat(extraData.getValidators()).isEqualTo(validators);
  }

  @Test
  public void fullyPopulatedDateIsEncodedCorrectly() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    Ibft2ExtraData expectedExtraData =
        new Ibft2ExtraData(vanity_data, committerSeals, vote, round, validators);

    Ibft2ExtraData actualExtraData = Ibft2ExtraData.decode(expectedExtraData.encode());

    assertThat(actualExtraData).isEqualToComparingFieldByField(expectedExtraData);
  }

  @Test
  public void incorrectlyStructuredRlpThrowsException() {
    final List<Address> validators = Lists.newArrayList();
    final Optional<Vote> vote = Optional.of(Vote.authVote(Address.fromHexString("1")));
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals = Lists.newArrayList();

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encoded vote
    encoder.startList();
    encoder.writeBytesValue(vote.get().getRecipient());
    vote.get().getVoteType().writeTo(encoder);
    encoder.endList();

    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.writeLong(1);
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    assertThatThrownBy(() -> Ibft2ExtraData.decode(bufferToInject))
        .isInstanceOf(RLPException.class);
  }

  @Test
  public void incorrectVoteTypeThrowsException() {
    final List<Address> validators = Arrays.asList(Address.ECREC, Address.SHA256);
    final Address voteRecipient = Address.fromHexString("1");
    final byte voteType = (byte) 0xAA;
    final int round = 0x00FEDCBA;
    final List<Signature> committerSeals =
        Arrays.asList(
            Signature.create(BigInteger.ONE, BigInteger.TEN, (byte) 0),
            Signature.create(BigInteger.TEN, BigInteger.ONE, (byte) 0));

    // Create a byte buffer with no data.
    final byte[] vanity_bytes = new byte[32];
    final BytesValue vanity_data = BytesValue.wrap(vanity_bytes);

    final BytesValueRLPOutput encoder = new BytesValueRLPOutput();
    encoder.startList();
    encoder.writeBytesValue(vanity_data);
    encoder.writeList(validators, (validator, rlp) -> rlp.writeBytesValue(validator));

    // encode vote
    encoder.startList();
    encoder.writeBytesValue(voteRecipient);
    encoder.writeByte(voteType);
    encoder.endList();

    encoder.writeInt(round);
    encoder.writeList(
        committerSeals, (committer, rlp) -> rlp.writeBytesValue(committer.encodedBytes()));
    encoder.endList();

    final BytesValue bufferToInject = encoder.encoded();

    assertThatThrownBy(() -> Ibft2ExtraData.decode(bufferToInject))
        .isInstanceOf(RLPException.class);
  }
}
