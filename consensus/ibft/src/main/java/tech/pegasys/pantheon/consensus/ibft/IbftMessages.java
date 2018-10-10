package tech.pegasys.pantheon.consensus.ibft;

import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.AbstractIbftMessageData;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrePrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftPrepareMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.IbftRoundChangeMessage;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;
import tech.pegasys.pantheon.ethereum.p2p.api.MessageData;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class IbftMessages {

  public static AbstractIbftMessageDecoded fromMessage(final Message message) {
    final MessageData messageData = message.getData();

    AbstractIbftMessageData ibftMessageData =
        Stream.<Function<MessageData, Optional<AbstractIbftMessageData>>>of(
                IbftPrePrepareMessage::fromMessage,
                IbftPrepareMessage::fromMessage,
                IbftRoundChangeMessage::fromMessage)
            .map(fromMessage -> fromMessage.apply(messageData))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Message is not a valid iBFT message"));

    return ibftMessageData.decode();
  }
}
