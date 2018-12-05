package adb;

import java.util.*;
import java.io.*;



public class Simulator {
  public static int numOfVariable;
  public static int numOfSite;
  public int timeStamp = 0;
  public static List<Site> siteList;
  public static TransactionManager tm;
  public boolean verbose = true;
  
  public Simulator(int numS, int numV) {
    this.numOfSite = numS;
    this.numOfVariable = numV;
    this.tm = new TransactionManager(this);
    this.siteList = new ArrayList<Site>();
   
    for (int i = 1; i <= numOfSite; i++) {
      siteList.add(new Site(i));  
    }
    setUpVariable();
  }
  
  public void setUpVariable() {
    for (int i = 1; i <= numOfVariable; i++) {
      String vId = "x" + Integer.toString(i);
      if (i % 2 == 1) {
        siteList.get(i % numOfSite).addVariable(new Variable(vId));
      } else {
        for (Site s : siteList) {
          s.addVariable(new Variable(vId));
        }
      }
    }
  }
  
  public void process(String fileName) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      printVerbose("Read input from " + fileName);
      String line;
      while ((line = br.readLine())!= null) {
        //System.out.println(line);
        if (line.startsWith("//") || line.equals("") ) {
          continue;
        }
        timeStamp++;

        runCommand(line); 

      }
      br.close();
    } catch (Exception e) {
      System.out.println("Error: " + timeStamp );
    }  
  }
  
  public void runCommand(String command) {
    String line = command.replaceAll("\\s+", "");
    printVerbose(line);
    if (line.startsWith("begin")) {
      String tId = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
      tm.begin(tId, timeStamp, line.startsWith("beginRO"));
    } else if (line.startsWith("W")) {
      int first = line.indexOf(",");
      String tId = line.substring(line.indexOf("(") + 1, first);
      int second = line.indexOf(",", first + 1);
      String vId = line.substring(first + 1, second);
      int val = Integer.parseInt(line.substring(second + 1, line.indexOf(")")));
      tm.write(tId, vId, val);
    } else if (line.startsWith("end")) {
      String tId = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
      tm.commitTransaction(tId, timeStamp);
    } else if (line.startsWith("dump")) {
      if (line.contains("()")) {
        dump();
      } else if (line.contains("x")) {
        String vId = line.substring(line.indexOf("x") + 1, line.indexOf(")"));
        showVariable(vId);
      } else {
        String sId = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
        dumpSite(Integer.parseInt(sId));
      }
      
    } else if (line.startsWith("fail")) {
      int sId = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
      failSite(sId, timeStamp);
    } else if (line.startsWith("recover")) {
      int sId = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
      recoverSite(sId);  
    } else if (line.startsWith("R")) {
      int split = line.indexOf(",");
      String tId = line.substring(line.indexOf("(") + 1, split);
      String vId = line.substring(split + 1, line.indexOf(")"));
      tm.read(tId, vId);
    }
  }
  
  public void printVerbose(String message) {
    if (verbose) {
      System.out.println("# " + message); 
    }
  }

  public void dump() {
    for (int i = 1; i <= numOfVariable; i++) {
      showVariable("x" + i);
    }
  }

  public void dumpSite(int sId) {
    Site s = siteList.get(sId - 1);
    StringBuilder sb = new StringBuilder("Site " + sId + ":\n");
    List<String> vIds = s.getAllVariableIds();
    for (String id : vIds) {
      int value = s.readVariable(id, true);
      sb.append(id + ": " + value  + "\n");
    }

    System.out.println(sb.toString());
  }

  public void failSite(int sId, int timeStamp) {
    System.out.println("site " + sId + " fails");

    Site s = siteList.get(sId - 1);
    Set<String> set = new HashSet<>();
    for (Map.Entry<String, List<Lock>> locks : s.lockTable.entrySet()) {
      for (Lock l : locks.getValue()) {
        set.add(l.transactionId);
      }
    }

    for (String id : set) {
      System.out.println("abort " + id + " because site " + sId + " fails");
      tm.abortTransaction(id, true);
    }

    s.fail(timeStamp);
  }

  public void recoverSite(int sId) {
    Site s = siteList.get(sId - 1);
    s.recover();
    System.out.println("site " + sId + " recovers");
    tm.runNextWaiting();
  }

  public void showVariable(String vId) {
    
    if (isOddVariable(vId)) {
      Site s = getSite(vId);
      int value = s.readVariable(vId, true);
      System.out.println(vId + ": " + value + " at site " + s.siteId);
    } else {
      showEvenVariable(vId);
    }

  }

  public void showEvenVariable(String vId) {
    Map<Integer, List<Integer>> map = new HashMap<>();
    for (Site s : siteList) {
      int value = s.readVariable(vId, true);
      if (!map.containsKey(value)) {
        map.put(value, new ArrayList<>());
      }
      map.get(value).add(s.siteId);
    }

    StringBuffer sb;
    for (int v : map.keySet()) {
      if (map.size() == 1) {
        System.out.println(vId + ": " + v + " at all sites");
      } else {
        sb = new StringBuffer(vId + ": ");
        sb.append(v + " at site ");
        for (int s : map.get(v)) {
          sb.append(s + " ");
        }
        System.out.println(sb.toString().trim());
      }
    }
  }

  public Site getSite(String vId) {
    int v = Integer.valueOf(vId.substring(1));
    Site s = siteList.get(v % numOfSite);
    return s;

  }

  public boolean isOddVariable(String vId) {
    int v = Integer.valueOf(vId.substring(1));
    if (v % 2 == 1) {
      return true;
    }
    return false;  
  }
  
  
	
}