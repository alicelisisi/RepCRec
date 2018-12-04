package adb;

import java.util.*;


public class TransactionManager {
  public Map<String, Transaction> transactions;
  public List<String> waitingList;
  public Set<String> abortList;
  public static Simulator db;
  public Map<String, Set<String>> waitForGraph;

 
  public TransactionManager(Simulator db) {
    this.db = db;
    this.transactions = new HashMap<>();
    this.waitingList = new ArrayList<>();
    this.abortList = new HashSet<>();
    this.waitForGraph = new HashMap<>();
  }

  public void begin(String tId, int timeStamp, boolean readOnly) {
  	Transaction t = new Transaction(tId, timeStamp, readOnly);
  	if (transactions.containsKey(tId)) {
  	  System.out.println("Error: " + tId + " has already begun.");
  	  return;
  	}
  	transactions.put(tId, t);
  	db.printVerbose(tId + " begins");
  }

  public void write(String tId, String vId, int val) {
    if (abortList.contains(tId)) {
      System.out.println("Failed to read " + vId + " beacuse " + tId + " is aborted");
      return;
    }

    if (waitingList.contains(tId)) {
      System.out.println("Error: cannot write " + vId + " beacuse " + tId + " is waiting");
      return;  
    }

    if (isOddVariable(vId)) {
      writeOddVariable(tId, vId, val);
    } else {
      writeEvenVariable(tId, vId, val);
    }

  }

  private void writeOddVariable(String tId, String vId, int val) { 
    Site s = db.siteList.get(Integer.valueOf(vId.substring(1)) % db.numOfSite);
    if (s.isFailed) {
      db.printVerbose("cannot write on failed site " + s.siteId);
      writeHold(tId, vId, val, s);
      return;
    } 

    if (!handleSameTransaction(s, vId, tId, val)) {
      return;
    }
    
    s.siteWrite(vId, val);
    transactions.get(tId).changedV.add(vId);
    db.printVerbose(tId + " writes on " + vId + " at site " + s.siteId + ": " + val);
    List<Lock> l = new ArrayList<>();
    l.add(new Lock(LockType.WL, tId, vId));
    s.lockTable.put(vId, l);
  }

  private void writeHold(String tId, String vId, int val, Site s) {
    if (!waitForGraph.containsKey(tId)) {
      waitForGraph.put(tId, new HashSet<>());
    }
    List<Lock> locks = s.lockTable.get(vId);
    for (Lock l : locks) { 
      if (!l.transactionId.equals(tId)) {
        waitForGraph.get(tId).add(l.transactionId);
      }
    }

    Operation op = new Operation(OperationType.W, vId, val);
    transactions.get(tId).addOperation(op);
    if (isDeadLock("?", tId)) {
      handleDeadLock(transactions.get(tId));
      if (!abortList.contains(tId)) {
        write(tId, vId, val);
      }
    } else {
      waitingList.add(tId);
      db.printVerbose(tId + " waits");
    }

  }

  private boolean handleSameTransaction(Site s, String vId, String tId, int val) {
    boolean result = true;
    transactions.get(tId).addTouchedSite(s.siteId, db.timeStamp);
    if (s.lockTable.containsKey(vId) && !s.lockTable.get(vId).isEmpty()) {
      List<Lock> locks = s.lockTable.get(vId);
      Lock lastLock = locks.get(locks.size() - 1);
      if (lastLock.transactionId.equals(tId)) {
        if (lastLock.type.equals(LockType.RL)) {
          s.lockTable.get(vId).remove(locks.size() - 1);
          s.lockTable.get(vId).add(new Lock(LockType.WL, tId, vId));
        }
      } else {
        result = false;
        writeHold(tId, vId, val, s);
      }
    }

    return result;
  }
  
  private void writeEvenVariable(String tId, String vId, int val) {
    int count = 0;
    for (Site s : db.siteList) {
      if (s.isFailed) {
        count++;
        continue;
      }
      
      if (!handleSameTransaction(s, vId, tId, val)) {
      return;
      }
      s.siteWrite(vId, val);
      List<Lock> l = new ArrayList<>();
      l.add(new Lock(LockType.WL, tId, vId));
      s.lockTable.put(vId, l);
    }
    /*if (count == db.numOfSite) {
      writeHold(tId, vId, val, s);
    }*/
    transactions.get(tId).changedV.add(vId);
    db.printVerbose(tId + " writes on " + vId + " at all available sites: " + val);
  }

