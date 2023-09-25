package sailpoint.iiqda.builder;

public class RuleReference {

  private String name;
  private int start;
  private int end;
  private int line;

  public RuleReference(String refName, int refStart, int refEnd, int refLine) {
    this.name=refName;
    this.start=refStart;
    this.end=refEnd;
    this.line=refLine;    
  }

  public String getName() {
    return name;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getLine() {
    return line;
  }

}
