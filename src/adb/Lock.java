package adb;

import java.util.Objects;

public class Lock {
  final LockType type;
  final String transactionId;
  final String variableId;

  public Lock(LockType type, String transactionId, String variableId) {
    this.type = type;
    this.transactionId = transactionId;
    this.variableId = variableId;
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == this) {
      return true;
    }

    if (!(ob instanceof Lock)) {
      return false;
    }

    Lock l = (Lock) ob;
    return type == l.type && transactionId == l.transactionId && variableId == l.variableId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, transactionId, variableId);
  }

}

enum LockType {
  RL("READ_LOCK"),
  WL("WRITE_LOCK");

  private final String type;

  LockType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

}