  private boolean isOddVariable(String v) {
    int val = Integer.valueOf(v.substring(1));
    if (val % 2 == 1) {
      return true;
    }
    return false;
  }	

  public void handleDeadLock(Transaction t) {
    String id = t.tId;
    db.printVerbose("dead lock detected!");
    List<String> cycle = getCycle(id);
    Transaction toAbort = t;
    int time = -1;
    for (String i : cycle) {
      Transaction current = transactions.get(i);
      if (current.startTime > time) {
        toAbort = current;
        time = current.startTime;
      }
    }

    System.out.println("abort " + toAbort.tId);
    abortTransaction(toAbort.tId, true);
  }

  private List<String> getCycle(String start) {
    List<String> cycle = new ArrayList<>();
    List<String> result = new ArrayList<>();
    dfs(start, cycle, result);
    return result;
  }

  private void dfs(String current, List<String> cycle, List<String> res) {
    if (waitForGraph.size() == 0 || !waitForGraph.containsKey(current)) {
      return;
    }

    for (String tid : waitForGraph.get(current)) {
      if (!cycle.contains(tid)) {
        cycle.add(tid);
        dfs(tid, cycle, res);
        cycle.remove(tid);
      } else {
        res = new ArrayList<>(cycle);
      }
    }
  }

  private boolean isDeadLock(String tId, String start) {
    if (tId.equals(start)) {
      return true;
    }
    if (tId.equals("?")) {
      tId = start;
    }

    if (!waitForGraph.containsKey(tId) || waitForGraph.get(tId).isEmpty()) {
      return false;
    }

    for (String next : waitForGraph.get(tId)) {
      if (isDeadLock(next, start)) {
        return true;
      }
    }

    return false;
  }

  public void abortTransaction(String abortId, boolean isAborted) {
    Transaction target = transactions.get(abortId);

    // Release all the locks
    for (int sid : target.touchedSiteTime.keySet()) {
      Site s = db.siteList.get(sid - 1);
      s.releaseLocks(target);
    }

    //remove from waiting list
    waitingList.remove(abortId);

    //remove from waitFor Graph
    for (String tid : waitForGraph.keySet()) {
      for (String child : waitForGraph.get(tid)) {
        if (target.tId.equals(child)) {
          waitForGraph.get(tid).remove(child);
          break;
        }
        
      }
    }
    waitForGraph.remove(abortId);

    //revert values

    if (isAborted) {
      abortList.add(abortId);
      for (int sid : target.touchedSiteTime.keySet()) {
        Site s = db.siteList.get(sid - 1);
        for (String dv : target.changedV) {
          Variable v = s.getVariable(dv);
          if (v != null) {
            v.revert();
          }
        }
      }
    }

    runNextWaiting();
  }

  public void runNextWaiting() {
    if (waitingList.isEmpty()) {
      return;
    }
    List<String> temp = new ArrayList<>();
    temp.addAll(waitingList);
    for (String nextTid : temp) {
      if (waitForGraph.containsKey(nextTid) && !waitForGraph.get(nextTid).isEmpty()) {
        db.printVerbose(nextTid + "still waits!");
        continue;
      }
      waitingList.remove(nextTid);
      String nextVid = transactions.get(nextTid).pendingOp.variableId;
      if (transactions.get(nextTid).pendingOp.type == OperationType.R) {
        System.out.println("to be implemented");
      } else if (transactions.get(nextTid).pendingOp.type == OperationType.W) {
        int nextVal = transactions.get(nextTid).pendingOp.readValue();
        write(nextTid, nextVid, nextVal); 
      } else {
        commitTransaction(nextTid, db.timeStamp);
      }
    }
  }

