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
      System.out.println();
      return;
    }
  }
  
	
	
}