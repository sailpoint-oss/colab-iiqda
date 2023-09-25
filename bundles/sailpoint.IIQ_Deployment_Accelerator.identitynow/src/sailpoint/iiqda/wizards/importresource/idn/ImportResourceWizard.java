package sailpoint.iiqda.wizards.importresource.idn;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class ImportResourceWizard extends Wizard implements INewWizard {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/Wizards"));
  
  private ImportResourceWizardPage wizardPage;
  private ISelection selection;

  private IDNRestHandler client;
  private IContainer destinationContainer;

  public void setDestinationContainer(IContainer containingProject) {
    this.destinationContainer = containingProject;
  }

  public ImportResourceWizard() {
    super();
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */

  public void setRESTHandler(IDNRestHandler client) {
    this.client=client;
    if(wizardPage!=null) {
      wizardPage.setRESTHandler(client);
    }
  }

  public void addPages() {
    wizardPage = new ImportResourceWizardPage(selection);
    wizardPage.setRESTHandler(client);
    addPage(wizardPage);


  }

  /**
   * This method is called when 'Finish' button is pressed in
   * the wizard. We will create an operation and run it
   * using wizard as execution context.
   */
  public boolean performFinish() {
    final List<ObjectDefinition> objects=wizardPage.getSelectedObjects();

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(objects, destinationContainer, monitor);
        } catch (CoreException e) {
          if (DEBUG_WIZARDS) IDNPlugin.logDebug("ImportResourceWizard.performFinish CoreException "+e);
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  /**
   * The worker method. It will find the container, create the
   * file if missing or just replace its contents, and open
   * the editor on the newly created file.
   * @param shouldInsertCDATA 
   */


  private void doFinish(
      List<ObjectDefinition> objects,
      IContainer destinationContainer2,
      IProgressMonitor monitor)
          throws CoreException {


    for (ObjectDefinition objDef: objects) {
      String fileName=objDef.getObjectType()+"-"+CoreUtils.toCamelCase(objDef.getObjectName(), true)+"."+objDef.getObjectType().toLowerCase();
      // Strip invalid characters from the name
      fileName=fileName.replace("?", "");
      fileName=fileName.replace(":", "_");
      monitor.beginTask("Creating " + fileName, 2);
      //IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IFile file = destinationContainer2.getFile(new Path(fileName));

      try {
        IDNHelper.writeObject(client, file, objDef.getObjectType(), objDef.getObjectName(), monitor);
        monitor.worked(1);
        monitor.setTaskName("Opening file for editing...");
        getShell().getDisplay().asyncExec(new Runnable() {
          public void run() {
            IWorkbenchPage page =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try {
              IDE.openEditor(page, file, true);
            } catch (PartInitException e) {
            }
          }
        });
        monitor.worked(1);
      } catch (ConnectionException ce) {
        CoreUtils.showConnectionError(getShell(), ce);
        return;
      } catch (IOException ioe) {
        IDNPlugin.logException("TODO: IOException", ioe);
      }
    }
  }




  private void throwCoreException(String message) throws CoreException {
    throw new CoreException(CoreUtils.toErrorStatus(message));
  }

  /**
   * We will accept the selection in the workbench to see if
   * we can initialize from it.
   * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.selection = selection;
  }

  public boolean isReady() {
    if(wizardPage==null) {
      return false;
    }
    return wizardPage.isReady();		
  }

  @Override
  public boolean canFinish() {
    return wizardPage!=null && wizardPage.isPageComplete();
  }

}