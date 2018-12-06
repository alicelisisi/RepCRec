package adb;

import java.util.*;
import java.io.*;

public class Site {
  public int siteId;
  public Map<String, Variable> variableList;
  public boolean isFailed;
  public Map<String, List<Lock>> lockTable;
  public int failedTime;
  public boolean canRead;
  
  
  public Site(int id) {
    this.siteId = id;
    this.variableList = new HashMap<>();
    this.isFailed = false;
    this.lockTable = new HashMap<>();
    this.failedTime = -1;
    this.canRead = true;
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
      for (Lock l : lockTable.get(var)){
        if (l.transactionId.equals(t.tId)) {
          lockTable.get(var).remove(l);
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

  public int readVariable(String vId, boolean committed) {
    Variable v = getVariable(vId);
    
    if (committed) {
      return v.readLastCommitted();
    }

    return v.read();
  }

  public int readVariable(String vId, int timeStamp) {
    Variable v = getVariable(vId);
    return v.readOnly(timeStamp);
  }

  public List<String> getAllVariableIds() {
    List<String> result = new ArrayList<>();
    for (String v : variableList.keySet()) {
      result.add(v);
    }
    Collections.sort(result, new Comparator<String>() {
      @Override
      public int compare(String v1, String v2) {
        return Integer.parseInt(v1) - Integer.parseInt(v2);
      }

    });
    return result;
  }

  public void fail(int timeStamp) {
    isFailed = true;
    failedTime = failedTime;
    lockTable.clear();
  }

  public void recover() {
    isFailed = false; 
    canRead = false;
    for (Map.Entry<String, Variable> e : variableList.entrySet()) {
      String vId = e.getKey().substring(1);
      int id = Integer.valueOf(vId);
      if (id % 2 == 0) {
        e.getValue().blockRead();
      }
    }
  }


	
}