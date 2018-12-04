package adb;

import java.util.*;


public class Variable {
  public String vId;
  public int value;
  public int lastCommitted;
  public Map<Integer, Integer> commits = new HashMap<>();
  public boolean canRead;


  public Variable (String id) {
    this.vId = id;
    this.value = getInitial(id);
    this.commits.put(0, value);
    this.lastCommitted = value;
    this.canRead = true;
  }

  public int readOnly(int timeStamp) {
    List<Integer> times = new ArrayList<>(commits.keySet());
    Collections.sort(times);
    int size = times.size();
    if (timeStamp > times.get(size - 1)) {
      return commits.get(times.get(size - 1));
    }

    int start = 0;
    int end = size - 1;
    while (start + 1 < end) {
      int mid = (start + end) / 2;
      if (timeStamp <= times.get(mid)) {
        mid = end;
      } else {
        mid = start;
      }
    }

    return commits.get(times.get(start));
  }

  public int getInitial(String id) {
    String s = id.replace("x", "");
    try {
      int i = Integer.valueOf(s);
      return 10 * i;
    } catch (NumberFormatException e) {
      System.out.println("Error: invalid variable name " + id + ". Expected example: x1");
      e.printStackTrace();
    }

    return -1;
  }

  public int read() {
    return value;
  }

  public void write(int val) {
    value = val;
  }

  public void recoverVariable() {
    canRead = true;
  }

  public void revert() {
    value = readLastCommitted();
  }

  public int readLastCommitted() {
    return lastCommitted;
  }

  public int commit(int timeStamp) {
  	commits.put(timeStamp, value);
  	lastCommitted = value;
  	return value;
  }

}
