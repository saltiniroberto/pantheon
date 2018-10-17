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

    Optional<IbftV2> messageCode = IbftV2.fromValue(messageData.getCode());

    Optional<? extends AbstractIbftMessage> optionalIbftMessage =
        messageCode.map(
            code -> {
              switch (code) {
                case PRE_PREPARE:
                  return IbftPrePrepareMessage.fromMessage(messageData);

                case PREPARE:
                  return IbftPrepareMessage.fromMessage(messageData);

                case ROUND_CHANGE:
                  return IbftRoundChangeMessage.fromMessage(messageData);
              }

              return null;
            });

    return optionalIbftMessage.map(AbstractIbftMessage::decode);
  }
}