  public void commitTransaction(String tId, int timeStamp) {
    if (abortList.contains(tId)) {
      System.out.println("Failed: " + tId + " was aborted");
      return;
    }

    Transaction target = transactions.get(tId);
    boolean canCommit = true;
    if (waitingList.contains(tId) && isDeadLock("?", tId)) {
      handleDeadLock(target);
    }

    if (target.readOnly) {
      for (int sid : target.touchedSiteTime.keySet()) {
        Site s = db.siteList.get(sid - 1);
        if (s.isFailed) {
          canCommit = false;
          break;
        }
      }

      if (canCommit) {
        System.out.println("commit " + tId);
      } else {
        Operation op = new Operation(OperationType.C);
        target.addOperation(op);
        waitingList.add(tId);
      }
      return;
    }

    for (int sid : target.touchedSiteTime.keySet()) {
      Site s = db.siteList.get(sid - 1);
      int firstTouch = target.touchedSiteTime.get(sid);
      if (firstTouch <= s.failedTime) {
        canCommit = false;
        break;
      }
    }

    for (int sid : target.touchedSiteTime.keySet()) {
      Site s = db.siteList.get(sid - 1);
      for (String dv : target.changedV) {
        Variable v = s.getVariable(dv);
        if (v != null) {
          if (canCommit) {
            v.commit(timeStamp);
          } else {
            v.revert();
          }
        }
      }
    }

    if (!canCommit) {
      System.out.println("abort " + tId);
    } else {
      System.out.println("commit " + tId);
    }

    abortTransaction(tId, !canCommit);


  }

  public void read(String tId, String vId) {
    if (abortList.contains(tId)) {
      System.out.println("Failed to read " + vId + " because " + tId + " is aborted");
      return;
    }

    if (waitingList.contains(tId)) {
      db.printVerbose("Failed: " + tId + " is waiting");
      return;
    }
    

    Transaction t = transactions.get(tId);
    if (t == null) {
      System.out.println("Error: " + tId + " did not begin");
      return;
    }

    Site s = selectSite(vId);
    if (s == null) {
      db.printVerbose("No available site for accessing " + vId);
      Operation op = new Operation(Operation.R, vId);
      t.addOperation(op);
      waitingList.add(tId);
      return;
    }

    if (t.readOnly) {
      int value = s.readVariable(vId, t.startTime);
      System.out.println(tId + " reads " + vId + " at site " + s.siteId + ": " + value);
      t.addTouchedSite(s.siteId, db.timeStamp);
      return;
    }

    regularRead(tId, vId, s);

  }

  public void regularRead(String tId, String vId, Site s) {
    if (!s.variableList.containsKey(vId)) {
      throw new IllegalArgumentException("Error: " + vId + " not found in the site " + s.siteId);
    }

    Transaction t = transactions.get(tId);
    t.addTouchedSite(s.siteId, db.timeStamp);
    if (!s.lockTable.containsKey(vId) || s.lockTable.get(vId).isEmpty()) {
      executeRead(tId,vId, s);
    }

    for (Lock l : s.lockTable.get(vId)) {
      if (l.transactionId.equals(tId)) {
        executeRead(tId,vId, s); 
      } else if (l.type == LockType.WL) {
        if (!waitForGraph.containsKey(tId)) {
          waitForGraph.put(tId, new HashSet<>());
        }
        waitForGraph.get(tId).add(l);
        readHold(tId, vId, s);
      }
    }
  }

  public void readHold(String tId, String vId, Site s) {
    Transaction t = transactions.get(tId);
    Operation op = new Operation(OperationType.R, vId);
    t.addOperation(op);
    if (isDeadLock("?", tId)) {
      handleDeadLock(t);
      if (!abortList.contains(tId)) {
        read(tId, vId);
      }
    } else {
      waitingList.add(tId);
      db.printVerbose(tId + " waits");
    }
  }

  public void executeRead(String tId, String vId, Site s) {
    int value = s.readVariable(vId, false);
    s.lockTable.get(vId).add(new Lock(LockType.RL, tId, vId));
    System.out.println(tId + " reads " + vId + " at site " + s.siteId + ": " + value);
  }

  public Site selectSite(String vId) {
    int val = Integer.parseInt(vId.substring(1))
    if (isOddVariable(vId)) {
      Site s = siteList.get(val % numOfSite);
      if (s.isFailed) {
        return null;
      }
      return s;
    }

    for (Site s : siteList) {
      if (!s.isFailed && s.getVariable(vId).canRead()) {
        return s;
      }
    }

    System.out.println("No available site for accessing " + vId);
    return null;
  }
	
}