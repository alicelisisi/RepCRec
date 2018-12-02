package adb;

import java.util.*;
import java.io.*;



public class Simulator {
  static int numOfVariable;
  static int numOfSite;
  int timeStamp = 0;
  final List<Site> siteList;
  final TransactionManager tm;
  private boolean verbose = true;
  
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
        timeStamp++;
        String[] events = line.split(";");
        for (String event : events) {
          if (event == null || event.trim().isEmpty() || event.startsWith("//")) {
            continue;
          }
          if (event.contains("//")) {
            event = event.substring(0, event.indexOf("//"));
          }
          process(event);
        }
      }
      br.close();
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
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
    }
  }
  
  public void printVerbose(String message) {
    if (verbose) {
      System.out.println("# " + message); 
    }
  }
  
  
	
}