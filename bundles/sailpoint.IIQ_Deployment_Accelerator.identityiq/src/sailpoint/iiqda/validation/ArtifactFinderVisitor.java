package sailpoint.iiqda.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class ArtifactFinderVisitor implements IResourceVisitor {

  private boolean found=false;
  private String artifactName;
	private IFile theFile=null;

  public ArtifactFinderVisitor(String name) {
    this.artifactName=name;
  }
  
  public IFile getFile() {
  	return theFile;
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
      IArtifactRootElement art=p.getArtifact(artifactName);
      if(art!=null) {
        theFile=file;
        found=true;
      }
    } catch (XMLArtifactParserException xe) {
      IIQPlugin.logException("Execption finding "+artifactName, xe);
      // Something went wrong looking at this file. Doesn't mean we shouldn't look at other files..
    }
    return !found;
  }

  public boolean found() {
    return found;
  }
}

