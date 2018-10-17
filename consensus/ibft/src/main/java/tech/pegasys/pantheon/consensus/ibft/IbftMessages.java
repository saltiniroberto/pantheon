package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.AbstractIbftMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftPrePrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftPrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftRoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftSignedMessageData;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class IbftMessages {

  public static Optional<IbftSignedMessageData> fromMessage(final Message message) {
    final MessageData messageData = message.getData();

    Optional<? extends AbstractIbftMessage> optionalIbftMessageData =
        Stream.<Function<MessageData, Optional<? extends AbstractIbftMessage>>>of(
                IbftPrePrepareMessage::fromMessage,
                IbftPrepareMessage::fromMessage,
                IbftRoundChangeMessage::fromMessage)
            .map(fromMessage -> fromMessage.apply(messageData))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    return optionalIbftMessageData.map(AbstractIbftMessage::decode);
  }
}
