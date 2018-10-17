package tech.pegasys.pantheon.consensus.ibft;

import static org.assertj.core.api.Java6Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.core.Address;

import org.junit.Test;

public class VoteTest {
  @Test
  public void testStaticVoteCreationMethods() {
    assertThat(Vote.authVote(Address.fromHexString("1")).isAuth()).isEqualTo(true);
    assertThat(Vote.authVote(Address.fromHexString("1")).isDrop()).isEqualTo(false);

    assertThat(Vote.dropVote(Address.fromHexString("1")).isAuth()).isEqualTo(false);
    assertThat(Vote.dropVote(Address.fromHexString("1")).isDrop()).isEqualTo(true);
  }
}
