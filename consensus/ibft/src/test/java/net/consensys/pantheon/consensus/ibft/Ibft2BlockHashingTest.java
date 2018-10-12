package net.consensys.pantheon.consensus.ibft;

import static org.assertj.core.api.Java6Assertions.assertThat;

import net.consensys.pantheon.crypto.SECP256K1;
import net.consensys.pantheon.crypto.SECP256K1.KeyPair;
import net.consensys.pantheon.crypto.SECP256K1.PrivateKey;
import net.consensys.pantheon.crypto.SECP256K1.Signature;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.BlockHeader;
import net.consensys.pantheon.ethereum.core.BlockHeaderBuilder;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.core.LogsBloomFilter;
import net.consensys.pantheon.ethereum.core.Util;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import net.consensys.pantheon.util.bytes.BytesValue;
import net.consensys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class Ibft2BlockHashingTest {

  private static final List<KeyPair> COMMITTERS_KEY_PAIRS = committersKeyPairs();
  private static final List<Address> VALIDATORS = Arrays.asList(Address.ECREC, Address.SHA256);
  private static final Address VOTE_RECIPIENT = Address.fromHexString("1");
  private static final Optional<Ibft2VoteType> VOTE = Optional.of(Ibft2VoteType.ADD);
  private static final int ROUND = 0x00FEDCBA;
  private static final BytesValue VANITY_DATA = vainityBytes();

  private static final BlockHeader HEADER_TO_BE_HASHED = headerToBeHashed();
  private static final Hash EXPECTED_HEADER_HASH = expecedHeaderHash();

  @Test
  public void testCalculateHashOfIbft2BlockOnChain() {
    Hash actualHeaderHash = Ibft2BlockHashing.calculateHashOfIbft2BlockOnChain(HEADER_TO_BE_HASHED);
    assertThat(actualHeaderHash).isEqualTo(EXPECTED_HEADER_HASH);
  }

  @Test
  public void testRecoverCommitterAddresses() {
    List<Address> actualCommitterAddresses =
        Ibft2BlockHashing.recoverCommitterAddresses(
            HEADER_TO_BE_HASHED, Ibft2ExtraData.decode(HEADER_TO_BE_HASHED.getExtraData()));

    List<Address> expectedCommitterAddresses =
        COMMITTERS_KEY_PAIRS
            .stream()
            .map(keyPair -> Util.publicKeyToAddress(keyPair.getPublicKey()))
            .collect(Collectors.toList());

    assertThat(actualCommitterAddresses).isEqualTo(expectedCommitterAddresses);
  }

  @Test
  public void testCalculateDataHashForCommittedSeal() {
    Hash dataHahsForCommittedSeal =
        Ibft2BlockHashing.calculateDataHashForCommittedSeal(
            HEADER_TO_BE_HASHED, Ibft2ExtraData.decode(HEADER_TO_BE_HASHED.getExtraData()));

    BlockHeaderBuilder builder = setHeaderFieldsExceptForExtraData();

    List<Signature> commitSeals =
        COMMITTERS_KEY_PAIRS
            .stream()
            .map(keyPair -> SECP256K1.sign(dataHahsForCommittedSeal, keyPair))
            .collect(Collectors.toList());

    Ibft2ExtraData extraDataWithCommitSeals =
        new Ibft2ExtraData(VANITY_DATA, commitSeals, VOTE_RECIPIENT, VOTE, ROUND, VALIDATORS);

    builder.extraData(extraDataWithCommitSeals.encode());
    BlockHeader actualHeader = builder.buildBlockHeader();
    assertThat(actualHeader).isEqualTo(HEADER_TO_BE_HASHED);
  }

  private static List<KeyPair> committersKeyPairs() {
    return IntStream.rangeClosed(1, 4)
        .mapToObj(i -> KeyPair.create(PrivateKey.create(UInt256.of(i).getBytes())))
        .collect(Collectors.toList());
  }

  private static BlockHeaderBuilder setHeaderFieldsExceptForExtraData() {
    final BlockHeaderBuilder builder = new BlockHeaderBuilder();
    builder.parentHash(
        Hash.fromHexString("0xa7762d3307dbf2ae6a1ae1b09cf61c7603722b2379731b6b90409cdb8c8288a0"));
    builder.ommersHash(
        Hash.fromHexString("0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347"));
    builder.coinbase(Address.fromHexString("0x0000000000000000000000000000000000000000"));
    builder.stateRoot(
        Hash.fromHexString("0xca07595b82f908822971b7e848398e3395e59ee52565c7ef3603df1a1fa7bc80"));
    builder.transactionsRoot(
        Hash.fromHexString("0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"));
    builder.receiptsRoot(
        Hash.fromHexString("0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"));
    builder.logsBloom(
        LogsBloomFilter.fromHexString(
            "0x000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "0000"));
    builder.difficulty(UInt256.ONE);
    builder.number(1);
    builder.gasLimit(4704588);
    builder.gasUsed(0);
    builder.timestamp(1530674616);
    builder.mixHash(
        Hash.fromHexString("0x63746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365"));
    builder.nonce(0);
    builder.blockHashFunction(Ibft2BlockHashing::calculateHashOfIbft2BlockOnChain);
    return builder;
  }

  private static BytesValue vainityBytes() {
    final byte[] vanity_bytes = new byte[32];
    for (int i = 0; i < vanity_bytes.length; i++) {
      vanity_bytes[i] = (byte) i;
    }
    return BytesValue.wrap(vanity_bytes);
  }

  private static BlockHeader headerToBeHashed() {
    BlockHeaderBuilder builder = setHeaderFieldsExceptForExtraData();

    Ibft2ExtraData extraDataForCommitSealCalculation =
        new Ibft2ExtraData(VANITY_DATA, new ArrayList<>(), VOTE_RECIPIENT, VOTE, ROUND, VALIDATORS);

    builder.extraData(extraDataForCommitSealCalculation.encode());

    BytesValueRLPOutput rlpForHeaderFroCommittersSigning = new BytesValueRLPOutput();
    builder.buildBlockHeader().writeTo(rlpForHeaderFroCommittersSigning);

    List<Signature> commitSeals =
        COMMITTERS_KEY_PAIRS
            .stream()
            .map(
                keyPair ->
                    SECP256K1.sign(Hash.hash(rlpForHeaderFroCommittersSigning.encoded()), keyPair))
            .collect(Collectors.toList());

    Ibft2ExtraData extraDataWithCommitSeals =
        new Ibft2ExtraData(VANITY_DATA, commitSeals, VOTE_RECIPIENT, VOTE, ROUND, VALIDATORS);

    builder.extraData(extraDataWithCommitSeals.encode());
    return builder.buildBlockHeader();
  }

  private static Hash expecedHeaderHash() {
    BlockHeaderBuilder builder = setHeaderFieldsExceptForExtraData();

    Ibft2ExtraData extraDataForBlockHashCalculation =
        new Ibft2ExtraData(VANITY_DATA, new ArrayList<>(), VOTE_RECIPIENT, VOTE, 0, VALIDATORS);
    builder.extraData(extraDataForBlockHashCalculation.encode());

    BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
    builder.buildBlockHeader().writeTo(rlpOutput);

    return Hash.hash(rlpOutput.encoded());
  }
}
