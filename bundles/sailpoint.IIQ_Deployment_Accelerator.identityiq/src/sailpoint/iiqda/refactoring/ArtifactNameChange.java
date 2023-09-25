package sailpoint.iiqda.refactoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.IIQDAConstants;

public class ArtifactNameChange extends Change{

  private static final boolean DEBUG_CHANGE = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQDAConstants.PLUGIN_ID+"/debug/ArtifactNameChange"));

  private IFile artifactFile;
  private String newname;

  public ArtifactNameChange(IFile element, RenameArguments arguments) {
    this.artifactFile=element;
    String newFilename=arguments.getNewName();
    // Note: this next line presupposes that the filename ends in .xml - this should be ensured
    // by the <enablement> in plugin.xml only enabling this Participant/Change for Artifacts
    String camel=newFilename.substring(newFilename.indexOf('-')+1, newFilename.length()-4);
    this.newname=deCamel(camel);
    
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return "rename Artifact to "+newname;
  }

  @Override
  public void initializeValidationData(IProgressMonitor pm) {
    // TODO Auto-generated method stub

  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
  OperationCanceledException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    // The change will change the 'name' attribute of the root element
    int loc=findNameAttributeLocation();
    replaceNameAttribute(loc, newname);
    //TODO: Create an undo change
    return null;
  }

  @Override
  public Object getModifiedElement() {
    // TODO Auto-generated method stub
    return null;
  }

  private String deCamel(String camel) {
    StringBuilder sb=new StringBuilder();
    sb.append(Character.toUpperCase(camel.charAt(0)));
    boolean lastWasUpper=Character.isUpperCase(camel.charAt(0));
    for(int i=1;i<camel.length();i++) {
      char c=camel.charAt(i);
      boolean isUpper = Character.isUpperCase(c);
      if(isUpper && !lastWasUpper) {
        sb.append(' ');
      }
      sb.append(c);
      lastWasUpper=isUpper;
    }
    return sb.toString();
  }


  // Do a STaX run through the file, find the first non <sailpoint> element,
  // return its start location. we then need to use thqt as a starting point when
  // going through the stream to find 'name='
  private int findNameAttributeLocation() {
    int location=-1;
    InputStream inStream=null;
    XMLEventReader reader=null;

    try {
      inStream=artifactFile.getContents();
      XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
      fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

      reader = fac.createXMLEventReader( new InputStreamReader(inStream) );

      Stack<Location> locations=new Stack<Location>();
      Stack<String> elements=new Stack<String>();
      boolean done=false;
      while(reader.hasNext() && !done) {
        XMLEvent event = reader.nextEvent();
        if(event.isStartElement()) {
          StartElement startElement = event.asStartElement();
          if (DEBUG_CHANGE) {
            IIQPlugin.logDebug(startElement.toString());
          }
          if (!"sailpoint".equals(startElement.getName())) {
            Location loc=event.getLocation();
            location=loc.getCharacterOffset();
            done=true;
          }
        }       
      }
    } catch (XMLStreamException e) {
      e.printStackTrace();
    } catch (CoreException e) {
      e.printStackTrace();
    } finally {

      try {
        reader.close();
      } catch (XMLStreamException e) {
        // Something went wrong. I don't care
      }
      try {
        inStream.close();
      } catch (IOException e) {
        // Something went wrong. I don't care
      }

    }
    return location;
  }

  private void replaceNameAttribute(int loc, String newname) throws CoreException {
    StringBuilder contents=CoreUtils.readFile(artifactFile);
    int idx=contents.indexOf("name", loc);
    int firstquoteIdx=contents.indexOf("\"", idx);
    int secondquoteIdx=contents.indexOf("\"", firstquoteIdx+1);
    contents.replace(firstquoteIdx+1, secondquoteIdx, newname);
    artifactFile.setContents(new ByteArrayInputStream(contents.toString().getBytes()), IFile.KEEP_HISTORY, null);
  }
}
