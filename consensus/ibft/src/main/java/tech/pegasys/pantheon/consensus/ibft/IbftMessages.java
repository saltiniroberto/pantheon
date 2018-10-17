package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.AbstractIbftMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftPrePrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftPrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftRoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftV2;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Optional;

public class IbftMessages {

  public static Optional<IbftSignedMessageData> fromMessage(final Message message) {
    final MessageData messageData = message.getData();

    final AbstractIbftMessage ibftMessage;

    switch (messageData.getCode()) {
      case IbftV2.PRE_PREPARE_MGS:
        ibftMessage = IbftPrePrepareMessage.fromMessage(messageData);
        break;

      case IbftV2.PREPARE_MGS:
        ibftMessage = IbftPrepareMessage.fromMessage(messageData);
        break;

      case IbftV2.ROUND_CHANGE_MSG:
        ibftMessage = IbftRoundChangeMessage.fromMessage(messageData);
        break;

      default:
        return Optional.empty();
    }

    return Optional.of(ibftMessage.decode());
  }
}
