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

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftV2;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

public class IbftPrepareUnsignedMessageData extends AbstractIbftInRoundUnsignedMessageData {
  private static final int TYPE = IbftV2.PREPARE.getValue();
  private final Hash digest;

  /** Constructor used when a validator wants to send a message */
  public IbftPrepareUnsignedMessageData(
      final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    super(roundIdentifier);
    this.digest = digest;
  }

  public static IbftPrepareUnsignedMessageData readFrom(final RLPInput rlpInput) {

    rlpInput.enterList();
    final ConsensusRoundIdentifier roundIdentifier = ConsensusRoundIdentifier.readFrom(rlpInput);
    final Hash digest = readDigest(rlpInput);
    rlpInput.leaveList();

    return new IbftPrepareUnsignedMessageData(roundIdentifier, digest);
  }

  @Override
  public void writeTo(final RLPOutput rlpOutput) {

    rlpOutput.startList();
    roundIdentifier.writeTo(rlpOutput);
    rlpOutput.writeBytesValue(digest);
    rlpOutput.endList();
  }

  @Override
  public int getMessageType() {
    return TYPE;
  }

  public Hash getDigest() {
    return digest;
  }
}
