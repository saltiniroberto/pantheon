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
package tech.pegasys.pantheon.ethereum.eth.sync.tasks;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.AbstractEthTask;
import tech.pegasys.pantheon.ethereum.eth.manager.AbstractPeerTask;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.util.BlockchainUtil;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetermineCommonAncestorTask<C> extends AbstractEthTask<BlockHeader> {
  private static final Logger LOG = LogManager.getLogger();
  private final EthContext ethContext;
  private final ProtocolSchedule<C> protocolSchedule;
  private final ProtocolContext<C> protocolContext;
  private final EthPeer peer;
  private final int headerRequestSize;

  private long maximumPossibleCommonAncestorNumber;
  private long minimumPossibleCommonAncestorNumber;
  private BlockHeader commonAncestorCandidate;
  private boolean initialQuery = true;

  private DetermineCommonAncestorTask(
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final EthPeer peer,
      final int headerRequestSize) {
    this.protocolSchedule = protocolSchedule;
    this.ethContext = ethContext;
    this.protocolContext = protocolContext;
    this.peer = peer;
    this.headerRequestSize = headerRequestSize;

    maximumPossibleCommonAncestorNumber = protocolContext.getBlockchain().getChainHeadBlockNumber();
    minimumPossibleCommonAncestorNumber = BlockHeader.GENESIS_BLOCK_NUMBER;
    commonAncestorCandidate =
        protocolContext.getBlockchain().getBlockHeader(BlockHeader.GENESIS_BLOCK_NUMBER).get();
  }

  public static <C> DetermineCommonAncestorTask<C> create(
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final EthPeer peer,
      final int headerRequestSize) {
    return new DetermineCommonAncestorTask<>(
        protocolSchedule, protocolContext, ethContext, peer, headerRequestSize);
  }

  @Override
  protected void executeTask() {
    if (maximumPossibleCommonAncestorNumber == minimumPossibleCommonAncestorNumber) {
      // Bingo, we found our common ancestor.
      result.get().complete(commonAncestorCandidate);
      return;
    }
    if (maximumPossibleCommonAncestorNumber < BlockHeader.GENESIS_BLOCK_NUMBER
        && !result.get().isDone()) {
      result.get().completeExceptionally(new IllegalStateException("No common ancestor."));
      return;
    }
    requestHeaders()
        .thenCompose(this::processHeaders)
        .whenComplete(
            (peerResult, error) -> {
              if (error != null) {
                result.get().completeExceptionally(error);
              } else if (!result.get().isDone()) {
                executeTask();
              }
            });
  }

  @VisibleForTesting
  CompletableFuture<AbstractPeerTask.PeerTaskResult<List<BlockHeader>>> requestHeaders() {
    final long range = maximumPossibleCommonAncestorNumber - minimumPossibleCommonAncestorNumber;
    final int skipInterval = initialQuery ? 0 : calculateSkipInterval(range, headerRequestSize);
    final int count =
        initialQuery ? headerRequestSize : calculateCount((double) range, skipInterval);
    LOG.debug(
        "Searching for common ancestor with {} between {} and {}",
        peer,
        minimumPossibleCommonAncestorNumber,
        maximumPossibleCommonAncestorNumber);
    return executeSubTask(
        () ->
            GetHeadersFromPeerByNumberTask.endingAtNumber(
                    protocolSchedule,
                    ethContext,
                    maximumPossibleCommonAncestorNumber,
                    count,
                    skipInterval)
                .assignPeer(peer)
                .run());
  }

  /**
   * In the case where the remote chain contains 100 blocks, the initial count work out to 11, and
   * the skip interval would be 9. This would yield the headers (0, 10, 20, 30, 40, 50, 60, 70, 80,
   * 90, 100).
   */
  @VisibleForTesting
  static int calculateSkipInterval(final long range, final int headerRequestSize) {
    return Math.max(0, Math.toIntExact(range / (headerRequestSize - 1) - 1) - 1);
  }

  @VisibleForTesting
  static int calculateCount(final double range, final int skipInterval) {
    return Math.toIntExact((long) Math.ceil(range / (skipInterval + 1)) + 1);
  }

  private CompletableFuture<Void> processHeaders(
      final AbstractPeerTask.PeerTaskResult<List<BlockHeader>> headersResult) {
    initialQuery = false;
    final List<BlockHeader> headers = headersResult.getResult();

    final OptionalInt maybeAncestorNumber =
        BlockchainUtil.findHighestKnownBlockIndex(protocolContext.getBlockchain(), headers, false);

    // Means the insertion point is in the next header request.
    if (!maybeAncestorNumber.isPresent()) {
      maximumPossibleCommonAncestorNumber = headers.get(headers.size() - 1).getNumber() - 1L;
      return CompletableFuture.completedFuture(null);
    }
    final int ancestorNumber = maybeAncestorNumber.getAsInt();
    commonAncestorCandidate = headers.get(ancestorNumber);

    if (ancestorNumber - 1 >= 0) {
      maximumPossibleCommonAncestorNumber = headers.get(ancestorNumber - 1).getNumber() - 1L;
    }
    minimumPossibleCommonAncestorNumber = headers.get(ancestorNumber).getNumber();

    return CompletableFuture.completedFuture(null);
  }
}
