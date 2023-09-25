package sailpoint.iiqda.validation;

import javax.xml.stream.XMLStreamReader;

public class ReferenceElement extends AbstractArtifactElement {

  private boolean includable=false;
  private int refStart;
  private int refEnd;
  private int refLine;
  private String refName;
  private String refClass;
  
  public ReferenceElement(XMLStreamReader stream) {
    if("sailpoint.object.Rule".equals(stream.getAttributeValue(null, "class")) ) {
      includable=true;
    }
    refStart=stream.getLocation().getCharacterOffset();
    refLine=stream.getLocation().getLineNumber();
    refName=stream.getAttributeValue(null, "name");
    refClass=stream.getAttributeValue(null, "class");
  }

  public boolean isIncludable() {
    return includable;
  }

  public int getStartChar() {
    return refStart;
  }

  public int getLine() {
    return refLine;
  }

  public String getName() {
    return refName;
  }

  public String getClassName() {
    return refClass;
  }

  public void setEndChar(int end) {
    this.refEnd=end;
  }
  
  public int getEndChar() {
    return refEnd;
  }
  
  
}
