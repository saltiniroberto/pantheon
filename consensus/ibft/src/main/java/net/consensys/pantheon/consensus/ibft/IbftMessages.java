package net.consensys.pantheon.consensus.ibft;

import net.consensys.pantheon.consensus.ibft.ibftmessagedata.AbstractIbftMessageData;
import net.consensys.pantheon.consensus.ibft.ibftmessagedata.IbftPrePrepareMessage;
import net.consensys.pantheon.consensus.ibft.ibftmessagedata.IbftPrepareMessage;
import net.consensys.pantheon.consensus.ibft.ibftmessagedata.IbftRoundChangeMessage;
import net.consensys.pantheon.consensus.ibft.ibftmessagedecoded.AbstractIbftMessageDecoded;
import net.consensys.pantheon.ethereum.p2p.api.Message;
import net.consensys.pantheon.ethereum.p2p.api.MessageData;

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
