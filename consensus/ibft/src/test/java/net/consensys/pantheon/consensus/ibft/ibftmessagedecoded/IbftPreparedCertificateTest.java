package net.consensys.pantheon.consensus.ibft.ibftmessagedecoded;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonObject;
import net.consensys.pantheon.consensus.common.VoteTally;
import net.consensys.pantheon.consensus.ibft.IbftContext;
import net.consensys.pantheon.consensus.ibft.IbftExtraData;
import net.consensys.pantheon.consensus.ibft.IbftProtocolSchedule;
import net.consensys.pantheon.consensus.ibft.blockcreation.IbftBlockCreator;
import net.consensys.pantheon.consensus.ibft.ibftmessagedata.IbftPrePrepareMessage;
import net.consensys.pantheon.ethereum.ProtocolContext;
import net.consensys.pantheon.ethereum.chain.MutableBlockchain;
import net.consensys.pantheon.ethereum.core.*;
import net.consensys.pantheon.ethereum.mainnet.ProtocolSchedule;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPInput;
import org.junit.Before;
import org.junit.Test;

import static net.consensys.pantheon.ethereum.core.InMemoryWorldState.createInMemoryWorldStateArchive;
import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import net.consensys.pantheon.crypto.SECP256K1.KeyPair;
import net.consensys.pantheon.crypto.SECP256K1.PrivateKey;
import net.consensys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import net.consensys.pantheon.ethereum.rlp.RLP;
import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.util.bytes.Bytes32;
import net.consensys.pantheon.util.bytes.BytesValue;
import static org.mockito.ArgumentMatchers.any;
import net.consensys.pantheon.util.uint.UInt256;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import javax.swing.text.Style;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IbftPreparedCertificateTest {

    private final String HEX_PRIVATE_KEY =
            "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63";
    private final KeyPair VALIDATOR_KEY_PAIR =
            KeyPair.create(PrivateKey.create(Bytes32.fromHexString(HEX_PRIVATE_KEY)));
    private final Address VALIDATOR_ADDRESS =
            Util.publicKeyToAddress(VALIDATOR_KEY_PAIR.getPublicKey());
    private final int ROUND = 0xFEDCBA98;
    private final long SEQUENCE = 0x1234567890ABCDEFL;
    private final ConsensusRoundIdentifier ROUND_IDENTIFIER = new ConsensusRoundIdentifier(SEQUENCE,ROUND);
    private final long BLOCK_TIMESTAMP = 1_000;
    private final String HEX_ENCODED_PREPARE_MESSAGE =
            "f907b7f9031ff902d9881234567890abcdef84fedcba98f902c8f902c3a0425d3ca1eb9eb4ff6e866e4c3db34b623699afd434a3fd97c4e48152dc81e284a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000101821388808203e8b8d10000000000000000000000000000000000000000000000000000000000000000f8aff86994000000000000000000000000000000000000000194000000000000000000000000000000000000000294000000000000000000000000000000000000000394000000000000000000000000000000000000000494fe3b557e8fb62b89f4916b721be55ceb828dbd73b8413f3f23b41c4c2c0ddfd10fe47a4d6ffcbfe2d9c812c819446635fc8ebc76bb1831b5d0923c95a93c9438a44475c82f768565765954d7c64068553eef044d6d6201c0a063746963616c2062797a616e74696e65206661756c7420746f6c6572616e6365880000000000000000c0c0b841cb87f911fca20df25e2d8f445c5d1e1e9b4f56b251b56251900c23d206e204b0268377d357227d2537799921bb4046c32d4e34e9562f68e7a8e49f06cca91a0a00f90492f873ef881234567890abcdef84fedcba98a0bc36789e7a1e281436464229828f817d6612f7b477d66591ff96a9e064bcc98ab8414e5dd57dcf2d08767bb894da5c4eb2998d00a010f2289dd473ed8da1d2df5d5051ea418da23dbc33064b2dfd6bbadbde0297b14f24a7cc84a77506cd51fa34a200f873ef881234567890abcdf084fedcba99a05fe7f977e71dba2ea1a68e21057beebb9be2ac30c6410aa38d4f3fbe41dcffd2b8413ad0f2b35472ac7f93074935e5d47975b61d8c00ad8494cd04c981e71151fb1f44b4907c9109cbe19f7841bb03e93096a5bcc4a1e3d8ce059d36f3457f6c284500f873ef881234567890abcdf184fedcba9aa0f2ee15ea639b73fa3db9b34a245bdfa015c260c598b211bf05a1ecc4b3e3b4f2b841918d3557d801043d14fbdc26efaf8352a9e0fe5dac5506c43511c5411c01d4395cebacd1ba69c845b60f314d2fb96cbd1c80fa500357b99e1c04a07f5aa1538600f873ef881234567890abcdf284fedcba9ba069c322e3248a5dfc29d73c5b0553b0185a35cd5bb6386747517ef7e53b15e287b84171ba419df56cb49e964d79353839b03bdffc210939bc79f6abff9a4ba3184275053586fa7ab45ed8835fb9275e93f93dbdf621ccfd25aa035886d7687d4e77ae00f873ef881234567890abcdf384fedcba9ca0f343681465b9efe82c933c3e8748c70cb8aa06539c361de20f72eac04e766393b8415252c3ca8522c58c7c20f4cb82cff6abfebd5ef4472b39f8ff0a473ed8c16a884e006605e26dec4c4e5289ce2bce3c233c99c1818aaa3957845bc4bdc76abbba01f873ef881234567890abcdf484fedcba9da0dbb8d0f4c497851a5043c6363657698cb1387682cac2f786c731f8936109d795b841e322b55a422b76bbb4bc9039c16ef071dc744664d267d20e98ea8ecef6f1f00a4705a02840009723e74bf5a416040c03681da98e1bd4627afdcae2fafdff145c00f873ef881234567890abcdf584fedcba9ea0d0591206d9e81e07f4defc5327957173572bcd1bca7838caa7be39b0c12b1873b841c91aa52342bcd86d2c294edd1098bba68972ca140f9d031f9dca24cc258ace9f720ce70b8a2d9216987e0dc713cd844f203394c3b99720711e8c454e5755c17901f873ef881234567890abcdf684fedcba9fa0ee2a4bc7db81da2b7164e56b3649b1e2a09c58c455b15dabddd9146c7582cebcb8418edd7734bcbf26ef9535478b6db3e1629f5205b5470d07a2d4cebe20816939b14cb830460993baaaedd48290e4864f806b205df07c54022a718ce5ced9a464e100f873ef881234567890abcdf784fedcbaa0a0d33e25809fcaa2b6900567812852539da8559dc8b76a7ce3fc5ddd77e8d19a69b8415f23db68997b84f859ec82d01fbbe4c75505217ee2d3ff00c9befaebbcd33a636c1f57d11e712f21956a0bf54acedfa95c6983b306557f5ccc3d5c700fb3d4fa00f873ef881234567890abcdf884fedcbaa1a0b2e7b7a21d986ae84d62a7de4a916f006c4e42a596358b93bad65492d174c4ffb841d466a6c90d946ae55835aca56bb6fed30f83233ad28490fe65d65c8dacf659a06360e2e8a28add91a97ac19711140432009a099932482424ae5c0c4fd4f0be5600";
    private Block BLOCK;

    @Before
    public void setUp(){

        // Construct a parent block.
        final BlockHeaderTestFixture blockHeaderBuilder = new BlockHeaderTestFixture();
        blockHeaderBuilder.gasLimit(5000);
        final BlockHeader parentHeader = blockHeaderBuilder.buildHeader();
        final Optional<BlockHeader> optionalHeader = Optional.of(parentHeader);

        // Construct a block chain and world state
        final MutableBlockchain blockchain = mock(MutableBlockchain.class);
        when(blockchain.getChainHeadHash()).thenReturn(parentHeader.getHash());
        when(blockchain.getBlockHeader(any())).thenReturn(optionalHeader);

//        final KeyPair nodeKeys = KeyPair.generate();
//        // Add the local node as a validator (can't propose a block if node is not a validator).
//        final Address localAddr = Address.extract(Hash.hash(nodeKeys.getPublicKey().getEncodedBytes()));
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
                        blockchain, createInMemoryWorldStateArchive(), new IbftContext(voteTally,null));


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

    @Test
    public void readFromRlp() {
        RLPInput rlp = RLP.input(BytesValue.wrap(Hex.decode(HEX_ENCODED_PREPARE_MESSAGE)));
        IbftPreparedCertificate ibftPreparedCertificate = IbftPreparedCertificate.readFrom(rlp);


        IbftPrePrepareMessageDecoded actualIbftPrePrepareMessage = ibftPreparedCertificate.getIbftPrePrepareMessage();
        Collection<IbftPrepareMessageDecoded> actuaIbftPrepareMessages = ibftPreparedCertificate.getIbftPrepareMessages();

        assertThat(actualIbftPrePrepareMessage.getRoundIdentifier()).isEqualToComparingFieldByField(ROUND_IDENTIFIER);
        assertThat(actualIbftPrePrepareMessage.getBlock()).isEqualTo(BLOCK);
        assertThat(actualIbftPrePrepareMessage.getSender()).isEqualTo(VALIDATOR_ADDRESS);

        int i = 0;
        for(IbftPrepareMessageDecoded ibftPrepareMessage:actuaIbftPrepareMessages)
        {
            PrivateKey privateKey = PrivateKey.create(UInt256.of(i + 1).getBytes());
            KeyPair validatorKeys = KeyPair.create(privateKey);
            Address validatorAddress = Util.publicKeyToAddress(validatorKeys.getPublicKey());

            assertThat(ibftPrepareMessage.getRoundIdentifier()).isEqualToComparingFieldByField(new ConsensusRoundIdentifier(SEQUENCE+i,ROUND+i));
            assertThat(ibftPrepareMessage.getDigest()).isEqualTo(Hash.hash(BytesValue.of(i)));
            assertThat(ibftPrepareMessage.getSender()).isEqualTo(validatorAddress);

            i++;
        }
    }

    @Test
    public void writeToRlp() {
        final int NUM_PREPARE_MESSAGES = 10;

        IbftPrePrepareMessageDecoded expectedIbftPrePrepareMessageDecoded = new IbftPrePrepareMessageDecoded(ROUND_IDENTIFIER,BLOCK,VALIDATOR_KEY_PAIR);
        List<IbftPrepareMessageDecoded> expectedIbftPrepareMessages = new ArrayList<>();
        for(int i=0;i<NUM_PREPARE_MESSAGES;i++)
        {
            PrivateKey privateKey = PrivateKey.create(UInt256.of(i + 1).getBytes());
            KeyPair validatorKeys = KeyPair.create(privateKey);

            expectedIbftPrepareMessages.add(new IbftPrepareMessageDecoded(new ConsensusRoundIdentifier(SEQUENCE+i,ROUND+i),Hash.hash(BytesValue.of(i)),validatorKeys));

        }
        IbftPreparedCertificate expectedIbftPreparedCertificate = new IbftPreparedCertificate(expectedIbftPrePrepareMessageDecoded,expectedIbftPrepareMessages);

        BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
        expectedIbftPreparedCertificate.writeTo(rlpOut);

        byte[] expectedEncoding = Hex.decode(HEX_ENCODED_PREPARE_MESSAGE);
        assertThat(rlpOut.encoded().extractArray()).isEqualTo(expectedEncoding);
    }

    @Test
    public void testWriteToFollowedByReadFrom()
    {
        final int NUM_PREPARE_MESSAGES = 10;
        ArrayList<Address> expectedPrepareMessagesValidatorAddresses = new ArrayList<>(NUM_PREPARE_MESSAGES);

        IbftPrePrepareMessageDecoded expectedIbftPrePrepareMessageDecoded = new IbftPrePrepareMessageDecoded(ROUND_IDENTIFIER,BLOCK,VALIDATOR_KEY_PAIR);
        List<IbftPrepareMessageDecoded> expectedIbftPrepareMessages = new ArrayList<>();
        for(int i=0;i<NUM_PREPARE_MESSAGES;i++)
        {
            PrivateKey privateKey = PrivateKey.create(UInt256.of(i + 1).getBytes());
            KeyPair validatorKeys = KeyPair.create(privateKey);
            Address expectedValidatorAddress = Util.publicKeyToAddress(validatorKeys.getPublicKey());
            expectedPrepareMessagesValidatorAddresses.add(i,expectedValidatorAddress);

            expectedIbftPrepareMessages.add(new IbftPrepareMessageDecoded(new ConsensusRoundIdentifier(SEQUENCE+i,ROUND+i),Hash.hash(BytesValue.of(i)),validatorKeys));

        }
        IbftPreparedCertificate expectedIbftPreparedCertificate = new IbftPreparedCertificate(expectedIbftPrePrepareMessageDecoded,expectedIbftPrepareMessages);

        BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
        expectedIbftPreparedCertificate.writeTo(rlpOut);

        System.out.println(Hex.toHexString(rlpOut.encoded().extractArray()));

        RLPInput rlpInput = RLP.input(rlpOut.encoded());

        IbftPreparedCertificate actualIbftPreparedCetificate;
        actualIbftPreparedCetificate = IbftPreparedCertificate.readFrom(rlpInput);

        IbftPrePrepareMessageDecoded actualIbftPrePrepareMessage = actualIbftPreparedCetificate.getIbftPrePrepareMessage();
        Collection<IbftPrepareMessageDecoded> actuaIbftPrepareMessages = actualIbftPreparedCetificate.getIbftPrepareMessages();

        assertThat(actualIbftPrePrepareMessage.getRoundIdentifier()).isEqualToComparingFieldByField(ROUND_IDENTIFIER);
        assertThat(actualIbftPrePrepareMessage.getBlock()).isEqualTo(BLOCK);
        assertThat(actualIbftPrePrepareMessage.getSender()).isEqualTo(VALIDATOR_ADDRESS);

        int i = 0;
        for(IbftPrepareMessageDecoded ibftPrepareMessage:actuaIbftPrepareMessages)
        {
            Address validatorAddress = expectedPrepareMessagesValidatorAddresses.get(i);

            assertThat(ibftPrepareMessage.getRoundIdentifier()).isEqualToComparingFieldByField(new ConsensusRoundIdentifier(SEQUENCE+i,ROUND+i));
            assertThat(ibftPrepareMessage.getDigest()).isEqualTo(Hash.hash(BytesValue.of(i)));
            assertThat(ibftPrepareMessage.getSender()).isEqualTo(validatorAddress);

            i++;
        }


    }

}