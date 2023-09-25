package sailpoint.iiqda.handlers.idn;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.idn.DeployObjectJob;
import sailpoint.iiqda.idn.IDNEnvironment;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.idn.IDNRestHandler;

public class DeployResourceCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

	  System.out.println("DeployResourceCommandHandler.execute: ");

		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win.getShell();

		IProject project = null;
		IContainer container;

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

          if( !(element instanceof IFile) ) { 
            MessageDialog.openInformation(
                shell,
                "IIQ Plugin",
                "Can't deploy non file "+element.getClass().getName());
            return null;
          }
          filelist.add((IFile)element);
          project=((IFile)element).getProject();
        }
      }
    }
    String environment=event.getParameter(IDNPlugin.PLUGIN_ID+".commands.targetEnvironment");
    System.out.println("DeployResourceCommandHandler.execute: environment="+environment);

    IDNRestHandler client=null;
    
    try {
      IDNEnvironment idn=IDNHelper.getEnvironment(project, environment);
    	client=new IDNRestHandler(idn);
    	Job job = new DeployObjectJob(filelist, idn);
    	job.schedule();
    } catch (CoreException ce) {
    	throw new ExecutionException("CoreException "+ce);
    }
    
    return null;
	}
	


}
