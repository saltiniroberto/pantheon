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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum JsonRpcError {
  // Standard errors
  PARSE_ERROR(-32700, "Parse error"),
  INVALID_REQUEST(-32600, "Invalid Request"),
  METHOD_NOT_FOUND(-32601, "Method not found"),
  INVALID_PARAMS(-32602, "Invalid params"),
  INTERNAL_ERROR(-32603, "Internal error"),

  // Filter & Subscription Errors
  FILTER_NOT_FOUND(-32000, "Filter not found"),
  SUBSCRIPTION_NOT_FOUND(-32000, "Subscription not found"),
  NO_MINING_WORK_FOUND(-32000, "No mining work available yet"),

  // Transaction validation failures
  NONCE_TOO_LOW(-32001, "Nonce too low"),
  INVALID_TRANSACTION_SIGNATURE(-32002, "Invalid signature"),
  INTRINSIC_GAS_EXCEEDS_LIMIT(-32003, "Intrinsic gas exceeds gas limit"),
  TRANSACTION_UPFRONT_COST_EXCEEDS_BALANCE(-32004, "Upfront cost exceeds account balance"),
  EXCEEDS_BLOCK_GAS_LIMIT(-32005, "Transaction gas limit exceeds block gas limit"),
  INCORRECT_NONCE(-32006, "Incorrect nonce"),

  // Miner failures
  COINBASE_NOT_SET(-32010, "Coinbase not set. Unable to start mining without a coinbase."),

  // Wallet errors
  COINBASE_NOT_SPECIFIED(-32000, "Coinbase must be explicitly specified");

  private final int code;
  private final String message;

  JsonRpcError(final int code, final String message) {
    this.code = code;
    this.message = message;
  }

  @JsonGetter("code")
  public int getCode() {
    return code;
  }

  @JsonGetter("message")
  public String getMessage() {
    return message;
  }

  @JsonCreator
  public static JsonRpcError fromJson(
      @JsonProperty("code") final int code, @JsonProperty("message") final String message) {
    for (final JsonRpcError error : JsonRpcError.values()) {
      if (error.getCode() == code) {
        return error;
      }
    }
    return null;
  }
}
