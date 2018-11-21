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
package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.jsonrpc.Net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cluster implements AutoCloseable {

  private final Map<String, RunnableNode> nodes = new HashMap<>();
  private final PantheonNodeRunner pantheonNodeRunner = PantheonNodeRunner.instance();
  private final Net net;

  public Cluster(final Net net) {
    this.net = net;
  }

  public void start(final Node... nodes) {
    start(
        Arrays.stream(nodes)
            .map(
                n -> {
                  assertThat(n instanceof RunnableNode).isTrue();
                  return (RunnableNode) n;
                })
            .collect(Collectors.toList()));
  }

  public void start(final List<RunnableNode> nodes) {
    this.nodes.clear();

    final List<String> bootNodes = new ArrayList<>();
    for (final RunnableNode node : nodes) {
      this.nodes.put(node.getName(), node);
      bootNodes.add(node.getConfiguration().enodeUrl());
    }

    for (final RunnableNode node : nodes) {
      node.getConfiguration().bootnodes(bootNodes);
      node.start(pantheonNodeRunner);
    }

    for (final RunnableNode node : nodes) {
      node.awaitPeerDiscovery(net.awaitPeerCount(nodes.size() - 1));
    }
  }

  public void stop() {
    for (final RunnableNode node : nodes.values()) {
      node.stop();
    }
    pantheonNodeRunner.shutdown();
  }

  @Override
  public void close() {
    for (final RunnableNode node : nodes.values()) {
      node.close();
    }
    pantheonNodeRunner.shutdown();
  }

  public void verify(final Condition expected) {
    for (final Node node : nodes.values()) {
      expected.verify(node);
    }
  }
}
