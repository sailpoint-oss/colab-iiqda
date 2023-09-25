package sailpoint.iiqda.validation;

import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public interface IArtifactRootElement {

  public ArtifactType getType();
  
  public void setStartChar(int characterOffset);
  public int getStartChar();
  
  public void setStartLine(int lineNumber);
  public int getStartLine();
  public void setElementEndChar(int elementEnd);
  public int getElementEndChar();
  public boolean hasReferencedRules();
  public List<ReferenceElement> getReferencedRules();

  public String getName();
  
}
