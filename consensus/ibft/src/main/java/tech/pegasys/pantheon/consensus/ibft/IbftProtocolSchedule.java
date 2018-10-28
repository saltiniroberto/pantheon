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

import tech.pegasys.pantheon.ethereum.mainnet.MutableProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;

import io.vertx.core.json.JsonObject;

/** Defines the protocol behaviours for a blockchain using IBFT. */
public class IbftProtocolSchedule {
  public static ProtocolSchedule<IbftContext> create(final JsonObject config) {
    final IbftConfig ibftConfig = IbftConfig.create(config);

    final MutableProtocolSchedule<IbftContext> protocolSchedule = new MutableProtocolSchedule<>();

    final IbftProtocolSpecs specs =
        new IbftProtocolSpecs(
            ibftConfig.getBlockPeriod(),
            ibftConfig.getEpochLength(),
            ibftConfig.getChainId(),
            protocolSchedule);

    protocolSchedule.putMilestone(0, specs.frontier());

    ibftConfig
        .getHomesteadBlock()
        .ifPresent(blockNumber -> protocolSchedule.putMilestone(blockNumber, specs.homestead()));

    ibftConfig
        .getEip150Block()
        .ifPresent(
            blockNumber -> protocolSchedule.putMilestone(blockNumber, specs.tangerineWhistle()));

    ibftConfig
        .getEip158Block()
        .ifPresent(
            blockNumber -> protocolSchedule.putMilestone(blockNumber, specs.spuriousDragon()));

    ibftConfig
        .getByzantiumBlock()
        .ifPresent(blockNumber -> protocolSchedule.putMilestone(blockNumber, specs.byzantium()));

    return protocolSchedule;
  }
}
