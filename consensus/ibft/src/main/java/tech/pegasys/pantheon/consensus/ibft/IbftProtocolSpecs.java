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

import static tech.pegasys.pantheon.consensus.ibft.IbftBlockHeaderValidationRulesetFactory.ibftBlockHeaderValidator;

import tech.pegasys.pantheon.consensus.common.EpochManager;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockBodyValidator;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockImporter;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetProtocolSpecs;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpecBuilder;

import java.math.BigInteger;

/** Factory for producing Ibft protocol specs for given configurations and known fork points */
public class IbftProtocolSpecs {

  private final long secondsBetweenBlocks;
  private final long epochLength;
  private final int chainId;
  private final ProtocolSchedule<IbftContext> protocolSchedule;

  public IbftProtocolSpecs(
      final long secondsBetweenBlocks,
      final long epochLength,
      final int chainId,
      final ProtocolSchedule<IbftContext> protocolSchedule) {
    this.secondsBetweenBlocks = secondsBetweenBlocks;
    this.epochLength = epochLength;
    this.chainId = chainId;
    this.protocolSchedule = protocolSchedule;
  }

  public ProtocolSpec<IbftContext> frontier() {
    return applyIbftSpecificModifications(MainnetProtocolSpecs.frontierDefinition());
  }

  public ProtocolSpec<IbftContext> homestead() {
    return applyIbftSpecificModifications(MainnetProtocolSpecs.homesteadDefinition());
  }

  public ProtocolSpec<IbftContext> tangerineWhistle() {
    return applyIbftSpecificModifications(MainnetProtocolSpecs.tangerineWhistleDefinition());
  }

  public ProtocolSpec<IbftContext> spuriousDragon() {
    return applyIbftSpecificModifications(MainnetProtocolSpecs.spuriousDragonDefinition(chainId));
  }

  public ProtocolSpec<IbftContext> byzantium() {
    return applyIbftSpecificModifications(MainnetProtocolSpecs.byzantiumDefinition(chainId));
  }

  private ProtocolSpec<IbftContext> applyIbftSpecificModifications(
      final ProtocolSpecBuilder<Void> specBuilder) {
    final EpochManager epochManager = new EpochManager(epochLength);
    return specBuilder
        .<IbftContext>changeConsensusContextType(
            difficultyCalculator -> ibftBlockHeaderValidator(secondsBetweenBlocks),
            difficultyCalculator -> ibftBlockHeaderValidator(secondsBetweenBlocks),
            MainnetBlockBodyValidator::new,
            (blockHeaderValidator, blockBodyValidator, blockProcessor) ->
                new IbftBlockImporter(
                    new MainnetBlockImporter<>(
                        blockHeaderValidator, blockBodyValidator, blockProcessor),
                    new IbftVoteTallyUpdater(epochManager)),
            (time, parent, protocolContext) -> BigInteger.ONE)
        .blockReward(Wei.ZERO)
        .blockHashFunction(IbftBlockHashing::calculateHashOfIbftBlockOnChain)
        .build(protocolSchedule);
  }
}
