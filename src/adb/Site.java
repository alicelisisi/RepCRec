package adb;

import java.util.*;
import java.io.*;

public class Site {
  final int siteId;
  final Map<String, Variable> variableList;
  public boolean isFailed;
  final Map<String, List<Lock>> lockTable;
  public int failedTime;
  
  
  
  
  
  
  
  
  public Site(int id) {
    this.siteId = id;
    this.variableList = new HashMap<>();
    this.isFailed = false;
    this.lockTable = new HashMap<>();
    this.failedTime = -1;
  }
  
  public void addVariable(Variable v) {
    if (!variableList.containsKey(v.vId)) {
      variableList.put(v.vId, v);
    }
  }

  public void siteWrite(String vId, int val) {
    if (!variableList.containsKey(vId)) {
      throw new IllegalArgumentException("Error: " + vId + "not found in the site " + siteId);
    }

    Variable var = variableList.get(vId);
    var.write(val);
    var.recoverVariable();
    if (failedTime == -1) {
      variableList.put(vId, var);
    }

  }

  public void releaseLocks(Transaction t) {
    for (String var : lockTable.keySet()) {
      for (Lock l : lockTable.get(var) {
        if (l.transactionId.equals(t.tId)) {
          lockTable.get(var).remove(lock);
          break;
        }
      }
    }
  }

  public Variable getVariable(String id) {
    if (!variableList.containsKey(id)) {
      return null;
    }

    return variableList.get(id);
  }
	
}