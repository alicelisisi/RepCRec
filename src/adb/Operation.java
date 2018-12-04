package adb;

import java.util.Objects;

public class Operation {
  public OperationType type;
  public String variableId;
  public int value;

  public Operation(OperationType type) {
    this.type = type;
    this.variableId = null;
  }

  public Operation(OperationType type, String variable) {
    this.type = type;
    this.variableId = variable;
  }

  public Operation(OperationType type, String variable, int value) {
    if (type != OperationType.W) {
      throw new IllegalArgumentException("Error: operation should be WRITE");
    }

    this.type = type;
    this.variableId = variable;
    this.value = value;
  }

  public int readValue() {
    return value;
  }


	
}

enum OperationType {
  R ("READ"),
  W ("WRITE"),
  C ("COMMIT");

  private final String type;

  OperationType(String type) {
  	this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }
}