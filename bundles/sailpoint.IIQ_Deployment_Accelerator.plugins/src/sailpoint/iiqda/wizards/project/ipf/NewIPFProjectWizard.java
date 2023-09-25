package sailpoint.iiqda.wizards.project.ipf;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import sailpoint.iiqda.IPFPlugin;
import sailpoint.iiqda.core.CorePlugin;
import sailpoint.iiqda.i18n.IPFStrings;

/**
 * Wizard for creation of a new IPF Project.
 * 
 * @author Kevin James
 */

public class NewIPFProjectWizard extends Wizard implements INewWizard{

	
	private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IPFPlugin.PLUGIN_ID+"/debug/Wizards"));

  public enum CreationType {
    REMOTE,
    PROJECT,
    FILESYSTEM;
  }

  // Instance variables
  private IConfigurationElement configElement;
  private WizardNewProjectCreationPage projectPage;
  private IPFNewProjectCreationPage1 pageOne;
  private IPFNewProjectCreationPageFullPage pageTwo;
  private IPFNewProjectCreationPageSnippets pageSnippets;
  private IPFNewProjectCreationPageREST pageREST;
  private IPFNewProjectCreationPageServices pageServices;
  private IPFNewProjectCreationPageRights pageRights;
  private IPFNewProjectCreationPageLibraries pageSix;
  
  private boolean projectLocationExisted;

  private IProject project;

	private List<String> spRights;
  private IPFNewProjectCreationPageWidgets pageWidgets;
	
  /**
   * Constructor
   */
  public NewIPFProjectWizard() {
    super();

    setWindowTitle(IPFStrings.getString("wizard.newipfproject.title"));

    ImageDescriptor descriptor =
        CorePlugin.getIconImageDescriptor("sailpoint.jpg");
    setDefaultPageImageDescriptor(descriptor);
    setNeedsProgressMonitor(true);
    spRights=new ArrayList<String>();
  }
  
  public List<String> getSPRights() {
    return this.spRights;
  }

  public void addRight(String right) {
    if (!spRights.contains(right)) {
      spRights.add(right);
      pageRights.setRights(spRights);
    }
  }
  
	/*
   * @see Wizard#addPages
   */
  public void addPages() {
    super.addPages();

    projectPage = new WizardNewProjectCreationPage("IPF Project");
    
    pageOne = new IPFNewProjectCreationPage1(this);
    
    pageTwo = new IPFNewProjectCreationPageFullPage();
    //pageTwo.setTitle(IPFStrings
    //    .getString("wizard.newipfproject.main.title"));
    //pageTwo.setDescription(IPFStrings
    //    .getString("wizard.newipfproject.main.description"));

    pageSnippets = new IPFNewProjectCreationPageSnippets(this);
    
    pageREST = new IPFNewProjectCreationPageREST(this);
    
    pageServices = new IPFNewProjectCreationPageServices(this);
    
    pageWidgets = new IPFNewProjectCreationPageWidgets(this);

    pageRights = new IPFNewProjectCreationPageRights(this);
    
    pageSix = new IPFNewProjectCreationPageLibraries();
    
    addPage(projectPage);
    addPage(pageOne);
    addPage(pageTwo);
    addPage(pageSnippets);
    addPage(pageREST);
    addPage(pageServices);
    addPage(pageWidgets);
    addPage(pageRights);
    addPage(pageSix);
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
//    Point size = getShell().computeSize( 510, 550 );
//    getShell().setSize( size );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish() {
    return pageOne.isPageComplete() && pageTwo.isPageComplete() && pageSnippets.isPageComplete()
        && super.canFinish();
  }

  /**
   * 
   * @param monitor
   * @throws InterruptedException
   * @throws CoreException
   */
//  protected void finishPage(IProgressMonitor monitor)
//      throws InterruptedException, CoreException {
//    BasicNewProjectResourceWizard.updatePerspective(configElement);
//  }

  /**
   * @see IWizard#performCancel()
   */
  public boolean performCancel() {
    if (!projectLocationExisted) {
      removeProject();
    }

    return super.performCancel();
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

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  public boolean performFinish() {
    boolean completed = true;

    // Track whether the project location already exists
    File projectLocationFile = projectPage.getLocationPath().toFile();
    projectLocationExisted = projectLocationFile.exists();

    // The steps necessary for creating the new IIQ Project
    IPFProjectCreationRunnable runnable = new IPFProjectCreationRunnable(
        projectPage.getProjectName(),
        projectPage.getLocationURI(),
        pageOne.getData(),
        pageTwo.getData(),
        pageSnippets.getSnippets(),
        pageServices.getData(),
        pageRights.getData().getCapabilities(),
        pageREST.getData(),
        pageWidgets.getData(),
        pageSix.getData()
        
    );

    try {
       ProgressMonitorDialog dlg=new ProgressMonitorDialog(getShell());
       dlg.run(true, true,
          //          new WorkspaceModifyDelegatingOperation(runnable));
          runnable);
    } catch (InvocationTargetException e) {
      // EclipseMECorePlugin.log(IStatus.ERROR, "performFinish",
      // e.getCause());
      completed = false;
      e.printStackTrace();
    } catch (Exception e) {
      project=runnable.getProject();
      if (project!=null) {
        removeProject();
      }
      completed = false;    
    } finally {
      project=runnable.getProject();
    }

    return completed;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
  }

  /**
   * Return the handle to the project to be created.
   * 
   * @return
   */
  IProject getCreatedProjectHandle() {
    return projectPage.getProjectHandle();
  }

  /**
   * Return the runnable used to perform the project creation work.
   * 
   * @return
   */

  //  private void copyFolderToProject(IProject project, String fldr) {
  //
  //    IFolder destFldr=project.getFolder(fldr);
  //    Bundle bndle=CoreActivator.getDefault().getBundle();
  //    Enumeration<String> paths=bndle.getEntryPaths("/resources/"+fldr);
  //    //srcFldrUrl.
  //    while(paths.hasMoreElements()) {
  //      System.out
  //      .println("NewIIQProjectWizard.copyFolderToProject: "+paths.nextElement());
  //    }
  //  }


//  private void copyFolders(String resource, IContainer fldr){
//
//    URL u= getClass().getResource(resource);
//    try {
//      URI i = new URI(u.getFile());
//      File f=new File(i);
//      copyFiles(f, fldr);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  void copyFiles (File srcFolder, IContainer destFolder) throws Exception {
    for (File f: srcFolder.listFiles()) {
      if (f.isDirectory()) {
        IFolder newFolder = destFolder.getFolder(new Path(f.getName()));
        newFolder.create(true, true, null);
        copyFiles(f, newFolder);
      } else {
        IFile newFile = destFolder.getFile(new Path(f.getName()));
        newFile.create(new FileInputStream(f), true, null);
      }
    }
  }
  @Override
  public boolean needsProgressMonitor() {
    return true;
  }

  public WizardNewProjectCreationPage getProjectPage() {
    return projectPage;
  }
  
  public IPFNewProjectCreationPage1 getPageOne() {
    return pageOne;
  }

  public IPFNewProjectCreationPageFullPage getPageTwo() {
    return pageTwo;
  }

  public IPFNewProjectCreationPageSnippets getPageThree() {
    return pageSnippets;
  }

  public IPFNewProjectCreationPageREST getPageFour() {
    return pageREST;
  }
  
  public IPFNewProjectCreationPageRights getPageFive() {
    return pageRights;
  }

  public IPFNewProjectCreationPageLibraries getPageSix() {
    return pageSix;
  }

}
