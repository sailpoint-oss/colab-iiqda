package sailpoint.iiqda.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class SimpleRuleListVisitor implements IResourceVisitor {

  private String[] excludedDirs; 

  private Map<String,String> allTheRules;
  
  public SimpleRuleListVisitor(String[] excludedDirs) {
    this.excludedDirs=excludedDirs;
    if(this.excludedDirs==null) this.excludedDirs=new String[0];
    allTheRules=new HashMap<String,String>();
    }
  
  public Map<String,String> allTheRules() {
    return allTheRules;
  }

  @Override
  public boolean visit(IResource resource) throws CoreException {

    // If it's not a file, keep searching
    if(!(resource instanceof IFile)) return true;

    IFile file = (IFile)resource;
    if(!file.getName().endsWith(".xml") || file.getName().endsWith(".hbm.xml")) {
      return true; // keep looking. Ignore XML File
    }

    // check if the current resource is (or is in) one of the excluded directories
    for(String excluded: excludedDirs) {
      if(resource.getProjectRelativePath().toString().startsWith(excluded)) {
        return true;
      }
    }
    
    SystemRuleParser p=new SystemRuleParser(file.getContents());
    try {
      p.parse();
  		Map<String,String> artifacts=p.getRuleList();
  		for(String key: artifacts.keySet()) {
  		  allTheRules.put(key, artifacts.get(key));
  		}
    } catch (XMLArtifactParserException e) {
      IIQPlugin.logException("SimpleArtifactVisitor: Something went wrong parsing "+resource.getFullPath().toString(), e);
    }
    return true;
  }

  public void clearExcludedDirs() {
    this.excludedDirs=new String[0];
  }

}
