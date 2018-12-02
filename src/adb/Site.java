package adb;

import java.util.*;
import java.io.*;

public class Site {
  final int siteId;
  final Map<String, Variable> variableList;
  public boolean isFailed;
  final Map<String, List<Lock>> lockTable;
  
  
  
  
  
  
  
  
  public Site(int id) {
    this.siteId = id;
    this.variableList = new HashMap<>();
    this.isFailed = false;
    this.lockTable = new HashMap<>();
  }
  
  public void addVariable(Variable v) {
    if (!variableList.containsKey(v.vId)) {
      variableList.put(v.vId, v);
    }
  }

  public void siteWrite(String vId, int val) {

  }
	
}