package tech.pegasys.pantheon.ethereum.p2p.api;

import tech.pegasys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;

@FunctionalInterface
public interface DisconnectCallback {
  void onDisconnect(PeerConnection connection, DisconnectReason reason, boolean initiatedByPeer);
}
