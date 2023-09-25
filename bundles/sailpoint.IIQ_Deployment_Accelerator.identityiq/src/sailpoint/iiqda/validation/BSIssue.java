package sailpoint.iiqda.validation;

import java.util.HashMap;
import java.util.Map;

public class BSIssue {

  enum IssueSeverity {
    ERR,
    WARN,
    TODO;
  }
  
  private String markerType;
  private IssueSeverity severity;
  private int start;
  private int length;
  private int line;
  private String message;
  
  private Map<String,Object> attributes;
  
  public BSIssue(String markerType, IssueSeverity sev, int start, int length, int line, String message) {
    this.markerType=markerType;
    this.severity=sev;
    this.start = start;
    this.length = length;
    this.line = line;
    this.message = message;
    this.attributes=new HashMap<String,Object>();
  }
  
  public String getType() {
    return markerType;
  }

  public IssueSeverity getSeverity() {
    return severity;
  }

  public int getStart() {
    return start;
  }

  public int getLength() {
    return length;
  }

  public int getLine() {
    return line;
  }

  public String getMessage() {
    return message;
  }
  
  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }
  
  public Map<String,Object> getAttributes() {
    return attributes;
  }
}
