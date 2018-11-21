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

import tech.pegasys.pantheon.consensus.ibft.blockcreation.IbftBlockCreatorFactory;

/** Stateful evaluator for ibft events */
public class IbftStateMachine {

  private final IbftBlockCreatorFactory blockCreatorFactory;

  public IbftStateMachine(final IbftBlockCreatorFactory blockCreatorFactory) {
    this.blockCreatorFactory = blockCreatorFactory;
  }

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
    return true;
  }
}
