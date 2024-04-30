package sailpoint.iiqda.wizards.importresource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import sailpoint.iiqda.ArtifactHelper;
import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class ImportResourceWizard extends Wizard implements INewWizard {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));
  
  private ImportResourceWizardPage wizardPage;
  private ISelection selection;

  private IIQRESTClient client;
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

  public void setRESTClient(IIQRESTClient client) {
    this.client=client;
    if(wizardPage!=null) {
      wizardPage.setRESTClient(client);
    }
  }

  public void addPages() {
    wizardPage = new ImportResourceWizardPage(selection);
    wizardPage.setRESTClient(client);
    addPage(wizardPage);


  }

  /**
   * This method is called when 'Finish' button is pressed in
   * the wizard. We will create an operation and run it
   * using wizard as execution context.
   */
  public boolean performFinish() {
    final List<ObjectDefinition> objects=wizardPage.getSelectedObjects();
    final boolean shouldInsertCDATA=wizardPage.shouldInsertCDATA();

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(objects, destinationContainer, shouldInsertCDATA, monitor);
        } catch (CoreException e) {
          if (DEBUG_WIZARDS) IIQPlugin.logDebug("ImportResourceWizard.performFinish CoreException "+e);
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

  // TODO: when we have more than two of these, abstract it out
  private void confirmCDataStateChange(boolean cdata) {
    MessageDialog dialog = new MessageDialog(getShell(), "Confirm Preference Change", null,
        "Do you wish to save '"+(cdata?"Yes":"No")+"' as your default for 'Automatically surround beanshell with CDATA tags'",
        MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);

    int returnCode = dialog.open();

    if (returnCode == Window.OK) {
      IIQPlugin.getDefault().setBooleanPreference(IIQPreferenceConstants.P_IMPORT_AUTO_CDATA, cdata);
    }
  }


  private void doFinish(
      List<ObjectDefinition> objects,
      IContainer destinationContainer2,
      boolean shouldInsertCDATA, IProgressMonitor monitor)
          throws CoreException {

    IResource prj=destinationContainer2.getProject();
    String sOpenOnImport=prj.getPersistentProperty(new QualifiedName("", IIQPreferenceConstants.P_OPEN_ON_IMPORT));
    // KCS 2023-12-28
    String sCustomFilenames=prj.getPersistentProperty(new QualifiedName("", IIQPreferenceConstants.P_CUSTOM_FILENAMES));
    // KCS 2023-12-28
    // respect old default functionality
    boolean bOpenOnImport=true;
    if (sOpenOnImport!=null) bOpenOnImport=Boolean.parseBoolean(sOpenOnImport);
    // KCS 2023-12-28
    boolean bCustomFilenames=false;
    if (sCustomFilenames!=null) bCustomFilenames=Boolean.parseBoolean(sCustomFilenames);
    // KCS 2023-12-28
    
    for (ObjectDefinition objDef: objects) {
      String fileName=objDef.getObjectType()+"-"+CoreUtils.toCamelCase(objDef.getObjectName(), true)+".xml";
      // KCS 2023-12-28
      if(bCustomFilenames) {
        fileName=objDef.getObjectType()+"-"+CoreUtils.toCustomCase(objDef.getObjectName(), true)+".xml";
      }
      // KCS 2023-12-28
      // Strip invalid characters from the name
      fileName=fileName.replace("?", "");
      fileName=fileName.replace(":", "_");
      monitor.beginTask("Creating " + fileName, 2);
      //IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IFile file = destinationContainer2.getFile(new Path(fileName));

      try {
        ArtifactHelper.writeObject(client, file, objDef.getObjectType(), objDef.getObjectName(), shouldInsertCDATA, monitor);
        monitor.worked(1);
        
        
        if (bOpenOnImport) {
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
        }
        monitor.worked(1);
      } catch (ConnectionException ce) {
        CoreUtils.showConnectionError(getShell(), ce);
        return;
      } catch (IOException ioe) {
        IIQPlugin.logException("TODO: IOException", ioe);
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