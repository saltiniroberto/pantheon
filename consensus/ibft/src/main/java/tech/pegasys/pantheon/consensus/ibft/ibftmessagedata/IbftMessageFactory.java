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
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Util;
import tech.pegasys.pantheon.util.bytes.BytesValues;

import java.util.Optional;

public class IbftMessageFactory {
  private final KeyPair validatorKayPair;

  public IbftMessageFactory(KeyPair validatorKayPair) {
    this.validatorKayPair = validatorKayPair;
  }

  public IbftSignedMessageData<IbftPrepareUnsignedMessageData> createIbftSignedPrepareMessageData(
      ConsensusRoundIdentifier roundIdentifier, Hash digest) {

    IbftPrepareUnsignedMessageData prepareUnsignedMessageData =
        new IbftPrepareUnsignedMessageData(roundIdentifier, digest);

    return createSignedMessage(prepareUnsignedMessageData);
  }

  public IbftSignedMessageData<IbftRoundChangeUnsignedMessageData>
      createIbftSignedRoundChangeMessageData(
          ConsensusRoundIdentifier roundIdentifier,
          Optional<IbftPreparedCertificate> preparedCertificate) {

    IbftRoundChangeUnsignedMessageData prepareUnsignedMessageData =
        new IbftRoundChangeUnsignedMessageData(roundIdentifier, preparedCertificate);

    return createSignedMessage(prepareUnsignedMessageData);
  }

  private <M extends AbstractIbftUnsignedMessageData> IbftSignedMessageData<M> createSignedMessage(
      M ibftUnsignedMessage) {
    final Signature signature = sign(ibftUnsignedMessage, validatorKayPair);

    return new IbftSignedMessageData<>(
        ibftUnsignedMessage, Util.publicKeyToAddress(validatorKayPair.getPublicKey()), signature);
  }

  protected static Hash hashForSignature(AbstractIbftUnsignedMessageData unsignedMessageData) {
    return Hash.hash(
        BytesValues.concatenate(
            BytesValues.ofUnsignedByte(unsignedMessageData.getMessageType()),
            unsignedMessageData.encoded()));
  }

  protected static Signature sign(
      final AbstractIbftUnsignedMessageData unsignedMessageData, final KeyPair nodeKeys) {

    return SECP256K1.sign(hashForSignature(unsignedMessageData), nodeKeys);
  }
}
