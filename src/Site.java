import java.util.*;
import java.io.*;

public class Site {
  final int siteId;
  final Map<String, Variable> variableList;
  
  
  
  
  
  
  
  
  public Site(int id) {
    this.siteId = id;
    this.variableList = new HashMap<>();
  }
  
  public void addVariable(Variable v) {
    if (!variableList.containsKey(v.vId)) {
      variableList.put(v.vId, v);
    }
  }
	
}