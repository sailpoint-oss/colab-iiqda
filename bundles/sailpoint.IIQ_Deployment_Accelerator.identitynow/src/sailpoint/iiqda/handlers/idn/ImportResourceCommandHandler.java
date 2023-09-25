package sailpoint.iiqda.handlers.idn;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.idn.IDNEnvironment;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.wizards.importresource.idn.ImportResourceWizard;

public class ImportResourceCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

	  System.out.println("ImportResourceCommandHandler.execute: ");

		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win.getShell();

		IProject project;
		IContainer container;

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
				System.out
				.println("ResourceImportTaskHandler.execute: can't work on multiple objects");
				MessageDialog.openError(
						shell,
						"IIQ Plugin",
						"Can't work on multiple objects!");
				return null;      
			}
			Object element = strucSelection.getFirstElement();
			if( !(element instanceof IProject || element instanceof IFolder) ) { 
				MessageDialog.openError(
						shell,
						"IIQ Plugin",
						"Can't import into non-project or folder");
				return null;
			}
			if(element instanceof IProject) {
				project=((IProject)element);
				container=(IContainer)element;
			}
			else {
				project=((IFolder)element).getProject();
				container=(IContainer)element;
			}
		} else {
			MessageDialog.openError(
					shell,
					"IIQ Plugin",
					"Unknown selection type "+selection.getClass().getName());
			return null;      
		}

    String environment=event.getParameter(IDNPlugin.PLUGIN_ID+".commands.targetEnvironment");
System.out.println("ImportResourceCommandHandler.execute: environment="+environment);

    IDNRestHandler client=null;
    
    try {
      IDNEnvironment idn=IDNHelper.getEnvironment(project, environment);
    	client=new IDNRestHandler(idn);
    } catch (CoreException ce) {
    	throw new ExecutionException("CoreException "+ce);
    }
    
		ImportResourceWizard wizard = new ImportResourceWizard();
		wizard.setRESTHandler(client);
		wizard.setDestinationContainer((IContainer)container);
		wizard.init(HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench(),
				(IStructuredSelection)selection);
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();		
		if(!wizard.isReady()) {
		  return null;
		}		  
		int ret=dialog.open();
		return null;
	}
	


}
