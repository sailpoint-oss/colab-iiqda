package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArtifactRootElement extends AbstractArtifactElement implements IArtifactElement, IArtifactRootElement {

  protected int startChar;
  protected int startLine;
  protected int endChar;
  protected List<ReferenceElement> references;  
  protected String name;

  public AbstractArtifactRootElement(String name) {
    this.name=name;
    references=new ArrayList<ReferenceElement>();
  }
  
  @Override
  public void setStartChar(int characterOffset) {
    this.startChar=characterOffset;
  }

  @Override
  public int getStartChar() {
    return startChar;
  }

  @Override
  public void setStartLine(int lineNumber) {
    this.startLine=lineNumber;
  }

  @Override
  public int getStartLine() {
    return startLine;
  }

  @Override
  public void setElementEndChar(int elementEnd) {
    this.endChar=elementEnd;
  }

  @Override
  public int getElementEndChar() {
    return endChar;
  }
  
  @Override
  public boolean hasReferencedRules() {
    // TODO Auto-generated method stub
    return getReferencedRules().size()>0;
  }

  @Override
  public List<ReferenceElement> getReferencedRules() {
    List<ReferenceElement> rules=new ArrayList<ReferenceElement>();
    for(IArtifactElement re: children) {
      if (re instanceof ReferenceElement) {
        String clazzName = ((ReferenceElement)re).getClassName();
        if ( clazzName!=null && clazzName.equals("sailpoint.object.Rule") ) {
          rules.add((ReferenceElement)re);
        }
      }
    }
    return rules;
  }

  @Override
  public String getName() {
    return name;
  }

}
