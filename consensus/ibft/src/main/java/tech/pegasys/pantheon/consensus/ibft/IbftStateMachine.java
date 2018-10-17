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

import tech.pegasys.pantheon.consensus.ibft.ibftevent.IbftMessageEvent;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.AbstractIbftUnsignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedPrePrepareMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftUnsignedPrepareMessageData;

/** Stateful evaluator for ibft events */
public class IbftStateMachine {

  /**
   * Attempt to consume the event and update the maintained state
   *
   * @param event the external action that has occurred
   * @param roundTimer timer that will fire expiry events that are expected to be received back into
   *     this machine
   * @return whether this event was consumed or requires reprocessing later once the state machine
   *     catches up
   */
  public boolean processEvent(final IbftEvent event, final RoundTimer roundTimer) {
    // TODO: don't just discard the event, do some logic
    if(event instanceof IbftMessageEvent)
    {
      IbftSignedMessageData<?> signedMessageData = ((IbftMessageEvent)event).ibftMessage();
      AbstractIbftUnsignedMessageData usignedMessageData = signedMessageData.getUnsignedMessageData();

      if(usignedMessageData instanceof IbftUnsignedPrepareMessageData)
      {
        return handlePrePrepare((IbftSignedMessageData<IbftUnsignedPrePrepareMessageData>)signedMessageData);
      }
      else if(usignedMessageData instanceof IbftUnsignedPrePrepareMessageData)
      {
        return handlePrepare((IbftSignedMessageData<IbftUnsignedPrepareMessageData>)signedMessageData);
      }

    }
    return true;
  }

  private static boolean handlePrePrepare(IbftSignedMessageData<IbftUnsignedPrePrepareMessageData> signedMessageData)
  {
    return true;
  }

  private static boolean handlePrepare(IbftSignedMessageData<IbftUnsignedPrepareMessageData> signedMessageData)
  {
    return true;
  }
}
