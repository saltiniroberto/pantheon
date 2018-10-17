package tech.pegasys.pantheon.consensus.ibft.ibftmessagedata;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.protocol.IbftSubProtocol;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Optional;

public class IbftRoundChangeUnsignedMessageData extends AbstractIbftUnsignedMessageData {

  private static final int TYPE = IbftSubProtocol.NotificationType.PREPARE.getValue();
  private final ConsensusRoundIdentifier roundChangeIdentifier;

  // The validator may not hae any prepared certificate
  private final Optional<IbftPreparedCertificate> preparedCertificate;

  /** Constructor used only by the {@link #readFrom(RLPInput)} method */
  public IbftRoundChangeUnsignedMessageData(
      final ConsensusRoundIdentifier roundIdentifier,
      final Optional<IbftPreparedCertificate> preparedCertificate) {
    this.roundChangeIdentifier = roundIdentifier;
    this.preparedCertificate = preparedCertificate;
  }

  public ConsensusRoundIdentifier getRoundChangeIdentifier() {
    return roundChangeIdentifier;
  }

  public Optional<IbftPreparedCertificate> getPreparedCertificate() {
    return preparedCertificate;
  }

  @Override
  public void writeTo(final RLPOutput rlpOutput) {
    // RLP encode of the message data content (round identifier and prepared certificate)
    BytesValueRLPOutput ibftMessage = new BytesValueRLPOutput();
    ibftMessage.startList();
    roundChangeIdentifier.writeTo(ibftMessage);

    if (preparedCertificate.isPresent()) {
      preparedCertificate.get().writeTo(ibftMessage);
    } else {
      ibftMessage.writeNull();
    }
    ibftMessage.endList();
  }

  public static IbftRoundChangeUnsignedMessageData readFrom(final RLPInput rlpInput) {
    rlpInput.enterList();
    final ConsensusRoundIdentifier roundIdentifier = ConsensusRoundIdentifier.readFrom(rlpInput);

    final Optional<IbftPreparedCertificate> preparedCertificate;

    if (rlpInput.nextIsNull()) {
      rlpInput.skipNext();
      preparedCertificate = Optional.empty();
    } else {
      preparedCertificate = Optional.of(IbftPreparedCertificate.readFrom(rlpInput));
    }
    rlpInput.leaveList();

    return new IbftRoundChangeUnsignedMessageData(roundIdentifier, preparedCertificate);
  }

  @Override
  public int getMessageType() {
    return TYPE;
  }
}
