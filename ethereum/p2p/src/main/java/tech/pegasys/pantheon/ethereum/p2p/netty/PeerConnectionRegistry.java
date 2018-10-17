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
package tech.pegasys.pantheon.ethereum.p2p.netty;

import static java.util.Collections.unmodifiableCollection;

import tech.pegasys.pantheon.ethereum.p2p.api.DisconnectCallback;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;
import tech.pegasys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PeerConnectionRegistry implements DisconnectCallback {

  private final ConcurrentMap<BytesValue, PeerConnection> connections = new ConcurrentHashMap<>();

  public void registerConnection(final PeerConnection connection) {
    connections.put(connection.getPeer().getNodeId(), connection);
  }

  public Collection<PeerConnection> getPeerConnections() {
    return unmodifiableCollection(connections.values());
  }

  public int size() {
    return connections.size();
  }

  public boolean isAlreadyConnected(final BytesValue nodeId) {
    return connections.containsKey(nodeId);
  }

  @Override
  public void onDisconnect(
      final PeerConnection connection,
      final DisconnectReason reason,
      final boolean initiatedByPeer) {
    connections.remove(connection.getPeer().getNodeId());
  }
}
