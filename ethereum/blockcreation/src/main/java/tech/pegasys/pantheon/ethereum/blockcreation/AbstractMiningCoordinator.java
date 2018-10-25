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
package tech.pegasys.pantheon.ethereum.blockcreation;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.ethereum.chain.BlockAddedEvent;
import tech.pegasys.pantheon.ethereum.chain.BlockAddedObserver;
import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.chain.MinedBlockObserver;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.mainnet.EthHashSolution;
import tech.pegasys.pantheon.ethereum.mainnet.EthHashSolverInputs;
import tech.pegasys.pantheon.util.Subscribers;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import org.apache.logging.log4j.Logger;

public abstract class AbstractMiningCoordinator<
        C, M extends BlockMiner<C, ? extends AbstractBlockCreator<C>>>
    implements BlockAddedObserver {
  private static final Logger LOG = getLogger();
  protected boolean isEnabled = false;
  protected volatile Optional<M> currentRunningMiner = Optional.empty();

  private final Subscribers<MinedBlockObserver> minedBlockObservers = new Subscribers<>();
  private final AbstractMinerExecutor<C, M> executor;
  protected final Blockchain blockchain;
  private final SyncState syncState;

  public AbstractMiningCoordinator(
      final Blockchain blockchain,
      final AbstractMinerExecutor<C, M> executor,
      final SyncState syncState) {
    this.executor = executor;
    this.blockchain = blockchain;
    this.syncState = syncState;
    this.blockchain.observeBlockAdded(this);
    syncState.addInSyncListener(this::inSyncChanged);
  }

  public void enable() {
    synchronized (this) {
      if (isEnabled) {
        return;
      }
      if (syncState.isInSync()) {
        startAsyncMiningOperation();
      }
      isEnabled = true;
    }
  }

  public void disable() {
    synchronized (this) {
      if (!isEnabled) {
        return;
      }
      haltCurrentMiningOperation();
      isEnabled = false;
    }
  }

  public boolean isRunning() {
    synchronized (this) {
      return currentRunningMiner.isPresent();
    }
  }

  protected void startAsyncMiningOperation() {
    final BlockHeader parentHeader = blockchain.getChainHeadHeader();
    currentRunningMiner = Optional.of(executor.startAsyncMining(minedBlockObservers, parentHeader));
  }

  protected void haltCurrentMiningOperation() {
    currentRunningMiner.ifPresent(M::cancel);
    currentRunningMiner = Optional.empty();
  }

  @Override
  public void onBlockAdded(final BlockAddedEvent event, final Blockchain blockchain) {
    synchronized (this) {
      if (isEnabled && event.isNewCanonicalHead()) {
        haltCurrentMiningOperation();
        if (syncState.isInSync()) {
          startAsyncMiningOperation();
        }
      }
    }
  }

  public void inSyncChanged(final boolean inSync) {
    synchronized (this) {
      if (isEnabled && inSync) {
        LOG.info("Resuming mining operations");
        startAsyncMiningOperation();
      } else if (!inSync) {
        LOG.info("Pausing mining while behind chain head");
        haltCurrentMiningOperation();
      }
    }
  }

  public void addMinedBlockObserver(final MinedBlockObserver obs) {
    minedBlockObservers.subscribe(obs);
  }

  // Required for JSON RPC, and are deemed to be valid for all mining mechanisms
  public void setMinTransactionGasPrice(final Wei minGasPrice) {
    executor.setMinTransactionGasPrice(minGasPrice);
  }

  public Wei getMinTransactionGasPrice() {
    return executor.getMinTransactionGasPrice();
  }

  public void setExtraData(final BytesValue extraData) {
    executor.setExtraData(extraData);
  }

  public void setCoinbase(final Address coinbase) {
    throw new UnsupportedOperationException(
        "Current consensus mechanism prevents setting coinbase.");
  }

  public Optional<Address> getCoinbase() {
    throw new UnsupportedOperationException(
        "Current consensus mechanism prevents querying of coinbase.");
  }

  public Optional<Long> hashesPerSecond() {
    throw new UnsupportedOperationException(
        "Current consensus mechanism prevents querying of hashrate.");
  }

  public Optional<EthHashSolverInputs> getWorkDefinition() {
    throw new UnsupportedOperationException(
        "Current consensus mechanism prevents querying work definition.");
  }

  public boolean submitWork(final EthHashSolution solution) {
    throw new UnsupportedOperationException(
        "Current consensus mechanism prevents submission of work solutions.");
  }
}
