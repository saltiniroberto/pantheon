package tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.consensus.common.VoteTally;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.IbftContext;
import tech.pegasys.pantheon.consensus.ibft.IbftExtraData;
import tech.pegasys.pantheon.consensus.ibft.IbftProtocolSchedule;
import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.PrivateKey;
import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.chain.MutableBlockchain;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.PendingTransactions;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.ethereum.core.InMemoryWorldState.createInMemoryWorldStateArchive;

public class IbftPreparedCertificateTest {

  private final String HEX_PRIVATE_KEY =
      "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63";
  private final KeyPair VALIDATOR_KEY_PAIR =
      KeyPair.create(PrivateKey.create(Bytes32.fromHexString(HEX_PRIVATE_KEY)));
  private final Address VALIDATOR_ADDRESS =
      Util.publicKeyToAddress(VALIDATOR_KEY_PAIR.getPublicKey());
  private final int ROUND = 0xFEDCBA98;
  private final long SEQUENCE = 0x1234567890ABCDEFL;
  private final ConsensusRoundIdentifier ROUND_IDENTIFIER =
      new ConsensusRoundIdentifier(SEQUENCE, ROUND);
  private final long BLOCK_TIMESTAMP = 1_000;
  private Block BLOCK;

  @Before
  public void setUp() {

    // Construct a parent block.
    final BlockHeaderTestFixture blockHeaderBuilder = new BlockHeaderTestFixture();
    blockHeaderBuilder.gasLimit(5000);
    final BlockHeader parentHeader = blockHeaderBuilder.buildHeader();
    final Optional<BlockHeader> optionalHeader = Optional.of(parentHeader);

    // Construct a block chain and world state
    final MutableBlockchain blockchain = mock(MutableBlockchain.class);
    when(blockchain.getChainHeadHash()).thenReturn(parentHeader.getHash());
    when(blockchain.getBlockHeader(any())).thenReturn(optionalHeader);

    final List<Address> initialValidatorList =
        Arrays.asList(
            Address.fromHexString(String.format("%020d", 1)),
            Address.fromHexString(String.format("%020d", 2)),
            Address.fromHexString(String.format("%020d", 3)),
            Address.fromHexString(String.format("%020d", 4)),
            VALIDATOR_ADDRESS);

    final VoteTally voteTally = new VoteTally(initialValidatorList);

    final ProtocolSchedule<IbftContext> protocolSchedule =
        IbftProtocolSchedule.create(new JsonObject("{\"spuriousDragonBlock\":0}"));
    final ProtocolContext<IbftContext> protContext =
        new ProtocolContext<>(
            blockchain, createInMemoryWorldStateArchive(), new IbftContext(voteTally, null));

    final IbftBlockCreator blockCreator =
        new IbftBlockCreator(
            Address.fromHexString(String.format("%020d", 0)),
            parent ->
                new IbftExtraData(
                        BytesValue.wrap(new byte[32]),
                        Lists.newArrayList(),
                        null,
                        initialValidatorList)
                    .encode(),
            new PendingTransactions(1),
            protContext,
            protocolSchedule,
            parentGasLimit -> parentGasLimit,
            VALIDATOR_KEY_PAIR,
            Wei.ZERO,
            parentHeader);

    BLOCK = blockCreator.createBlock(BLOCK_TIMESTAMP);
  }

  /**
   * NOTE: Is the following test good enough or do we need to go to the extent of the {@link
   * IbftPrepareMessageDecodedTest#writeToRlp()} and {@link
   * IbftPrepareMessageDecodedTest#readFromRlp()} ()} tests in IbftPreparedMessageDecodedTest.java ?
   */
  //
  @Test
  public void testWriteToFollowedByReadFrom() {
    final int NUM_PREPARE_MESSAGES = 10;
    ArrayList<Address> expectedPrepareMessagesValidatorAddresses =
        new ArrayList<>(NUM_PREPARE_MESSAGES);

    IbftPrePrepareMessageDecoded expectedIbftPrePrepareMessageDecoded =
        new IbftPrePrepareMessageDecoded(ROUND_IDENTIFIER, BLOCK, VALIDATOR_KEY_PAIR);
    List<IbftPrepareMessageDecoded> expectedIbftPrepareMessages = new ArrayList<>();
    for (int i = 0; i < NUM_PREPARE_MESSAGES; i++) {
      PrivateKey privateKey = PrivateKey.create(UInt256.of(i + 1).getBytes());
      KeyPair validatorKeys = KeyPair.create(privateKey);
      Address expectedValidatorAddress = Util.publicKeyToAddress(validatorKeys.getPublicKey());
      expectedPrepareMessagesValidatorAddresses.add(i, expectedValidatorAddress);

      expectedIbftPrepareMessages.add(
          new IbftPrepareMessageDecoded(
              new ConsensusRoundIdentifier(SEQUENCE + i, ROUND + i),
              Hash.hash(BytesValue.of(i)),
              validatorKeys));
    }
    IbftPreparedCertificate expectedIbftPreparedCertificate =
        new IbftPreparedCertificate(
            expectedIbftPrePrepareMessageDecoded, expectedIbftPrepareMessages);

    BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    expectedIbftPreparedCertificate.writeTo(rlpOut);

    RLPInput rlpInput = RLP.input(rlpOut.encoded());

    IbftPreparedCertificate actualIbftPreparedCetificate;
    actualIbftPreparedCetificate = IbftPreparedCertificate.readFrom(rlpInput);

    IbftPrePrepareMessageDecoded actualIbftPrePrepareMessage =
        actualIbftPreparedCetificate.getIbftPrePrepareMessage();
    Collection<IbftPrepareMessageDecoded> actuaIbftPrepareMessages =
        actualIbftPreparedCetificate.getIbftPrepareMessages();

    assertThat(actualIbftPrePrepareMessage.getRoundIdentifier())
        .isEqualToComparingFieldByField(ROUND_IDENTIFIER);
    assertThat(actualIbftPrePrepareMessage.getBlock()).isEqualTo(BLOCK);
    assertThat(actualIbftPrePrepareMessage.getSender()).isEqualTo(VALIDATOR_ADDRESS);

    int i = 0;
    for (IbftPrepareMessageDecoded ibftPrepareMessage : actuaIbftPrepareMessages) {
      Address validatorAddress = expectedPrepareMessagesValidatorAddresses.get(i);

      assertThat(ibftPrepareMessage.getRoundIdentifier())
          .isEqualToComparingFieldByField(new ConsensusRoundIdentifier(SEQUENCE + i, ROUND + i));
      assertThat(ibftPrepareMessage.getDigest()).isEqualTo(Hash.hash(BytesValue.of(i)));
      assertThat(ibftPrepareMessage.getSender()).isEqualTo(validatorAddress);

      i++;
    }
  }
}
