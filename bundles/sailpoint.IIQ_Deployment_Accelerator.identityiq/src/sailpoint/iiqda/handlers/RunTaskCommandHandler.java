package sailpoint.iiqda.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.dialogs.RunningTaskDialog;
import sailpoint.iiqda.exceptions.ConnectionException;

public class RunTaskCommandHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
        .getActivePage().getSelection();

    IProject project=null;
    
    if (selection != null & selection instanceof IStructuredSelection) {
      IStructuredSelection strucSelection = (IStructuredSelection) selection;
      if(strucSelection instanceof TextSelection) {
        // The request came from the Artifact editor; the selection is some text
        // rather than a set of files in one of the explorer windows
        project=CoreUtils.getActiveWorkbenchFile().getProject();           
      }
    }
    String environment=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment");
    String task=event.getParameter(IIQPlugin.PLUGIN_ID+".commands.taskName");

    IIQRESTClient client;

    try {
      client=new IIQRESTClient(project, environment);
    } catch (CoreException ce) {
      throw new ExecutionException("CoreException "+ce);
    }

    try {
      String taskId=client.runTask(task);
      RunningTaskDialog dlg=new RunningTaskDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), client, taskId);
      int ret=dlg.open();
      return taskId;
    } catch (ConnectionException ce) {
      CoreUtils.showConnectionError(window.getShell(), ce);
      return null;
    }

  }

}
