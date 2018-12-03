package adb;

import java.util.Objects;

public class Operation {
  final OperationType type;
  final String variableId;
  private int value;

  public Operation(OperationType type) {
    this.type = type;
    variableId = null;
  }

  public Operation(OperationType type, String variable) {
    type = type;
    variableId = variable;
  }

  public Operation(Operation type, String variable, int value) {
    if (type != OperationType.W) {
      throw new IllegalArgumentException("Error: operation should be WRITE");
    }

    type = type;
    variableId = variable;
    value = value;
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