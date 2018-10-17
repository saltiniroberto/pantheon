package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Collection;

public class IbftPreparedCertificate {

  private final IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePrepareMessage;
  private final Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>> ibftPrepareMessages;

  public IbftPreparedCertificate(IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePrepareMessage, Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>> ibftPrepareMessages) {
    this.ibftPrePrepareMessage = ibftPrePrepareMessage;
    this.ibftPrepareMessages = ibftPrepareMessages;
  }


  public static IbftPreparedCertificate readFrom(final RLPInput rlpInput) {
    final IbftSignedMessageData<IbftPrePrepareUnsignedMessageData> ibftPrePreparedMessage;
    final Collection<IbftSignedMessageData<IbftPrepareUnsignedMessageData>> ibftPrepareMessages;

    rlpInput.enterList();
    ibftPrePreparedMessage = IbftMessageFactory.readSignedIbftPrePrepareMessageFrom(rlpInput);
    ibftPrepareMessages = rlpInput.readList(IbftMessageFactory::readSignedIbftPrepareMessageFrom);
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
