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

import java.util.Optional;

import io.vertx.core.json.JsonObject;

public class IbftConfig {
  private static final long DEFAULT_EPOCH_LENGTH = 30_000;
  private static final int DEFAULT_BLOCK_PERIOD_SECONDS = 1;
  // TODO: Set DEFAULT_CHAIN_ID to an appropriate value
  private static final int DEFAULT_CHAIN_ID = 100;

  private final int chainId;
  private final long epochLength;
  private final long blockPeriod;
  private final Optional<Long> homesteadBlock;
  private final Optional<Long> eip150Block;
  private final Optional<Long> eip158Block;
  private final Optional<Long> byzantiumBlock;

  public IbftConfig(
      int chainId,
      long epochLength,
      long blockPeriod,
      Optional<Long> homesteadBlock,
      Optional<Long> eip150Block,
      Optional<Long> eip158Block,
      Optional<Long> byzantiumBlock) {
    this.chainId = chainId;
    this.epochLength = epochLength;
    this.blockPeriod = blockPeriod;
    this.homesteadBlock = homesteadBlock;
    this.eip150Block = eip150Block;
    this.eip158Block = eip158Block;
    this.byzantiumBlock = byzantiumBlock;
  }

  public static IbftConfig create(final JsonObject config) {
    final int chainId = config.getInteger("chainId", DEFAULT_CHAIN_ID);

    Optional<Long> homesteadBlock = Optional.ofNullable(config.getLong("homesteadBlock"));
    Optional<Long> eip150Block = Optional.ofNullable(config.getLong("eip150Block"));
    Optional<Long> eip158Block = Optional.ofNullable(config.getLong("eip158Block"));
    Optional<Long> byzantiumBlock = Optional.ofNullable(config.getLong("byzantiumBlock"));

    final Optional<JsonObject> ibftConfig = Optional.ofNullable(config.getJsonObject("ibft"));
    final long epochLength = getEpochLength(ibftConfig);
    final long blockPeriod =
        ibftConfig
            .map(iC -> iC.getInteger("blockPeriodSeconds"))
            .orElse(DEFAULT_BLOCK_PERIOD_SECONDS);

    return new IbftConfig(
        chainId,
        epochLength,
        blockPeriod,
        homesteadBlock,
        eip150Block,
        eip158Block,
        byzantiumBlock);
  }

  private static long getEpochLength(final Optional<JsonObject> ibftConfig) {
    return ibftConfig.map(conf -> conf.getLong("epochLength")).orElse(DEFAULT_EPOCH_LENGTH);
  }

  public int getChainId() {
    return chainId;
  }

  public long getEpochLength() {
    return epochLength;
  }

  public long getBlockPeriod() {
    return blockPeriod;
  }

  public Optional<Long> getHomesteadBlock() {
    return homesteadBlock;
  }

  public Optional<Long> getEip150Block() {
    return eip150Block;
  }

  public Optional<Long> getEip158Block() {
    return eip158Block;
  }

  public Optional<Long> getByzantiumBlock() {
    return byzantiumBlock;
  }
}
