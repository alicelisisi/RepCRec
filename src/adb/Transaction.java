package adb;

import java.util.*;

public class Transaction {
  final String tId;
  final int startTime;
  final boolean readOnly;
  final List<String> changedV;
  Operation pendingOp;
  final Map<Integer, Integer> touchSiteTime;

  public Transaction(String id, int timeStamp, boolean readOnly) {
    this.tId = id;
    this.startTime = timeStamp;
    this.readOnly = readOnly;
    this.changedV = new ArrayList<>();
    this.pendingOp = null;
    this.touchSiteTime = new HashMap<>();
  }

  public void addOperation(Operation op) {
    pendingOp = op;
  }

  public void addTouchedSite(int id, int time) {
    if (!touchSiteTime.containsKey(id)) {
      touchSiteTime.put(id, time);
    }
  }
}