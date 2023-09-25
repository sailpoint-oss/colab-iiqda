package sailpoint.iiqda.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.deployer.HotDeployJob;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class HotDeployCommandHandler extends AbstractHandler {
  /**
   * The constructor.
   */
  public HotDeployCommandHandler() {
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
    ArrayList<ICompilationUnit> culist=new ArrayList<ICompilationUnit>();

    if (selection != null & selection instanceof IStructuredSelection) {
      IStructuredSelection strucSelection = (IStructuredSelection) selection;

      for (@SuppressWarnings("unchecked")
      Iterator<Object> iterator = strucSelection.iterator(); iterator
          .hasNext();) {
        Object element = iterator.next();

        if (element instanceof IFile) {
            element=JavaCore.createCompilationUnitFrom((IFile)element);
        }
        
        if( !(element instanceof ICompilationUnit) ) {
          MessageDialog.openInformation(
              window.getShell(),
              "IIQ Plugin",
              "Can't hot-deploy non-java file "+(element.getClass().getName()) );
          return null;
        }
        culist.add((ICompilationUnit)element);
      }
      String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");

      try {
        Job job = new HotDeployJob(culist, environment);
        job.schedule();
      } catch (CoreException e) {
        MessageDialog.openError(
            window.getShell(),
            "IIQ Plugin",
            "Can't get deployment properties for target environment: "+environment);
        return null;
      }

    }

    return null;

  }

}
