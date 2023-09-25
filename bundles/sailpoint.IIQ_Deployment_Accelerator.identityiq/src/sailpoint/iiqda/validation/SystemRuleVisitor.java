package sailpoint.iiqda.validation;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class SystemRuleVisitor implements IResourceVisitor {

  private boolean found=false;
  private String artifactName;
  private String ruleContents;

  public SystemRuleVisitor(String name) {
    this.artifactName=name;
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

    SystemRuleParser p=new SystemRuleParser(file.getContents());
    try {
      p.parse();
  		Map<String,String> artifacts=p.getRuleList();
  		String rule=artifacts.get(artifactName);  		
  		if(rule!=null) {
  		  ruleContents=rule;
  		  found=true;
  		}
    } catch (XMLArtifactParserException e) {
      IIQPlugin.logException("SimpleArtifactVisitor: Something went wrong parsing "+resource.getFullPath().toString(), e);
    }
    return !found;
  }

  public boolean found() {
    return found;
  }
}
