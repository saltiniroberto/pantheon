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

import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetProtocolSpecs;
import tech.pegasys.pantheon.ethereum.mainnet.MutableProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSpec;

import org.junit.Test;

public class IbftProtocolSpecsTest {
  IbftProtocolSpecs protocolSpecs =
      new IbftProtocolSpecs(15, 30_000, 5, new MutableProtocolSchedule<>());

  @Test
  public void homsteadParametersAlignWithMainnetWithAdjustments() {
    final ProtocolSpec<IbftContext> homestead = protocolSpecs.homestead();

    assertThat(homestead.getName()).isEqualTo("Homestead");
    assertThat(homestead.getBlockReward()).isEqualTo(Wei.ZERO);
    assertThat(homestead.getDifficultyCalculator().nextDifficulty(0, null, null)).isEqualTo(1);
    // TODO: Must add tests for getBlockHeaderValidator(), getOmmerHeaderValidator() (already
    // doable) and getBlockImporter (not yet doable)

  }

  @Test
  public void allSpecsInheritFromMainnetCounterparts() {
    final ProtocolSchedule<Void> mainnetProtocolSchedule = new MutableProtocolSchedule<>();

    assertThat(protocolSpecs.frontier().getName())
        .isEqualTo(MainnetProtocolSpecs.frontier(mainnetProtocolSchedule).getName());
    assertThat(protocolSpecs.homestead().getName())
        .isEqualTo(MainnetProtocolSpecs.homestead(mainnetProtocolSchedule).getName());
    assertThat(protocolSpecs.tangerineWhistle().getName())
        .isEqualTo(MainnetProtocolSpecs.tangerineWhistle(mainnetProtocolSchedule).getName());
    assertThat(protocolSpecs.spuriousDragon().getName())
        .isEqualTo(MainnetProtocolSpecs.spuriousDragon(1, mainnetProtocolSchedule).getName());
    assertThat(protocolSpecs.byzantium().getName())
        .isEqualTo(MainnetProtocolSpecs.byzantium(1, mainnetProtocolSchedule).getName());
  }
}
