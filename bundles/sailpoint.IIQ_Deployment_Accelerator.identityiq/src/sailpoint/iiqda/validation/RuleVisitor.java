package sailpoint.iiqda.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class RuleVisitor implements IResourceVisitor {

  private boolean found=false;
  private String ruleContents;
  private String ruleName;

  public RuleVisitor(String name) {
    this.ruleName=name;
  }

  public String getContents() {
    return ruleContents;
  }

  @Override
  public boolean visit(IResource resource) throws CoreException {
    // Shortcut the process if we already found it.
    if(found) return false;

    // If it's not a file, keep searching
    if(!(resource instanceof IFile)) return true;

    IFile file = (IFile)resource;
    if(!file.getName().endsWith(".xml")) {
      return true; // keep looking. Ignore XML File
    }

    try {
      XMLIFileArtifactParser p=new XMLIFileArtifactParser(file);
      p.parse(true);
      IArtifactRootElement theRule=p.getArtifact(ruleName);
      if(theRule!=null && theRule.getType()==ArtifactType.RULE) {
        p=new XMLIFileArtifactParser(file);
        p.parse(false);
        RuleElement ruleWithContents=(RuleElement)p.getArtifact(ruleName);
        ruleContents=ruleWithContents.getSource().getSource();
        found=true;
      }
    } catch (XMLArtifactParserException xe) {
      IIQPlugin.logException("Execption finding "+ruleName, xe);
      // Something went wrong looking at this file. Doesn't mean we shouldn't look at other files..
    }
    return !found;
  }
}

