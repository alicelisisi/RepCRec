package adb;

import java.util.*;

public class Transaction {
  public String tId;
  public int startTime;
  public boolean readOnly;
  public List<String> changedV;
  public Operation pendingOp;
  public Map<Integer, Integer> touchedSiteTime;

  public Transaction(String id, int timeStamp, boolean readOnly) {
    
    this.tId = id;
    this.startTime = timeStamp;
    this.readOnly = readOnly;
    this.changedV = new ArrayList<>();
    this.pendingOp = null;
    this.touchedSiteTime = new HashMap<>();
  }

  public void addOperation(Operation op) {
    pendingOp = op;
  }

  public void addTouchedSite(int id, int time) {
    if (!touchedSiteTime.containsKey(id)) {
      touchedSiteTime.put(id, time);
    }
  }
}