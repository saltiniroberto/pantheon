package tech.pegasys.pantheon.consensus.ibft.protocol;

import tech.pegasys.pantheon.consensus.ibft.ibftmessage.IbftV2;
import tech.pegasys.pantheon.ethereum.p2p.wire.Capability;
import tech.pegasys.pantheon.ethereum.p2p.wire.SubProtocol;

public class IbftSubProtocol implements SubProtocol {

  public static String NAME = "IBF";
  public static final Capability IBFV1 = Capability.create(NAME, 1);

  private static final IbftSubProtocol INSTANCE = new IbftSubProtocol();

  public static IbftSubProtocol get() {
    return INSTANCE;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int messageSpace(final int protocolVersion) {
    return IbftV2.getMax() + 1;
  }

  @Override
  public boolean isValidMessageCode(final int protocolVersion, final int code) {
    return IbftV2.fromValue(code).isPresent();
  }
}
