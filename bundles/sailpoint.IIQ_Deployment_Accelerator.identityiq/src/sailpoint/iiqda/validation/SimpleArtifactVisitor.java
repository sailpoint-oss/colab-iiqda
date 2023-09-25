package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class SimpleArtifactVisitor implements IResourceVisitor {

  private boolean found=false;
  private String artifactName;
  private String artifactType;
  private String[] excludedDirs;
  
  private List<String> erroredFiles;

  public SimpleArtifactVisitor(String type, String name, String[] excludedDirs) {
    this.artifactType=type;
    this.artifactName=name;
    this.excludedDirs=excludedDirs;
    if(this.excludedDirs==null) this.excludedDirs=new String[0];
    System.out.println("New SimpleArtifactVisitor");
    erroredFiles=new ArrayList<String>();
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

    if (erroredFiles.contains(resource.getFullPath().toString())) {
      System.out.println("Skipping previously invalid file "+resource.getFullPath().toString());
      return true;
    }
    
    SimpleArtifactParser p=new SimpleArtifactParser(file.getContents());
    try {
      p.parse();
  		Map<String,List<String>> artifacts=p.getArtifactList();
  		List<String> artType=artifacts.get(artifactType);  		
  		if(artType!=null) {
  		  if(artType.contains(artifactName)) {
  		    found=true;
  		  }
  		}
    } catch (XMLArtifactParserException e) {
      IIQPlugin.logException("SimpleArtifactVisitor: Something went wrong parsing "+resource.getFullPath().toString(), e);
      erroredFiles.add(resource.getFullPath().toString());
    }
    return !found;
  }

  public boolean found() {
    return found;
  }
}
