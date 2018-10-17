/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
