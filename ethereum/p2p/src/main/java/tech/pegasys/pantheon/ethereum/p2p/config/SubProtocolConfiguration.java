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
package tech.pegasys.pantheon.ethereum.p2p.config;

import tech.pegasys.pantheon.ethereum.p2p.api.ProtocolManager;
import tech.pegasys.pantheon.ethereum.p2p.wire.SubProtocol;

import java.util.ArrayList;
import java.util.List;

public class SubProtocolConfiguration {

  private final List<SubProtocol> subProtocols = new ArrayList<>();
  private final List<ProtocolManager> protocolManagers = new ArrayList<>();

  public SubProtocolConfiguration withSubProtocol(
      final SubProtocol subProtocol, final ProtocolManager protocolManager) {
    subProtocols.add(subProtocol);
    protocolManagers.add(protocolManager);
    return this;
  }

  public List<SubProtocol> getSubProtocols() {
    return subProtocols;
  }

  public List<ProtocolManager> getProtocolManagers() {
    return protocolManagers;
  }
}
