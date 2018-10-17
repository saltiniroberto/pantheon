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
package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Collection;

public class IbftPreparedCertificate {

  private final IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePrepareMessage;
  private final Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>>
      ibftPrepareMessages;

  public IbftPreparedCertificate(
      IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePrepareMessage,
      Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>> ibftPrepareMessages) {
    this.ibftPrePrepareMessage = ibftPrePrepareMessage;
    this.ibftPrepareMessages = ibftPrepareMessages;
  }

  public static IbftPreparedCertificate readFrom(final RLPInput rlpInput) {
    final IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePreparedMessage;
    final Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>> ibftPrepareMessages;

    rlpInput.enterList();
    ibftPrePreparedMessage =
        IbftSignedMessageData.readIbftSignedPrePrepareMessageDataFrom(rlpInput);
    ibftPrepareMessages =
        rlpInput.readList(IbftSignedMessageData::readIbftSignedPrepareMessageDataFrom);
    rlpInput.leaveList();

    return new IbftPreparedCertificate(ibftPrePreparedMessage, ibftPrepareMessages);
  }

  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.startList();
    ibftPrePrepareMessage.writeTo(rlpOutput);
    rlpOutput.writeList(ibftPrepareMessages, IbftSignedMessageData::writeTo);
    rlpOutput.endList();
  }
}
