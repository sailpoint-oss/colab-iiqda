package sailpoint.iiqda.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class DeployArtifactCommandHandler extends AbstractHandler {
  /**
   * The constructor.
   */
  public DeployArtifactCommandHandler() {
    System.out.println("Dplo");
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
                  ((IFile)element).getName().endsWith(".xml") && "sailpoint.dtd".equals(IIQPlugin.getDTD((IFile)element))
                  )
              ) {
            MessageDialog.openInformation(
                window.getShell(),
                "IIQ Plugin",
                "Can't deploy non-artifact file "+((IFile)element).getName());
            return null;
          }
          filelist.add((IFile)element);
        }
      }
      String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");

      try {
        Job job = new DeployArtifactJob(filelist, environment);
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
