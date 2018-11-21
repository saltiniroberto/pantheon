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
package tech.pegasys.pantheon.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.pegasys.pantheon.controller.KeyPairUtil.loadKeyPair;
import static tech.pegasys.pantheon.controller.PantheonController.DATABASE_PATH;

import tech.pegasys.pantheon.config.GenesisConfigFile;
import tech.pegasys.pantheon.controller.MainnetPantheonController;
import tech.pegasys.pantheon.controller.PantheonController;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.MiningParameters;
import tech.pegasys.pantheon.ethereum.development.DevelopmentProtocolSchedule;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.storage.StorageProvider;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.RocksDbStorageProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.google.common.io.Resources;

public class PantheonControllerBuilder {

  public PantheonController<?> build(
      final SynchronizerConfiguration synchronizerConfiguration,
      final Path homePath,
      final EthNetworkConfig ethNetworkConfig,
      final boolean syncWithOttoman,
      final MiningParameters miningParameters,
      final boolean isDevMode,
      final File nodePrivateKeyFile)
      throws IOException {
    // instantiate a controller with mainnet config if no genesis file is defined
    // otherwise use the indicated genesis file
    final KeyPair nodeKeys = loadKeyPair(nodePrivateKeyFile);

    final StorageProvider storageProvider =
        RocksDbStorageProvider.create(homePath.resolve(DATABASE_PATH));
    if (isDevMode) {
      final GenesisConfigFile genesisConfig = GenesisConfigFile.development();
      return MainnetPantheonController.init(
          storageProvider,
          genesisConfig,
          DevelopmentProtocolSchedule.create(genesisConfig.getConfigOptions()),
          synchronizerConfiguration,
          miningParameters,
          nodeKeys);
    } else {
      final String genesisConfig =
          Resources.toString(ethNetworkConfig.getGenesisConfig().toURL(), UTF_8);
      final GenesisConfigFile genesisConfigFile = GenesisConfigFile.fromConfig(genesisConfig);
      return PantheonController.fromConfig(
          genesisConfigFile,
          synchronizerConfiguration,
          storageProvider,
          syncWithOttoman,
          ethNetworkConfig.getNetworkId(),
          miningParameters,
          nodeKeys);
    }
  }
}
