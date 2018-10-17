package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public abstract class AbstractIbftUnsignedMessageData {
  public abstract void writeTo(final RLPOutput rlpOutput);

  public BytesValue encoded() {
    BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
    writeTo(rlpOutput);

    return rlpOutput.encoded();
  }

  public abstract int getMessageType();

  protected static Hash readDigest(final RLPInput ibftMessageData) {
    return Hash.wrap(ibftMessageData.readBytes32());
  }
}
