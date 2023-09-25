package sailpoint.iiqda.comparison;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.editors.ArtifactEditor;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;

public class CompareLiveArtifactEditorInput extends CompareEditorInput {

	private ArtifactEditor editor;
	private IIQRESTClient client;
	private String type;
	private String name;
	private IFile file;
	
	public CompareLiveArtifactEditorInput(ArtifactEditor part, IIQRESTClient client, String type, String name) {
	  super(new CompareConfiguration());
	  this.editor=part;
	  this.file=null;
	  init(client, type, name);
	}

	public CompareLiveArtifactEditorInput(IFile theFile, IIQRESTClient client,
      String objectType, String objectName) {
		super(new CompareConfiguration());
		this.editor=null;
		this.file=theFile;
		init(client, objectType, objectName);
  }

	private void init(IIQRESTClient client, String type, String name) {
	  this.client=client;
	  this.type=type;
	  this.name=name;
	  getCompareConfiguration().setLeftEditable(true);
	  getCompareConfiguration().setRightEditable(false);
	  getCompareConfiguration().setProperty(CompareConfiguration.IGNORE_WHITESPACE, true);
	}
	
	@Override
	protected Object prepareInput(IProgressMonitor monitor)
	    throws InvocationTargetException, InterruptedException {
		try {
			CompareItem left = null;
			if(file==null) {
				String content = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
				left = new ArtifactCompareItem("Local.xml", content);
			} else {
				left = new ArtifactCompareItem("Local.xml", file);
			}
  		String object = client.getObject(type, name);
  		boolean bDoSubstOnCmp=false;
  		try {
        try {
          bDoSubstOnCmp=Boolean.parseBoolean(client.getProject().getPersistentProperty(
              new QualifiedName("", IIQPreferenceConstants.P_SUBSTITUTION_ON_COMPARE)));
        } catch (NumberFormatException nfe) {}
        if (bDoSubstOnCmp) {
          Reader rdr=CoreUtils.doReverseSubstitution(new StringReader(object), client.getProject());
          StringBuilder bldr=new StringBuilder();
          char[] buf=new char[1024];
          int bytesread=0;
          while( (bytesread=rdr.read(buf))!=-1 ) {
            bldr.append(buf, 0, bytesread);
          }
          rdr.close();
          object=bldr.toString();
        }
  		} catch (CoreException ce) {
  		  
  		} catch (IOException ioe) {
  		  
  		}
      CompareItem right=
				new ArtifactCompareItem("Remote.xml", object);
		return new DiffNode(left, right);
		} catch (ConnectionException ce) {
			throw new InvocationTargetException(ce);
		}
	}

	private class ArtifactCompareItem extends CompareItem {

    public ArtifactCompareItem(String name, IFile file) {
      super(name, file);
    }
  
    public ArtifactCompareItem(String name, String contents) {
      super(name, contents);
    }
    
    public String getType() {return "sailpoint.IIQ_Deployment_Accelerator.content.Artifact";}
	}
	
}
