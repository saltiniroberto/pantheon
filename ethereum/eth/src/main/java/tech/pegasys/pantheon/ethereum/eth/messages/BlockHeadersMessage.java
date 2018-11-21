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
package tech.pegasys.pantheon.ethereum.eth.messages;

import tech.pegasys.pantheon.ethereum.core.BlockHashFunction;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.ethereum.mainnet.ScheduleBasedBlockHashFunction;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.p2p.wire.AbstractMessageData;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPInput;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Iterator;

public final class BlockHeadersMessage extends AbstractMessageData {

  public static BlockHeadersMessage readFrom(final MessageData message) {
    if (message instanceof BlockHeadersMessage) {
      return (BlockHeadersMessage) message;
    }
    final int code = message.getCode();
    if (code != EthPV62.BLOCK_HEADERS) {
      throw new IllegalArgumentException(
          String.format("Message has code %d and thus is not a BlockHeadersMessage.", code));
    }
    return new BlockHeadersMessage(message.getData());
  }

  public static BlockHeadersMessage create(final Iterable<BlockHeader> headers) {
    final BytesValueRLPOutput tmp = new BytesValueRLPOutput();
    tmp.startList();
    for (final BlockHeader header : headers) {
      header.writeTo(tmp);
    }
    tmp.endList();
    return new BlockHeadersMessage(tmp.encoded());
  }

  private BlockHeadersMessage(final BytesValue data) {
    super(data);
  }

  @Override
  public int getCode() {
    return EthPV62.BLOCK_HEADERS;
  }

  public <C> Iterator<BlockHeader> getHeaders(final ProtocolSchedule<C> protocolSchedule) {
    final BlockHashFunction blockHashFunction =
        ScheduleBasedBlockHashFunction.create(protocolSchedule);
    return new BytesValueRLPInput(data, false)
        .readList(rlp -> BlockHeader.readFrom(rlp, blockHashFunction))
        .iterator();
  }
}
