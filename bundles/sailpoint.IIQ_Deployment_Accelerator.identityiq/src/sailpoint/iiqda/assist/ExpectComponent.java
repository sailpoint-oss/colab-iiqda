package sailpoint.iiqda.assist;

public class ExpectComponent {

  private String fqClass;
  private int start;
  private int length;

  public ExpectComponent(String fqClass, int start, int length) {
    this.fqClass=fqClass;
    this.start=start;
    this.length=length;
    
  }

  public String getFQClass() {
    return fqClass;
  }

  public int getStart() {
    return start;
  }

  public int getLength() {
    return length;
  }
  
  
}
