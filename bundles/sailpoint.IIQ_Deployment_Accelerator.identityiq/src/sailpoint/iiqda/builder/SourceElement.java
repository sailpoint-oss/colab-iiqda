package sailpoint.iiqda.builder;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;
import sailpoint.iiqda.validation.IArtifactElement;
import sailpoint.iiqda.validation.IArtifactRootElement;
import sailpoint.iiqda.validation.IRuleBasedScript;
import sailpoint.iiqda.validation.IScriptContainerElement;
import sailpoint.iiqda.validation.ReferenceElement;

public class SourceElement {

  private int startChar;
  private int startLine;
  private String sourcecode;
  private boolean isCDATA;
  private IArtifactElement parent;

  public SourceElement(int startChar, int startLine, String source, boolean isCDATA, IArtifactElement parent) {
    this.startChar=startChar;
    this.startLine=startLine;
    this.sourcecode=source;
    this.isCDATA=isCDATA;
    this.parent=parent;
  }

  public int getStartChar() {
    return startChar+(isCDATA?9:0); // adjust for <![CDATA[
  }

  public int getStartLine() {
    return startLine;
  }

  public int getLength() {
    return (sourcecode!=null?sourcecode.length():0);
  }

  public String getSource() {
    return sourcecode;
  }

  public boolean isCDATA() {
    return isCDATA;
  }

  public boolean needsReturn() {
    return getReturnType()!=null;
  }

  public String getReturnType() {
    return ((IScriptContainerElement)parent).getReturnType();
  }
  
  public String getRuleType() {
    if (parent instanceof IRuleBasedScript) {
      return ((IRuleBasedScript)parent).getRuleType();
    }
    return null;
  }

  public boolean matches(int offs, int len) {

    // see if the offset and length match this SourceElement
    // if it is a CDATA element, the length will be 12 more (for '<![CDATA[]]>')

    if(!isCDATA) {
      return (offs==startChar && len==sourcecode.length());
    }	else {
      // se.getStartChar() returns the start of the actual code (after the CDATA tag) since we add that
      // here offs is the actual startChar; we just need to add 12 to len
      return(offs==startChar && len==sourcecode.length()+12);
    }
  }
  
  public boolean inDirtyRegion(int offs, int len) {
    return (startChar>=offs && startChar+sourcecode.length()<=offs+len);
  }

  public int getRelativeIndex(int absoluteChar) {
    // This method takes a char index from the whole document
    // and makes it relative to this Source element
    int relative = absoluteChar-startChar;
    if(isCDATA) relative-=9;
    return relative;
  }

  public void setParent(IArtifactElement peek) {
    this.parent=peek;
  }
  
  public IArtifactElement getParent() {
    return parent;
  }

  public boolean scriptReferencesRules() {
    List<ReferenceElement> referencedRules = getReferencedRules();
    return (referencedRules!=null && referencedRules.size()!=0);
  }

  public List<ReferenceElement> getReferencedRules() {
    IArtifactRootElement root=getRootElement();
    if (root.hasReferencedRules()) {
      return root.getReferencedRules();
    }
    return new ArrayList<ReferenceElement>();
  }

  public IArtifactRootElement getRootElement() {
    IArtifactElement ae=parent;
    while(ae.getParent()!=null) {
      ae=ae.getParent();
    }
    return (IArtifactRootElement)ae;
  }
  
  public ArtifactType getRootType() {
    IArtifactRootElement rootEl=getRootElement();
    return rootEl.getType();
  }
  
}
