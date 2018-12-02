package adb;

import java.util.*;


public class Variable {
  public final String vId;
  private int value;
  private int lastCommitted;


  public Variable (String id) {
    this.vId = id;
  }

}
