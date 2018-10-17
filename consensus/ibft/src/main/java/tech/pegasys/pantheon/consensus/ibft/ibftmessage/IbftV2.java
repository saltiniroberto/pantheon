package tech.pegasys.pantheon.consensus.ibft.ibftmessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/** Message codes for iBFT v2 messages */
public enum IbftV2 {
  PRE_PREPARE(0),
  PREPARE(1),
  COMMIT(2),
  ROUND_CHANGE(3);

  private final int value;

  IbftV2(final int value) {
    this.value = value;
  }

  public final int getValue() {
    return value;
  }

  public static int getMax() {
    return Collections.max(Arrays.asList(IbftV2.values()), Comparator.comparing(IbftV2::getValue))
        .getValue();
  }

  public static Optional<IbftV2> fromValue(final int i) {
    return Stream.of(IbftV2.values()).filter(n -> n.getValue() == i).findFirst();
  }
}
