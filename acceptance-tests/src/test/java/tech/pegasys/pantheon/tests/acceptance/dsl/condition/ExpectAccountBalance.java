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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.web3j.utils.Convert.toWei;
import static tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils.waitFor;

import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;

import org.web3j.utils.Convert.Unit;

public class ExpectAccountBalance implements Condition {

  private final Account account;
  private final String expectedBalance;
  private final Unit balanceUnit;

  public ExpectAccountBalance(
      final Account account, final String expectedBalance, final Unit balanceUnit) {
    this.expectedBalance = expectedBalance;
    this.balanceUnit = balanceUnit;
    this.account = account;
  }

  @Override
  public void verify(final Node node) {
    waitFor(
        () ->
            assertThat(node.getAccountBalance(account))
                .isEqualTo(toWei(expectedBalance, balanceUnit).toBigIntegerExact()));
  }
}
