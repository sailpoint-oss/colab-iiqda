package sailpoint.iiqda.wizards.project.idn;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import sailpoint.iiqda.IDNPlugin;

public class NewIDNProjectWizard extends Wizard implements INewWizard {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/Wizards"));
  private IDNNewProjectCreationPage projectPage;
  private boolean projectLocationExisted;
  private IProject project;

  @Override
  public boolean performFinish() {
    boolean completed = true;

    System.out.println("NewIDNProjectWizard.performFinish:");

    // Track whether the project location already exists
    File projectLocationFile = projectPage.getLocationPath().toFile();
    projectLocationExisted = projectLocationFile.exists();
    
    // The steps necessary for creating the new IIQ Project
    IDNPageData pageData=projectPage.getPageData();
    IDNProjectCreationRunnable runnable = new IDNProjectCreationRunnable(projectPage.getProjectName(), 
        pageData.getOrganisation(),
        pageData.getAPIKey(),
        pageData.getAPISecret()
    );

    try {
      getContainer().run(true, true,
          //          new WorkspaceModifyDelegatingOperation(runnable));
          runnable);
    } catch (InvocationTargetException e) {
      // EclipseMECorePlugin.log(IStatus.ERROR, "performFinish",
      // e.getCause());
      completed = false;
      e.printStackTrace();
    } catch (InterruptedException e) {
      project=runnable.getProject();
      removeProject();
      completed = false;
    } finally {
      project=runnable.getProject();
    }
    return completed;
    
  }

  @Override
  public void addPages() {
    super.addPages();
    projectPage = new IDNNewProjectCreationPage("IDN Project");

    addPage(projectPage);
    
  }

  @Override
  public boolean canFinish() {
    return projectPage.isPageComplete();
  }

  @Override
  public void init(IWorkbench arg0, IStructuredSelection arg1) {
    // TODO Auto-generated method stub
    System.out.println("IWorkbenchWizard.init:");
    
  }

  /**
   * Remove the already created project, as the user cancelled the operation.
   * Or, there was an error during project creation
   */
  private void removeProject() {
    if (project != null) {

      IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException
            {
          monitor.beginTask("##Remove Project", 3);

          try {
            project.delete(true, false, monitor);
          } catch (CoreException e) {
            throw new InvocationTargetException(e);
          } finally {
            monitor.done();
          }
            }
      };

      try {
        getContainer().run(false, true, op);
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        // cancel pressed
      }
    }
  }
  
}
