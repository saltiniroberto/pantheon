/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.ethereum.eth.sync.worldstate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.Account;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator.BlockOptions;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.MutableWorldState;
import tech.pegasys.pantheon.ethereum.core.WorldState;
import tech.pegasys.pantheon.ethereum.eth.manager.DeterministicEthScheduler.TimeoutPolicy;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManager;
import tech.pegasys.pantheon.ethereum.eth.manager.EthProtocolManagerTestUtil;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.RespondingEthPeer.Responder;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStorageWorldStateStorage;
import tech.pegasys.pantheon.ethereum.trie.MerklePatriciaTrie;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateArchive;
import tech.pegasys.pantheon.ethereum.worldstate.WorldStateStorage;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import tech.pegasys.pantheon.services.queue.BigQueue;
import tech.pegasys.pantheon.services.queue.InMemoryBigQueue;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class WorldStateDownloaderTest {

  private static final Hash EMPTY_TRIE_ROOT = Hash.wrap(MerklePatriciaTrie.EMPTY_TRIE_NODE_HASH);

  @Test
  public void downloadWorldStateFromPeers_onePeerOneWithManyRequestsOneAtATime() {
    downloadAvailableWorldStateFromPeers(1, 50, 1, 1);
  }

  @Test
  public void downloadWorldStateFromPeers_onePeerOneWithManyRequests() {
    downloadAvailableWorldStateFromPeers(1, 50, 1, 10);
  }

  @Test
  public void downloadWorldStateFromPeers_onePeerWithSingleRequest() {
    downloadAvailableWorldStateFromPeers(1, 1, 100, 10);
  }

  @Test
  public void downloadWorldStateFromPeers_largeStateFromMultiplePeers() {
    downloadAvailableWorldStateFromPeers(5, 100, 10, 10);
  }

  @Test
  public void downloadWorldStateFromPeers_smallStateFromMultiplePeers() {
    downloadAvailableWorldStateFromPeers(5, 5, 1, 10);
  }

  @Test
  public void downloadWorldStateFromPeers_singleRequestWithMultiplePeers() {
    downloadAvailableWorldStateFromPeers(5, 1, 50, 50);
  }

  @Test
  public void downloadEmptyWorldState() {
    BlockDataGenerator dataGen = new BlockDataGenerator(1);
    final EthProtocolManager ethProtocolManager = EthProtocolManagerTestUtil.create();
    final BlockHeader header =
        dataGen
            .block(BlockOptions.create().setStateRoot(EMPTY_TRIE_ROOT).setBlockNumber(10))
            .getHeader();

    // Create some peers
    List<RespondingEthPeer> peers =
        Stream.generate(
                () -> EthProtocolManagerTestUtil.createPeer(ethProtocolManager, header.getNumber()))
            .limit(5)
            .collect(Collectors.toList());

    BigQueue<NodeDataRequest> queue = new InMemoryBigQueue<>();
    WorldStateStorage localStorage =
        new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage());
    WorldStateDownloader downloader =
        new WorldStateDownloader(
            ethProtocolManager.ethContext(),
            localStorage,
            queue,
            10,
            10,
            NoOpMetricsSystem.NO_OP_LABELLED_TIMER);

    CompletableFuture<Void> future = downloader.run(header);
    assertThat(future).isDone();

    // Peers should not have been queried
    for (RespondingEthPeer peer : peers) {
      assertThat(peer.hasOutstandingRequests()).isFalse();
    }
  }

  @Test
  public void canRecoverFromTimeouts() {
    BlockDataGenerator dataGen = new BlockDataGenerator(1);
    TimeoutPolicy timeoutPolicy = TimeoutPolicy.timeoutXTimes(2);
    final EthProtocolManager ethProtocolManager = EthProtocolManagerTestUtil.create(timeoutPolicy);

    // Setup "remote" state
    final WorldStateStorage remoteStorage =
        new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage());
    final WorldStateArchive remoteWorldStateArchive = new WorldStateArchive(remoteStorage);
    final MutableWorldState remoteWorldState = remoteWorldStateArchive.getMutable();

    // Generate accounts and save corresponding state root
    final List<Account> accounts = dataGen.createRandomAccounts(remoteWorldState, 20);
    final Hash stateRoot = remoteWorldState.rootHash();
    assertThat(stateRoot).isNotEqualTo(EMPTY_TRIE_ROOT); // Sanity check
    final BlockHeader header =
        dataGen.block(BlockOptions.create().setStateRoot(stateRoot).setBlockNumber(10)).getHeader();

    // Create some peers
    List<RespondingEthPeer> peers =
        Stream.generate(
                () -> EthProtocolManagerTestUtil.createPeer(ethProtocolManager, header.getNumber()))
            .limit(5)
            .collect(Collectors.toList());

    BigQueue<NodeDataRequest> queue = new InMemoryBigQueue<>();
    WorldStateStorage localStorage =
        new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage());
    WorldStateDownloader downloader =
        new WorldStateDownloader(
            ethProtocolManager.ethContext(),
            localStorage,
            queue,
            10,
            10,
            NoOpMetricsSystem.NO_OP_LABELLED_TIMER);

    CompletableFuture<Void> result = downloader.run(header);

    // Respond to node data requests
    Responder responder =
        RespondingEthPeer.blockchainResponder(mock(Blockchain.class), remoteWorldStateArchive);
    while (!result.isDone()) {
      for (RespondingEthPeer peer : peers) {
        peer.respond(responder);
      }
    }

    // Check that all expected account data was downloaded
    WorldStateArchive localWorldStateArchive = new WorldStateArchive(localStorage);
    final WorldState localWorldState = localWorldStateArchive.get(stateRoot);
    assertThat(result).isDone();
    assertAccountsMatch(localWorldState, accounts);
  }

  private void downloadAvailableWorldStateFromPeers(
      final int peerCount,
      final int accountCount,
      final int hashesPerRequest,
      final int maxOutstandingRequests) {
    final EthProtocolManager ethProtocolManager = EthProtocolManagerTestUtil.create();
    final int trailingPeerCount = 5;
    BlockDataGenerator dataGen = new BlockDataGenerator(1);

    // Setup "remote" state
    final WorldStateStorage remoteStorage =
        new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage());
    final WorldStateArchive remoteWorldStateArchive = new WorldStateArchive(remoteStorage);
    final MutableWorldState remoteWorldState = remoteWorldStateArchive.getMutable();

    // Generate accounts and save corresponding state root
    final List<Account> accounts = dataGen.createRandomAccounts(remoteWorldState, accountCount);
    final Hash stateRoot = remoteWorldState.rootHash();
    assertThat(stateRoot).isNotEqualTo(EMPTY_TRIE_ROOT); // Sanity check
    final BlockHeader header =
        dataGen.block(BlockOptions.create().setStateRoot(stateRoot).setBlockNumber(10)).getHeader();

    // Generate more data that should not be downloaded
    final List<Account> otherAccounts = dataGen.createRandomAccounts(remoteWorldState, 5);
    Hash otherStateRoot = remoteWorldState.rootHash();
    BlockHeader otherHeader =
        dataGen
            .block(BlockOptions.create().setStateRoot(otherStateRoot).setBlockNumber(11))
            .getHeader();
    assertThat(otherStateRoot).isNotEqualTo(stateRoot); // Sanity check

    BigQueue<NodeDataRequest> queue = new InMemoryBigQueue<>();
    WorldStateStorage localStorage =
        new KeyValueStorageWorldStateStorage(new InMemoryKeyValueStorage());
    WorldStateArchive localWorldStateArchive = new WorldStateArchive(localStorage);
    WorldStateDownloader downloader =
        new WorldStateDownloader(
            ethProtocolManager.ethContext(),
            localStorage,
            queue,
            hashesPerRequest,
            maxOutstandingRequests,
            NoOpMetricsSystem.NO_OP_LABELLED_TIMER);

    // Create some peers that can respond
    List<RespondingEthPeer> usefulPeers =
        Stream.generate(
                () -> EthProtocolManagerTestUtil.createPeer(ethProtocolManager, header.getNumber()))
            .limit(peerCount)
            .collect(Collectors.toList());
    // And some irrelevant peers
    List<RespondingEthPeer> trailingPeers =
        Stream.generate(
                () ->
                    EthProtocolManagerTestUtil.createPeer(
                        ethProtocolManager, header.getNumber() - 1L))
            .limit(trailingPeerCount)
            .collect(Collectors.toList());

    // Start downloader
    CompletableFuture<?> result = downloader.run(header);

    // Respond to node data requests
    Responder responder =
        RespondingEthPeer.blockchainResponder(mock(Blockchain.class), remoteWorldStateArchive);
    while (!result.isDone()) {
      for (RespondingEthPeer peer : usefulPeers) {
        peer.respond(responder);
      }
    }

    // Check that trailing peers were not queried for data
    for (RespondingEthPeer trailingPeer : trailingPeers) {
      assertThat(trailingPeer.hasOutstandingRequests()).isFalse();
    }

    // Check that all expected account data was downloaded
    final WorldState localWorldState = localWorldStateArchive.get(stateRoot);
    assertThat(result).isDone();
    assertAccountsMatch(localWorldState, accounts);

    // We shouldn't have any extra data locally
    assertThat(localStorage.contains(otherHeader.getStateRoot())).isFalse();
    for (Account otherAccount : otherAccounts) {
      assertThat(localWorldState.get(otherAccount.getAddress())).isNull();
    }
  }

  private void assertAccountsMatch(
      final WorldState worldState, final List<Account> expectedAccounts) {
    for (Account expectedAccount : expectedAccounts) {
      Account actualAccount = worldState.get(expectedAccount.getAddress());
      assertThat(actualAccount).isNotNull();
      // Check each field
      assertThat(actualAccount.getNonce()).isEqualTo(expectedAccount.getNonce());
      assertThat(actualAccount.getCode()).isEqualTo(expectedAccount.getCode());
      assertThat(actualAccount.getBalance()).isEqualTo(expectedAccount.getBalance());

      Map<Bytes32, UInt256> actualStorage = actualAccount.storageEntriesFrom(Bytes32.ZERO, 500);
      Map<Bytes32, UInt256> expectedStorage = expectedAccount.storageEntriesFrom(Bytes32.ZERO, 500);
      assertThat(actualStorage).isEqualTo(expectedStorage);
    }
  }
}
