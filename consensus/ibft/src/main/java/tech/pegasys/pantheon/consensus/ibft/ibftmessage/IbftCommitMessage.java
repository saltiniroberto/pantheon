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
package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedCommitMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class IbftCommitMessage extends AbstractIbftMessage {

  private static final int MESSAGE_CODE = IbftV2.COMMIT;

  private IbftCommitMessage(final BytesValue data) {
    super(data);
  }

  public static IbftCommitMessage fromMessage(final MessageData message) {
    if (message instanceof IbftCommitMessage) {
      return (IbftCommitMessage) message;
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      throw new IllegalArgumentException(
          String.format("Message has code %d and thus is not a CommitMessage", code));
    }

    return new IbftCommitMessage(message.getData());
  }

  @Override
  public IbftSignedMessageData<IbftUnsignedCommitMessageData> decode() {
    return IbftSignedMessageData.readIbftSignedCommitMessageDataFrom(RLP.input(data));
  }

  public static IbftCommitMessage create(
      final IbftSignedMessageData<IbftUnsignedCommitMessageData> ibftPrepareMessageDecoded) {

    return new IbftCommitMessage(ibftPrepareMessageDecoded.encode());
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
