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
package tech.pegasys.pantheon.consensus.ibft;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class IbftConfigTest {

  @Test
  public void testDefaultsWhenJsonIsCompletelyEmpty() {

    final String jsonInput = "{}";

    final JsonObject jsonObject = new JsonObject(jsonInput);

    final IbftConfig ibftConfig = IbftConfig.create(jsonObject);

    assertThat(ibftConfig.getEpochLength()).isEqualTo(30_000);
    assertThat(ibftConfig.getBlockPeriod()).isEqualTo(1);
    assertThat(ibftConfig.getChainId()).isEqualTo(100);
    assertThat(ibftConfig.getHomesteadBlock().isPresent()).isEqualTo(false);
    assertThat(ibftConfig.getEip150Block().isPresent()).isEqualTo(false);
    assertThat(ibftConfig.getEip158Block().isPresent()).isEqualTo(false);
    assertThat(ibftConfig.getByzantiumBlock().isPresent()).isEqualTo(false);
  }

  @Test
  public void testDefaultsWhenIbftBlockIsEmpty() {

    final String jsonInput =
        "{"
            + "\"chainId\":20,"
            + "\"homesteadBlock\": 10,\n"
            + "\"eip150Block\": 20,\n"
            + "\"eip158Block\": 30,\n"
            + "\"byzantiumBlock\": 40,"
            + "\"ibft\":{}"
            + "}";

    final JsonObject jsonObject = new JsonObject(jsonInput);

    final IbftConfig ibftConfig = IbftConfig.create(jsonObject);

    assertThat(ibftConfig.getEpochLength()).isEqualTo(30_000);
    assertThat(ibftConfig.getBlockPeriod()).isEqualTo(1);
    assertThat(ibftConfig.getChainId()).isEqualTo(20);
    assertThat(ibftConfig.getHomesteadBlock().get()).isEqualTo(10);
    assertThat(ibftConfig.getEip150Block().get()).isEqualTo(20);
    assertThat(ibftConfig.getEip158Block().get()).isEqualTo(30);
    assertThat(ibftConfig.getByzantiumBlock().get()).isEqualTo(40);
  }

  @Test
  public void testNonDefaults() {
    final String jsonInput =
        "{"
            + "\"chainId\":20,"
            + "\"homesteadBlock\": 10,\n"
            + "\"eip150Block\": 20,\n"
            + "\"eip158Block\": 30,\n"
            + "\"byzantiumBlock\": 40,"
            + "\"ibft\":{"
            + "\"blockPeriodSeconds\" : 60,"
            + "\"epochLength\" : 1000"
            + "}"
            + "}";

    final JsonObject jsonObject = new JsonObject(jsonInput);

    final IbftConfig ibftConfig = IbftConfig.create(jsonObject);

    assertThat(ibftConfig.getEpochLength()).isEqualTo(1000);
    assertThat(ibftConfig.getBlockPeriod()).isEqualTo(60);
    assertThat(ibftConfig.getChainId()).isEqualTo(20);
    assertThat(ibftConfig.getHomesteadBlock().get()).isEqualTo(10);
    assertThat(ibftConfig.getEip150Block().get()).isEqualTo(20);
    assertThat(ibftConfig.getEip158Block().get()).isEqualTo(30);
    assertThat(ibftConfig.getByzantiumBlock().get()).isEqualTo(40);
  }
}
