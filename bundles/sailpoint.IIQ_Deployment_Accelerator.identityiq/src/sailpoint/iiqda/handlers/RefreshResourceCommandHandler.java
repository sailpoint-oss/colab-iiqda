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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.ArtifactHelper;
import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class RefreshResourceCommandHandler extends AbstractHandler {

  private static final boolean DEBUG_REFRESH = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/RefreshHandler"));

  
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
        if(DEBUG_REFRESH) {
          IIQPlugin.logDebug("ResourceImportTaskHandler.execute: can't work on multiple objects");
        }
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

    IIQRESTClient client;
    // Get List of possible exportable classes

    String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");

    try {
      client=new IIQRESTClient(project, environment);
    } catch (CoreException ce) {
      throw new ExecutionException("CoreException "+ce);
    }

    try {
      ArtifactHelper.writeObject(client, theFile, art.getObjectType(), art.getObjectName(), IIQPlugin.getDefault().getBooleanPreference(IIQPreferenceConstants.P_IMPORT_AUTO_CDATA), null);

    } catch (ConnectionException ce) {
      CoreUtils.showConnectionError(shell, ce);
      return null;
    } catch (IOException ioe) {
      IIQPlugin.logException("TODO: IOException ", ioe);
    } catch (CoreException e) {
      IIQPlugin.logException("TODO: CoreException ", e);
    }
    
    return null;

  }

  private ObjectDefinition getTypeAndName(IFile artifactFile) throws XMLArtifactParserException {
    
    InputStream inStream=null;
    XMLEventReader reader=null;

    ObjectDefinition def=null;
    
    try {
      inStream=artifactFile.getContents();
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
          if (DEBUG_REFRESH) {
            IIQPlugin.logDebug(startElement.toString());
          }
          if (!"sailpoint".equals(startElement.getName())) {
            QName type=startElement.getName();
            Attribute name=startElement.getAttributeByName(new QName("name"));
            def=new ObjectDefinition(type.getLocalPart(), name.getValue(), null);
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
    if(def==null) throw new XMLArtifactParserException();
    return def;
  }
  
}
