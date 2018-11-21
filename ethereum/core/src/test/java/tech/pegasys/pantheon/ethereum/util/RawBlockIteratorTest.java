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
package tech.pegasys.pantheon.ethereum.util;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.testutil.BlockDataGenerator;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RawBlockIteratorTest {

  @Rule public final TemporaryFolder tmp = new TemporaryFolder();
  private BlockDataGenerator gen;

  @Before
  public void setup() {
    gen = new BlockDataGenerator(1);
  }

  @Test
  public void readsBlockAtBoundaryOfInitialCapacity() throws IOException {
    readsBlocksWithInitialCapacity(Function.identity());
  }

  @Test
  public void readsBlockThatExtendsPastInitialCapacity() throws IOException {
    readsBlocksWithInitialCapacity((size) -> size / 2);
  }

  @Test
  public void readsBlockWithinInitialCapacity() throws IOException {
    readsBlocksWithInitialCapacity((size) -> size * 2);
  }

  public void readsBlocksWithInitialCapacity(
      final Function<Integer, Integer> initialCapacityFromBlockSize) throws IOException {
    final int blockCount = 3;
    final List<Block> blocks = gen.blockSequence(blockCount);

    // Write a few blocks to a tmp file
    byte[] firstSerializedBlock = null;
    final File blocksFile = tmp.newFolder().toPath().resolve("blocks").toFile();
    final DataOutputStream writer = new DataOutputStream(new FileOutputStream(blocksFile));
    for (Block block : blocks) {
      final byte[] serializedBlock = serializeBlock(block);
      writer.write(serializedBlock);
      if (firstSerializedBlock == null) {
        firstSerializedBlock = serializedBlock;
      }
    }
    writer.close();

    // Read blocks
    final int initialCapacity = initialCapacityFromBlockSize.apply(firstSerializedBlock.length);
    final RawBlockIterator iterator =
        new RawBlockIterator(
            blocksFile.toPath(),
            rlp -> BlockHeader.readFrom(rlp, MainnetBlockHashFunction::createHash),
            initialCapacity);

    // Read blocks and check that they match
    for (int i = 0; i < blockCount; i++) {
      assertThat(iterator.hasNext()).isTrue();
      final Block readBlock = iterator.next();
      final Block expectedBlock = blocks.get(i);
      assertThat(readBlock).isEqualTo(expectedBlock);
    }

    assertThat(iterator.hasNext()).isFalse();
  }

  private byte[] serializeBlock(final Block block) {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    block.getHeader().writeTo(out);
    out.writeList(block.getBody().getTransactions(), Transaction::writeTo);
    out.writeList(block.getBody().getOmmers(), BlockHeader::writeTo);
    out.endList();
    return out.encoded().extractArray();
  }
}
