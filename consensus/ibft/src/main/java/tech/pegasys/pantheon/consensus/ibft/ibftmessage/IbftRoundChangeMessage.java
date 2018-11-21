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
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedRoundChangeMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;
import tech.pegasys.pantheon.ethereum.rlp.RLP;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public class IbftRoundChangeMessage extends AbstractIbftMessage {

  private static final int MESSAGE_CODE = IbftV2.ROUND_CHANGE;

  private IbftRoundChangeMessage(final BytesValue data) {
    super(data);
  }

  public static IbftRoundChangeMessage fromMessage(final MessageData message) {
    if (message instanceof IbftRoundChangeMessage) {
      return (IbftRoundChangeMessage) message;
    }
    final int code = message.getCode();
    if (code != MESSAGE_CODE) {
      throw new IllegalArgumentException(
          String.format("Message has code %d and thus is not a RoundChangeMessage", code));
    }

    return new IbftRoundChangeMessage(message.getData());
  }

  @Override
  public IbftSignedMessageData<IbftUnsignedRoundChangeMessageData> decode() {
    return IbftSignedMessageData.readIbftSignedRoundChangeMessageDataFrom(RLP.input(data));
  }

  public static IbftRoundChangeMessage create(
      final IbftSignedMessageData<IbftUnsignedRoundChangeMessageData> ibftPrepareMessageDecoded) {

    return new IbftRoundChangeMessage(ibftPrepareMessageDecoded.encode());
  }

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }
}
