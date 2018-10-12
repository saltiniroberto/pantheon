package tech.pegasys.pantheon.ethereum.vm;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/** A mock for representing EVM Code associated with an account. */
public class CodeMock extends Code {

  /**
   * Public constructor.
   *
   * @param bytes - A hex string representation of the code.
   */
  @JsonCreator
  public CodeMock(final String bytes) {
    super(BytesValue.fromHexString(bytes));
  }
}
