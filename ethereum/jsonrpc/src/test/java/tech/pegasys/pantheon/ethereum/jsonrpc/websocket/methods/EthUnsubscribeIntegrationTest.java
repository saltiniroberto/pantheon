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
package tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.WebSocketRequestHandler;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.SubscriptionManager;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.request.SubscribeRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.request.SubscriptionType;

import java.util.HashMap;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class EthUnsubscribeIntegrationTest {

  private Vertx vertx;
  private WebSocketRequestHandler webSocketRequestHandler;
  private SubscriptionManager subscriptionManager;
  private WebSocketMethodsFactory webSocketMethodsFactory;
  private final int ASYNC_TIMEOUT = 5000;
  private final String CONNECTION_ID = "test-connection-id-1";

  @Before
  public void before() {
    vertx = Vertx.vertx();
    subscriptionManager = new SubscriptionManager();
    webSocketMethodsFactory = new WebSocketMethodsFactory(subscriptionManager, new HashMap<>());
    webSocketRequestHandler = new WebSocketRequestHandler(vertx, webSocketMethodsFactory.methods());
  }

  @Test
  public void shouldRemoveConnectionWithSingleSubscriptionFromMap(final TestContext context) {
    final Async async = context.async();

    // Check the connectionMap is empty
    assertThat(subscriptionManager.getConnectionSubscriptionsMap().size()).isEqualTo(0);

    // Add the subscription we'd like to remove
    final SubscribeRequest subscribeRequest =
        new SubscribeRequest(SubscriptionType.SYNCING, null, null, CONNECTION_ID);
    final Long subscriptionId = subscriptionManager.subscribe(subscribeRequest);
    assertThat(subscriptionManager.getConnectionSubscriptionsMap().size()).isEqualTo(1);

    final JsonRpcRequest unsubscribeRequest =
        createEthUnsubscribeRequest(subscriptionId, CONNECTION_ID);

    vertx
        .eventBus()
        .consumer(CONNECTION_ID)
        .handler(
            msg -> {
              assertThat(subscriptionManager.getConnectionSubscriptionsMap().isEmpty()).isTrue();
              async.complete();
            })
        .completionHandler(
            v ->
                webSocketRequestHandler.handle(
                    CONNECTION_ID, Buffer.buffer(Json.encode(unsubscribeRequest))));

    async.awaitSuccess(ASYNC_TIMEOUT);
  }

  @Test
  public void shouldRemoveSubscriptionAndKeepConnection(final TestContext context) {
    final Async async = context.async();

    // Check the connectionMap is empty
    assertThat(subscriptionManager.getConnectionSubscriptionsMap().size()).isEqualTo(0);

    // Add the subscriptions we'd like to remove
    final SubscribeRequest subscribeRequest =
        new SubscribeRequest(SubscriptionType.SYNCING, null, null, CONNECTION_ID);
    final Long subscriptionId1 = subscriptionManager.subscribe(subscribeRequest);
    final Long subscriptionId2 = subscriptionManager.subscribe(subscribeRequest);

    assertThat(subscriptionManager.getConnectionSubscriptionsMap().size()).isEqualTo(1);
    assertThat(subscriptionManager.getConnectionSubscriptionsMap().containsKey(CONNECTION_ID))
        .isTrue();
    assertThat(subscriptionManager.getConnectionSubscriptionsMap().get(CONNECTION_ID).size())
        .isEqualTo(2);

    final JsonRpcRequest unsubscribeRequest =
        createEthUnsubscribeRequest(subscriptionId2, CONNECTION_ID);

    vertx
        .eventBus()
        .consumer(CONNECTION_ID)
        .handler(
            msg -> {
              assertThat(subscriptionManager.getConnectionSubscriptionsMap().size()).isEqualTo(1);
              assertThat(
                      subscriptionManager
                          .getConnectionSubscriptionsMap()
                          .containsKey(CONNECTION_ID))
                  .isTrue();
              assertThat(
                      subscriptionManager.getConnectionSubscriptionsMap().get(CONNECTION_ID).size())
                  .isEqualTo(1);
              assertThat(
                      subscriptionManager.getConnectionSubscriptionsMap().get(CONNECTION_ID).get(0))
                  .isEqualTo(subscriptionId1);
              async.complete();
            })
        .completionHandler(
            v ->
                webSocketRequestHandler.handle(
                    CONNECTION_ID, Buffer.buffer(Json.encode(unsubscribeRequest))));

    async.awaitSuccess(ASYNC_TIMEOUT);
  }

  private WebSocketRpcRequest createEthUnsubscribeRequest(
      final Long subscriptionId, final String connectionId) {
    return Json.decodeValue(
        "{\"id\": 1, \"method\": \"eth_unsubscribe\", \"params\": [\""
            + subscriptionId
            + "\"], \"connectionId\": \""
            + connectionId
            + "\"}",
        WebSocketRpcRequest.class);
  }
}
