package tech.pegasys.pantheon.consensus.ibft;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class Ibft2VoteTypeTest {

  @Test
  public void testValidatorVoteMethodImplementation() {
    assertThat(Ibft2VoteType.ADD.isAddVote()).isTrue();
    assertThat(Ibft2VoteType.ADD.isDropVote()).isFalse();

    assertThat(Ibft2VoteType.DROP.isAddVote()).isFalse();
    assertThat(Ibft2VoteType.DROP.isDropVote()).isTrue();
  }
}
