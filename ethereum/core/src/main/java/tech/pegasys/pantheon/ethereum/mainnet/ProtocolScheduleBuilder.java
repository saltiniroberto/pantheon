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
package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.config.GenesisConfigOptions;

import java.util.OptionalLong;
import java.util.function.Function;

public class ProtocolScheduleBuilder<C> {

  private final GenesisConfigOptions config;
  private final Function<ProtocolSpecBuilder<Void>, ProtocolSpecBuilder<C>> protocolSpecAdapter;
  private final int defaultChainId;

  public ProtocolScheduleBuilder(
      final GenesisConfigOptions config,
      final int defaultChainId,
      final Function<ProtocolSpecBuilder<Void>, ProtocolSpecBuilder<C>> protocolSpecAdapter) {
    this.config = config;
    this.protocolSpecAdapter = protocolSpecAdapter;
    this.defaultChainId = defaultChainId;
  }

  public ProtocolSchedule<C> createProtocolSchedule() {
    final int chainId = config.getChainId().orElse(defaultChainId);
    final MutableProtocolSchedule<C> protocolSchedule = new MutableProtocolSchedule<>(chainId);

    addProtocolSpec(
        protocolSchedule, OptionalLong.of(0), MainnetProtocolSpecs.frontierDefinition());
    addProtocolSpec(
        protocolSchedule,
        config.getHomesteadBlockNumber(),
        MainnetProtocolSpecs.homesteadDefinition());

    config
        .getDaoForkBlock()
        .ifPresent(
            daoBlockNumber -> {
              if (daoBlockNumber > 0) {
                final ProtocolSpec<C> originalProtocolSpec =
                    protocolSchedule.getByBlockNumber(daoBlockNumber);
                addProtocolSpec(
                    protocolSchedule,
                    OptionalLong.of(daoBlockNumber),
                    MainnetProtocolSpecs.daoRecoveryInitDefinition());
                addProtocolSpec(
                    protocolSchedule,
                    OptionalLong.of(daoBlockNumber + 1),
                    MainnetProtocolSpecs.daoRecoveryTransitionDefinition());

                // Return to the previous protocol spec after the dao fork has completed.
                protocolSchedule.putMilestone(daoBlockNumber + 10, originalProtocolSpec);
              }
            });

    addProtocolSpec(
        protocolSchedule,
        config.getTangerineWhistleBlockNumber(),
        MainnetProtocolSpecs.tangerineWhistleDefinition());
    addProtocolSpec(
        protocolSchedule,
        config.getSpuriousDragonBlockNumber(),
        MainnetProtocolSpecs.spuriousDragonDefinition(chainId));
    addProtocolSpec(
        protocolSchedule,
        config.getByzantiumBlockNumber(),
        MainnetProtocolSpecs.byzantiumDefinition(chainId));
    addProtocolSpec(
        protocolSchedule,
        config.getConstantinopleBlockNumber(),
        MainnetProtocolSpecs.constantinopleDefinition(chainId));

    return protocolSchedule;
  }

  private void addProtocolSpec(
      final MutableProtocolSchedule<C> protocolSchedule,
      final OptionalLong blockNumber,
      final ProtocolSpecBuilder<Void> definition) {
    blockNumber.ifPresent(
        number ->
            protocolSchedule.putMilestone(
                number, protocolSpecAdapter.apply(definition).build(protocolSchedule)));
  }
}
