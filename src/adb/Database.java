package adb;

import java.util.*;
import java.io.*;

public class DataBase {
  public static void main(String[] args) {
    Simulator db = new Simulator(10, 20);
    if (args.length < 1) {
      System.out.println("Error: no valid input file");
      System.exit(1);
    }
    db.process(args[0]);  
   }
}