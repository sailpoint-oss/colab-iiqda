package sailpoint.iiqda.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.comparison.CompareLiveArtifactEditorInput;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.editors.ArtifactEditor;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class CompareWithTargetEnvironmentCommandHandler extends AbstractHandler {

  private static final boolean DEBUG_COMPARE = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/CompareHandler"));

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
        .getActivePage().getSelection();
    IWorkbench wb = PlatformUI.getWorkbench();
    IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
    Shell shell = win.getShell();

    IProject project;
    IFile theFile;
    ObjectDefinition art=null;
    
    // get the project that is selected    
    IWorkbenchPage page=win.getActivePage();
    IWorkbenchPart part=page.getActivePart();
    String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");

    if(part instanceof ArtifactEditor) {
    	

      try {
      	IFile file = ((IFileEditorInput)((ArtifactEditor) part).getEditorInput()).getFile();
				project=file.getProject();
        IIQRESTClient client=new IIQRESTClient(project, environment);
        art=getTypeAndName(file);       
        
        CompareLiveArtifactEditorInput cei=new CompareLiveArtifactEditorInput((ArtifactEditor)part,
        		client, art.getObjectType(), art.getObjectName());
        CompareUI.openCompareEditor(cei);
      } catch (CoreException | XMLArtifactParserException ce) {
        throw new ExecutionException("Exception "+ce);
      }
      return null;      
    }
    if (selection == null) {
      MessageDialog.openError(
          null,
          "IIQ Plugin",
          "Null Selection!");
      return null;      
    }
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection strucSelection = (IStructuredSelection) selection;
      if(strucSelection.size()>1) {
        System.out
        .println("ResourceImportTaskHandler.execute: can't work on multiple objects");
        MessageDialog.openError(
            shell,
            "IIQ Plugin",
            "Can't work on multiple objects!");
        return null;      
      }
      Object element = strucSelection.getFirstElement();
      if( !(element instanceof IFile) ) { 
        MessageDialog.openError(
            shell,
            "IIQ Plugin",
            "Can't refresh non-artifact");
        return null;
      }
      theFile=(IFile) element;
      try {
        art=getTypeAndName(theFile);        
      } catch (XMLArtifactParserException xe) {
        MessageDialog.openError(
            shell,
            "IIQ Plugin",
            "Can't find artifact type and/or name");
        return null;      
      }
      
      project=theFile.getProject();
    } else {
      MessageDialog.openError(
          shell,
          "IIQ Plugin",
          "Unknown selection type "+selection.getClass().getName());
      return null;      
    }

    // Get List of possible exportable classes



    try {
      IIQRESTClient client=new IIQRESTClient(project, environment);
      art=getTypeAndName(theFile);
      CompareLiveArtifactEditorInput cei=new CompareLiveArtifactEditorInput(theFile,
      		client, art.getObjectType(), art.getObjectName());
      CompareUI.openCompareEditor(cei);
    } catch (CoreException e) {
      IIQPlugin.logException("TODO: CoreException", e);
    } catch (XMLArtifactParserException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
    
    return null;

  }

  private ObjectDefinition getTypeAndName(IFile artifactFile) throws XMLArtifactParserException {
    
    try {
	    return getTypeAndName(artifactFile.getContents());
    } catch (CoreException e) {
	    e.printStackTrace();
    }
    
    return null;
    
  }
  
  private ObjectDefinition getTypeAndName(InputStream inStream) throws XMLArtifactParserException {

  	XMLEventReader reader=null;
  	
  	ObjectDefinition def=null;

  	try {
      XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
      fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

      reader = fac.createXMLEventReader( new InputStreamReader(inStream) );

      boolean done=false;
      while(reader.hasNext() && !done) {
        XMLEvent event = reader.nextEvent();
        if(event.isStartElement()) {
          StartElement startElement = event.asStartElement();
          if (DEBUG_COMPARE) {
            IIQPlugin.logDebug(startElement.toString());
          }
          String elName=startElement.getName().getLocalPart();
          if (!"sailpoint".equals(elName)) {
            Attribute name=startElement.getAttributeByName(new QName("name"));
            def=new ObjectDefinition(elName, name.getValue(), null);
            done=true;
          }
        }       
      }
    } catch (XMLStreamException e) {
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
    if(def==null) throw new XMLArtifactParserException();
    return def;
  }
  
}
