package adb;

import java.util.*;


public class TransactionManager {
  private Map<String, Transaction> transactions;
  private List<String> waitingList;
  private Set<String> abortList;
  private final Simulator db;
  private Map<String, Set<String>> waitForGraph;


  public TransactionManager(Simulator db) {
    this.db = db;
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

    /*Operation op = new Operation(OperationType.W, vId, val);
    transactions.get(tId).addOperation(op);
    if (isDeadLock("?", tId)) {
      handleDeadLock(transactions.get(tId));
      if (!abortList.contains(tId)) {
        write(tId, vId, val);
      }
    } else {
      waitingList.add(tId);
      db.printVerbose(tId + " waits");
    }*/

    waitingList.add(tId);
    db.printVerbose(tId + " waits");

  }

  private boolean handleSameTransaction(Site s, String vId, String tId, int val) {
    boolean result = true;
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
	
}