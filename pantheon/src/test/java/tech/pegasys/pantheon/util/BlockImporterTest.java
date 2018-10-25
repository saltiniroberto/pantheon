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
package tech.pegasys.pantheon.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.controller.MainnetPantheonController;
import tech.pegasys.pantheon.controller.PantheonController;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.MiningParametersTestBuilder;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.testutil.BlockTestUtil;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BlockImporter}. */
public final class BlockImporterTest {

  @Rule public final TemporaryFolder folder = new TemporaryFolder();

  BlockImporter blockImporter = new BlockImporter();

  @Test
  public void blockImport() throws IOException {
    final Path source = folder.newFile().toPath();
    final Path target = folder.newFolder().toPath();
    BlockTestUtil.write1000Blocks(source);
    final BlockImporter.ImportResult result =
        blockImporter.importBlockchain(source, MainnetPantheonController.mainnet(target));
    assertThat(result.count).isEqualTo(1000);
    assertThat(result.td).isEqualTo(UInt256.of(21991996248790L));
  }

  @Test
  public void ibftLegacyImport() throws IOException {
    final Path source = folder.newFile().toPath();
    final Path target = folder.newFolder().toPath();
    final String config =
        Resources.toString(Resources.getResource("ibftlegacy_genesis.json"), UTF_8);

    try {
      Files.write(
          source,
          Resources.toByteArray(Resources.getResource("ibft.blocks")),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (final IOException ex) {
      throw new IllegalStateException(ex);
    }

    final PantheonController<?, ?> controller =
        PantheonController.fromConfig(
            SynchronizerConfiguration.builder().build(),
            config,
            target,
            false,
            10,
            new MiningParametersTestBuilder().enabled(false).build(),
            KeyPair.generate());
    final BlockImporter.ImportResult result = blockImporter.importBlockchain(source, controller);

    assertThat(result.count).isEqualTo(959);
  }

  // TODO: Add test for iBFT 2.0 block import once a valid iBFT 2.0 exported blockchain is available
}
