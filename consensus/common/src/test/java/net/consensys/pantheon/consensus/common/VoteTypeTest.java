package net.consensys.pantheon.consensus.common;

import org.junit.Test;
import tech.pegasys.pantheon.consensus.common.VoteType;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class VoteTypeTest {
  @Test
  public void testValidatorVoteMethodImplementation() {
    assertThat(VoteType.ADD.isAddVote()).isTrue();
    assertThat(VoteType.ADD.isDropVote()).isFalse();

    assertThat(VoteType.DROP.isAddVote()).isFalse();
    assertThat(VoteType.DROP.isDropVote()).isTrue();
  }
}
