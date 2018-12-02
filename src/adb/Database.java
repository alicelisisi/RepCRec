import java.util.*;
import java.io.*;

public class DataBase {
  public static void main(String[] args) {
    Simulator db = new Simulator(20, 10);
    if (args.length < 1) {
      System.out.println("Error: no valid input file");
      System.exit(1);
    }
    db.run(args[0]);  
   }
}