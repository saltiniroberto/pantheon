package tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.SubscriptionManager;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.request.SubscriptionRequestMapper;

abstract class AbstractSubscriptionMethod implements JsonRpcMethod {

  private final SubscriptionManager subscriptionManager;
  private final SubscriptionRequestMapper mapper;

  AbstractSubscriptionMethod(
      final SubscriptionManager subscriptionManager, final SubscriptionRequestMapper mapper) {
    this.subscriptionManager = subscriptionManager;
    this.mapper = mapper;
  }

  SubscriptionManager subscriptionManager() {
    return subscriptionManager;
  }

  public SubscriptionRequestMapper getMapper() {
    return mapper;
  }
}
