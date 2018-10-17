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
package tech.pegasys.pantheon.ethereum.eth;

import tech.pegasys.pantheon.ethereum.eth.messages.EthPV62;
import tech.pegasys.pantheon.ethereum.eth.messages.EthPV63;
import tech.pegasys.pantheon.ethereum.p2p.wire.Capability;
import tech.pegasys.pantheon.ethereum.p2p.wire.SubProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EthProtocol implements SubProtocol {
  public static final String NAME = "eth";
  public static final Capability ETH62 = Capability.create(NAME, EthVersion.V62);
  public static final Capability ETH63 = Capability.create(NAME, EthVersion.V63);
  private static final EthProtocol INSTANCE = new EthProtocol();

  private static final List<Integer> eth62Messages =
      Arrays.asList(
          EthPV62.STATUS,
          EthPV62.NEW_BLOCK_HASHES,
          EthPV62.TRANSACTIONS,
          EthPV62.GET_BLOCK_HEADERS,
          EthPV62.BLOCK_HEADERS,
          EthPV62.GET_BLOCK_BODIES,
          EthPV62.BLOCK_BODIES,
          EthPV62.NEW_BLOCK);

  private static final List<Integer> eth63Messages = new ArrayList<>(eth62Messages);

  static {
    eth63Messages.addAll(
        Arrays.asList(
            EthPV63.GET_NODE_DATA, EthPV63.NODE_DATA, EthPV63.GET_RECEIPTS, EthPV63.RECEIPTS));
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int messageSpace(final int protocolVersion) {
    switch (protocolVersion) {
      case EthVersion.V62:
        return 8;
      case EthVersion.V63:
        return 17;
      default:
        return 0;
    }
  }

  @Override
  public boolean isValidMessageCode(final int protocolVersion, final int code) {
    switch (protocolVersion) {
      case EthVersion.V62:
        return eth62Messages.contains(code);
      case EthVersion.V63:
        return eth63Messages.contains(code);
      default:
        return false;
    }
  }

  public static EthProtocol get() {
    return INSTANCE;
  }

  public static class EthVersion {
    public static final int V62 = 62;
    public static final int V63 = 63;
  }
}
