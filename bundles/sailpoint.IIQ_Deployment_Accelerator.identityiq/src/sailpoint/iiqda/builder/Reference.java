package sailpoint.iiqda.builder;

public class Reference {

  private String clazz;
  private String name;
  private int start;
  private int end;
  private int line;
  private boolean includable;

  public Reference(String refClass, String refName, int refStart, int refEnd, int refLine, boolean includable) {
    this.clazz=refClass;
    this.name=refName;
    this.start=refStart;
    this.end=refEnd;
    this.line=refLine;
    this.includable=includable;
  }

  public String getReferenceClass() {
    return clazz;
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

  public boolean isIncludable() {
    return includable;
  }
}
