import java.util.*;

public class Transaction {
  final String tId;
  final int startTime;
  final boolean readOnly;

  public Transaction(String id, int timeStamp, boolean readOnly) {
    this.tId = id;
    this.startTime = timeStamp;
    this.readOnly = readOnly;
  }
}