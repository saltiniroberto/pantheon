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
package tech.pegasys.pantheon.ethereum.chain;

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.LogsBloomFilter;
import tech.pegasys.pantheon.ethereum.core.MutableWorldState;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.core.WorldUpdater;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ScheduleBasedBlockHashFunction;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStorageWorldStateStorage;
import tech.pegasys.pantheon.ethereum.worldstate.DefaultMutableWorldState;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;

public final class GenesisState {

  private static final BlockBody BODY =
      new BlockBody(Collections.emptyList(), Collections.emptyList());

  private final Block block;
  private final List<GenesisAccount> genesisAccounts;

  private GenesisState(final Block block, final List<GenesisAccount> genesisAccounts) {
    this.block = block;
    this.genesisAccounts = genesisAccounts;
  }

  /**
   * Construct a {@link GenesisState} from a JSON string.
   *
   * @param json A JSON string describing the genesis block
   * @param protocolSchedule A protocol Schedule associated with
   * @param <C> The consensus context type
   * @return A new {@link GenesisState}.
   */
  public static <C> GenesisState fromJson(
      final String json, final ProtocolSchedule<C> protocolSchedule) {
    return fromConfig(GenesisConfigFile.fromConfig(json), protocolSchedule);
  }

  /**
   * Construct a {@link GenesisState} from a JSON object.
   *
   * @param config A {@link GenesisConfigFile} describing the genesis block.
   * @param protocolSchedule A protocol Schedule associated with
   * @param <C> The consensus context type
   * @return A new {@link GenesisState}.
   */
  @SuppressWarnings("unchecked")
  public static <C> GenesisState fromConfig(
      final GenesisConfigFile config, final ProtocolSchedule<C> protocolSchedule) {
    final List<GenesisAccount> genesisAccounts =
        parseAllocations(config).collect(Collectors.toList());
    final Block block =
        new Block(
            buildHeader(config, calculateGenesisStateHash(genesisAccounts), protocolSchedule),
            BODY);
    return new GenesisState(block, genesisAccounts);
  }

  public Block getBlock() {
    return block;
  }

  /**
   * Writes the genesis block's world state to the given {@link MutableWorldState}.
   *
   * @param target WorldView to write genesis state to
   */
  public void writeStateTo(final MutableWorldState target) {
    writeAccountsTo(target, genesisAccounts);
  }

  private static void writeAccountsTo(
      final MutableWorldState target, final List<GenesisAccount> genesisAccounts) {
    final WorldUpdater updater = target.updater();
    genesisAccounts.forEach(
        account -> updater.getOrCreate(account.address).setBalance(account.balance));
    updater.commit();
    target.persist();
  }

  private static Hash calculateGenesisStateHash(final List<GenesisAccount> genesisAccounts) {
    final MutableWorldState worldState =
        new DefaultMutableWorldState(
            new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage()));
    writeAccountsTo(worldState, genesisAccounts);
    return worldState.rootHash();
  }

  private static <C> BlockHeader buildHeader(
      final GenesisConfigFile genesis,
      final Hash genesisRootHash,
      final ProtocolSchedule<C> protocolSchedule) {

    return BlockHeaderBuilder.create()
        .parentHash(parseParentHash(genesis))
        .ommersHash(Hash.EMPTY_LIST_HASH)
        .coinbase(parseCoinbase(genesis))
        .stateRoot(genesisRootHash)
        .transactionsRoot(Hash.EMPTY_TRIE_HASH)
        .receiptsRoot(Hash.EMPTY_TRIE_HASH)
        .logsBloom(LogsBloomFilter.empty())
        .difficulty(parseDifficulty(genesis))
        .number(BlockHeader.GENESIS_BLOCK_NUMBER)
        .gasLimit(genesis.getGasLimit())
        .gasUsed(0L)
        .timestamp(genesis.getTimestamp())
        .extraData(parseExtraData(genesis))
        .mixHash(parseMixHash(genesis))
        .nonce(parseNonce(genesis))
        .blockHashFunction(ScheduleBasedBlockHashFunction.create(protocolSchedule))
        .buildBlockHeader();
  }

  private static Address parseCoinbase(final GenesisConfigFile genesis) {
    return genesis
        .getCoinbase()
        .map(Address::fromHexString)
        .orElseGet(() -> Address.wrap(BytesValue.wrap(new byte[Address.SIZE])));
  }

  private static Hash parseParentHash(final GenesisConfigFile genesis) {
    return Hash.wrap(Bytes32.fromHexString(genesis.getParentHash()));
  }

  private static BytesValue parseExtraData(final GenesisConfigFile genesis) {
    return BytesValue.fromHexString(genesis.getExtraData());
  }

  private static UInt256 parseDifficulty(final GenesisConfigFile genesis) {
    return UInt256.fromHexString(genesis.getDifficulty());
  }

  private static Hash parseMixHash(final GenesisConfigFile genesis) {
    return Hash.wrap(Bytes32.fromHexString(genesis.getMixHash()));
  }

  private static long parseNonce(final GenesisConfigFile genesis) {
    String nonce = genesis.getNonce().toLowerCase(Locale.US);
    if (nonce.startsWith("0x")) {
      nonce = nonce.substring(2);
    }
    return Long.parseUnsignedLong(nonce, 16);
  }

  @SuppressWarnings("unchecked")
  private static Stream<GenesisAccount> parseAllocations(final GenesisConfigFile genesis) {
    return genesis
        .getAllocations()
        .map(
            allocation ->
                new GenesisAccount(
                    Address.fromHexString(allocation.getAddress()), allocation.getBalance()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("block", block)
        .add("genesisAccounts", genesisAccounts)
        .toString();
  }

  private static final class GenesisAccount {

    final Address address;
    final Wei balance;

    GenesisAccount(final Address address, final String balance) {
      this.address = address;
      this.balance = parseBalance(balance);
    }

    private Wei parseBalance(final String balance) {
      final BigInteger val;
      if (balance.startsWith("0x")) {
        val = new BigInteger(1, BytesValue.fromHexStringLenient(balance).extractArray());
      } else {
        val = new BigInteger(balance);
      }

      return Wei.of(val);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("address", address)
          .add("balance", balance)
          .toString();
    }
  }
}
