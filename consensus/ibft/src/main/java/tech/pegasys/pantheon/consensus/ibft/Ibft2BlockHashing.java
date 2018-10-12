package tech.pegasys.pantheon.consensus.ibft;


import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Ibft2BlockHashing {

  /**
   * Constructs a hash of the block header suitable for signing as a committed seal. The extra data
   * in the hash uses an empty list for the committed seals.
   *
   * @param header The header for which a proposer seal is to be calculated (with or without extra
   *     data)
   * @param ibftExtraData The extra data block which is to be inserted to the header once seal is
   *     calculated
   * @return the hash of the header including the validator and proposer seal in the extra data
   */
  public static Hash calculateDataHashForCommittedSeal(
          final BlockHeader header, final Ibft2ExtraData ibftExtraData) {
    return Hash.hash(serializeHeader(header, ibftExtraData::encodeWithoutCommitSeals));
  }

  /**
   * Constructs a hash of the block header, but omits the committerSeals and sets round number to 0
   * (as these change on each of the potentially circulated blocks at the current chain height).
   *
   * @param header The header for which a block hash is to be calculated
   * @return the hash of the header to be used when referencing the header on the blockchain
   */
  public static Hash calculateHashOfIbft2BlockOnChain(final BlockHeader header) {
    final Ibft2ExtraData ibftExtraData = Ibft2ExtraData.decode(header.getExtraData());
    return Hash.hash(
        serializeHeader(header, ibftExtraData::encodeWithoutCommitSealsAndWithRoundEqualToZero));
  }

  /**
   * Recovers the {@link Address} for each validator that contributed a committed seal to the block.
   *
   * @param header the block header that was signed by the committed seals
   * @param ibftExtraData the parsed {@link Ibft2ExtraData} from the header
   * @return the addresses of validators that provided a committed seal
   */
  public static List<Address> recoverCommitterAddresses(
      final BlockHeader header, final Ibft2ExtraData ibftExtraData) {
    final Hash committerHash =
        Ibft2BlockHashing.calculateDataHashForCommittedSeal(header, ibftExtraData);

    return ibftExtraData
        .getSeals()
        .stream()
        .map(p -> Util.signatureToAddress(p, committerHash))
        .collect(Collectors.toList());
  }

  private static BytesValue serializeHeader(
      final BlockHeader header, final Supplier<BytesValue> extraDataSerializer) {

    // create a block header which is a copy of the header supplied as parameter except of the
    // extraData field
    BlockHeaderBuilder builder = new BlockHeaderBuilder();
    builder
        .parentHash(header.getParentHash())
        .ommersHash(header.getOmmersHash())
        .coinbase(header.getCoinbase())
        .stateRoot(header.getStateRoot())
        .transactionsRoot(header.getTransactionsRoot())
        .receiptsRoot(header.getReceiptsRoot())
        .logsBloom(header.getLogsBloom())
        .difficulty(header.getDifficulty())
        .number(header.getNumber())
        .gasLimit(header.getGasLimit())
        .gasUsed(header.getGasUsed())
        .timestamp(header.getTimestamp())
        .mixHash(header.getMixHash())
        .nonce(header.getNonce())
        .blockHashFunction(Ibft2BlockHashing::calculateHashOfIbft2BlockOnChain);

    // set the extraData field using the supplied extraDataSerializer if the header heigh is not 0
    if (header.getNumber() == 0) {
      builder.extraData(header.getExtraData());
    } else {
      builder.extraData(extraDataSerializer.get());
    }

    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    builder.buildBlockHeader().writeTo(out);
    return out.encoded();
  }
}
