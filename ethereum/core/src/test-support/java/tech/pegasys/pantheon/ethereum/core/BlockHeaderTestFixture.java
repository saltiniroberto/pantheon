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
package tech.pegasys.pantheon.ethereum.core;

import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

public class BlockHeaderTestFixture {

  private Hash parentHash = Hash.EMPTY;
  private Hash ommersHash = Hash.EMPTY_LIST_HASH;
  private Address coinbase = Address.ECREC;

  private Hash stateRoot = Hash.EMPTY_TRIE_HASH;
  private Hash transactionsRoot = Hash.EMPTY;
  private Hash receiptsRoot = Hash.EMPTY;

  private LogsBloomFilter logsBloom = LogsBloomFilter.empty();
  private UInt256 difficulty = UInt256.ZERO;
  private long number = 0;

  private long gasLimit = 0;
  private long gasUsed = 0;
  private long timestamp = 0;
  private BytesValue extraData = BytesValue.EMPTY;

  private Hash mixHash = Hash.EMPTY;
  private long nonce = 0;

  public BlockHeader buildHeader() {
    final BlockHeaderBuilder builder = BlockHeaderBuilder.create();
    builder.parentHash(parentHash);
    builder.ommersHash(ommersHash);
    builder.coinbase(coinbase);
    builder.stateRoot(stateRoot);
    builder.transactionsRoot(transactionsRoot);
    builder.receiptsRoot(receiptsRoot);
    builder.logsBloom(logsBloom);
    builder.difficulty(difficulty);
    builder.number(number);
    builder.gasLimit(gasLimit);
    builder.gasUsed(gasUsed);
    builder.timestamp(timestamp);
    builder.extraData(extraData);
    builder.mixHash(mixHash);
    builder.nonce(nonce);
    builder.blockHashFunction(MainnetBlockHashFunction::createHash);

    return builder.buildBlockHeader();
  }

  public BlockHeaderTestFixture parentHash(final Hash parentHash) {
    this.parentHash = parentHash;
    return this;
  }

  public BlockHeaderTestFixture ommersHash(final Hash ommersHash) {
    this.ommersHash = ommersHash;
    return this;
  }

  public BlockHeaderTestFixture coinbase(final Address coinbase) {
    this.coinbase = coinbase;
    return this;
  }

  public BlockHeaderTestFixture stateRoot(final Hash stateRoot) {
    this.stateRoot = stateRoot;
    return this;
  }

  public BlockHeaderTestFixture transactionsRoot(final Hash transactionsRoot) {
    this.transactionsRoot = transactionsRoot;
    return this;
  }

  public BlockHeaderTestFixture receiptsRoot(final Hash receiptsRoot) {
    this.receiptsRoot = receiptsRoot;
    return this;
  }

  public BlockHeaderTestFixture logsBloom(final LogsBloomFilter logsBloom) {
    this.logsBloom = logsBloom;
    return this;
  }

  public BlockHeaderTestFixture difficulty(final UInt256 difficulty) {
    this.difficulty = difficulty;
    return this;
  }

  public BlockHeaderTestFixture number(final long number) {
    this.number = number;
    return this;
  }

  public BlockHeaderTestFixture gasLimit(final long gasLimit) {
    this.gasLimit = gasLimit;
    return this;
  }

  public BlockHeaderTestFixture gasUsed(final long gasUsed) {
    this.gasUsed = gasUsed;
    return this;
  }

  public BlockHeaderTestFixture timestamp(final long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public BlockHeaderTestFixture extraData(final BytesValue extraData) {
    this.extraData = extraData;
    return this;
  }

  public BlockHeaderTestFixture mixHash(final Hash mixHash) {
    this.mixHash = mixHash;
    return this;
  }

  public BlockHeaderTestFixture nonce(final long nonce) {
    this.nonce = nonce;
    return this;
  }
}
