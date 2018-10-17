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

import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

public class TransactionTestFixture {

  private long nonce = 0;

  private Wei gasPrice = Wei.of(5);

  private long gasLimit = 5000;

  private Optional<Address> to = Optional.empty();
  private Address sender = Address.fromHexString(String.format("%020x", 1));

  private Wei value = Wei.of(4);

  private BytesValue payload = BytesValue.EMPTY;

  private int chainId = 2018;

  public Transaction createTransaction(final KeyPair keys) {
    final Transaction.Builder builder = Transaction.builder();
    builder
        .gasLimit(gasLimit)
        .gasPrice(gasPrice)
        .nonce(nonce)
        .payload(payload)
        .value(value)
        .sender(sender)
        .chainId(chainId);

    to.ifPresent(builder::to);

    return builder.signAndBuild(keys);
  }

  public TransactionTestFixture nonce(final long nonce) {
    this.nonce = nonce;
    return this;
  }

  public TransactionTestFixture gasPrice(final Wei gasPrice) {
    this.gasPrice = gasPrice;
    return this;
  }

  public TransactionTestFixture gasLimit(final long gasLimit) {
    this.gasLimit = gasLimit;
    return this;
  }

  public TransactionTestFixture to(final Optional<Address> to) {
    this.to = to;
    return this;
  }

  public TransactionTestFixture sender(final Address sender) {
    this.sender = sender;
    return this;
  }

  public TransactionTestFixture value(final Wei value) {
    this.value = value;
    return this;
  }

  public TransactionTestFixture payload(final BytesValue payload) {
    this.payload = payload;
    return this;
  }

  public TransactionTestFixture chainId(final int chainId) {
    this.chainId = chainId;
    return this;
  }
}
