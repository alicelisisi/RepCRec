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