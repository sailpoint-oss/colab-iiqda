package sailpoint.iiqda.handlers;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.DeployArtifactJob;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SetupXMLCommandHandler extends AbstractHandler {
	
  private static final boolean DEBUG_PARSER = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Handler"));
  
  /**
	 * The constructor.
	 */
	public SetupXMLCommandHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		// Sanity check - selection is only ".xml" IFiles

		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		ArrayList<IFile> filelist=new ArrayList<IFile>();

		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			if(strucSelection instanceof TextSelection) {
				// The request came from the Artifact editor; the selection is some text
				// rather than a set of files in one of the explorer windows
				filelist.add(CoreUtils.getActiveWorkbenchFile());           
			} else {
				for (@SuppressWarnings("unchecked")
				Iterator<Object> iterator = strucSelection.iterator(); iterator
						.hasNext();) {
					Object element = iterator.next();

					if( !(element instanceof IFile) || 
							! ( 
									((IFile)element).getName().equals("setup.xml") && "sailpoint.dtd".equals(IIQPlugin.getDTD((IFile)element))
									)
							) {
						MessageDialog.openInformation(
								window.getShell(),
								"IIQ Plugin",
								"Can't run with non setup.xml file "+((IFile)element).getName());
						return null;
					}
					filelist.add((IFile)element);
				}
			}
			String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");

			if(filelist.size()>1) {
				MessageDialog.openInformation(
						window.getShell(),
						"IIQ Plugin",
						"Can't work on multiple selections");
				return null;
			}

			//      try {
			System.out.println("Do something with setup.xml");
			try {
	      IFile setupXML = filelist.get(0);
				List<String> filesToImport=parse(setupXML);
	      // Now got a raw list. Make them relative to setup.xml
	      // or if they start with '/', relative to project
	      // TODO: the 'root'ing won't work with SERI because /config/catalog becomes
	      // /seri/catalog after deployment - maybe this will help force people to *not*
	      // use root filesystem declarations..
	      IContainer fldr=setupXML.getParent();
	      
	      List<IFile> outList=new ArrayList<IFile>();
	      for(String file: filesToImport) {
	      	IResource member = fldr.findMember(file);
					if(member==null) {
	  				MessageDialog.openInformation(
	  						window.getShell(),
	  						"IIQ Plugin",
	  						"Can't find resource "+file);
	  				return null;
	      	}
					if(!(member instanceof IFile)) {
						MessageDialog.openInformation(
	  						window.getShell(),
	  						"IIQ Plugin",
	  						"Resource "+file+" is not a file");
	  				return null;
	      	}
					outList.add((IFile)member);
	      }
	      Job job = new DeployArtifactJob(outList, environment);
	      job.schedule();
      } catch (XMLArtifactParserException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      } catch (CoreException e) {
      	MessageDialog.openError(
      			window.getShell(),
      			"IIQ Plugin",
      			"Can't get properties for target environment: "+environment);
      	return null;
      }

		}

		return null;

	}

	private List<String> parse(IFile element) throws XMLArtifactParserException {

		List<String> filesToImport=new ArrayList<String>();
		XMLInputFactory2 fac = (XMLInputFactory2) XMLInputFactory2.newInstance();
		XMLStreamReader2 stream = null;
		fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
				Boolean.FALSE);
		fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
		// Doesn't work:

		try {
			stream = (XMLStreamReader2) fac
					.createXMLStreamReader(new InputStreamReader(element.getContents()));

			while (stream.hasNext()) {
				int evtCode = stream.next();
				if (evtCode == XMLStreamConstants.START_ELEMENT) {

					String elName = stream.getLocalName();
					if (DEBUG_PARSER) {
						IIQPlugin.logDebug("start: " + elName);
					}
					// <sailpoint> is a wrapper for <ImportAction> Commands
					if (!("sailpoint".equals(elName))) {
						// If it's an <Identity>, ignore stuff until we have the </Identity>
						if (!("ImportAction".equals(elName))) {
							throw new XMLArtifactParserException("Unhandled element "+elName);
						}
						String name=stream.getAttributeValue("", "name");
						String value=stream.getAttributeValue("", "value");
						if (!"include".equals(name)) {
							throw new XMLArtifactParserException("unhandler ImportAction type '"+name+"'");
						}
						filesToImport.add(value);
					}
				}
			}

			stream.close();
		} catch (XMLStreamException xmle) {
			IIQPlugin.logException("SetupXMLCommandHandler.parse: XMLException ", xmle);
			throw new XMLArtifactParserException(xmle);
		} catch (CoreException e) {
			IIQPlugin.logException("SetupXMLCommandHandler.parse: CoreException ", e);
			throw new XMLArtifactParserException(e);
    }
		return filesToImport;
	}

}