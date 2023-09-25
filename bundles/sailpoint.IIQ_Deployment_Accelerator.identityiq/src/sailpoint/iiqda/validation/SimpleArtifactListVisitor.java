package sailpoint.iiqda.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

// This visitor is designed just to iterate through all the xml files
// and make a huge map of <type, List(of artifacts)>
// Should speed up the validation process

public class SimpleArtifactListVisitor implements IResourceVisitor {

  private boolean found=false;
  private String[] excludedDirs; 

  private Map<String,List<String>> allTheArtifacts;
  
  public SimpleArtifactListVisitor(String[] excludedDirs) {
    this.excludedDirs=excludedDirs;
    if(this.excludedDirs==null) this.excludedDirs=new String[0];
    allTheArtifacts=new HashMap<String,List<String>>();
  }
  
  public void clearExcludedDirs() {
    this.excludedDirs=new String[0];
  }

  @Override
  public boolean visit(IResource resource) throws CoreException {
    // Shortcut the process if we already found it.
    if(found) return false;
    
    // check if the current resource is (or is in) one of the excluded directories
    for(String excluded: excludedDirs) {
      if(resource.getProjectRelativePath().toString().startsWith(excluded)) {
        return true;
      }
    }

    // If it's not a file, keep searching
    if(!(resource instanceof IFile)) return true;

    IFile file = (IFile)resource;
    if(!file.getName().endsWith(".xml") || file.getName().endsWith(".hbm.xml")) {
      return true; // keep looking. Ignore XML File
    }

    SimpleArtifactParser p=new SimpleArtifactParser(file.getContents(), allTheArtifacts);
    try {
      p.parse();
    } catch (XMLArtifactParserException e) {
      IIQPlugin.logException("SimpleArtifactVisitor: Something went wrong parsing "+resource.getFullPath().toString(), e);
    }
    return true; // keep going
  }

  public Map<String,List<String>> allTheArtifacts() {
    return allTheArtifacts;
  }
}
