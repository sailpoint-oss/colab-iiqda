package sailpoint.iiqda.wizards.project.iiq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CorePlugin;
import sailpoint.iiqda.i18n.IIQStrings;

/**
 * Wizard for creation of a new IIQ Project.
 * 
 * @author Kevin James
 */

public class NewIIQProjectWizard extends Wizard implements INewWizard {

	
	private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));

  public enum CreationType {
    REMOTE,
    PROJECT,
    FILESYSTEM;
  }

  // Instance variables
  private IConfigurationElement configElement;
  private IIQNewProjectCreationPage mainPage;
  private boolean projectLocationExisted;

  private IProject project;
	private List<String> requiredJars;


  /**
   * Constructor
   */
  public NewIIQProjectWizard() {
    super();

    setWindowTitle(IIQStrings.getString("wizard.newproject.title"));

    ImageDescriptor descriptor =
        CorePlugin.getIconImageDescriptor("sailpoint.jpg");
    setDefaultPageImageDescriptor(descriptor);
    setNeedsProgressMonitor(true);
    requiredJars = getRequiredJars();
  }

  private List<String> getRequiredJars() {
  	
  	
	  if(DEBUG_WIZARDS) IIQPlugin.logDebug("NewIIQProjectWizard.getRequiredJars");
	  
	  List<String> requiredJars=new ArrayList<String>();
	  URL url;
	  try {
	          url = new URL("platform:/plugin/"+IIQPlugin.PLUGIN_ID+"/resources/requiredJars.txt");
	      InputStream inputStream = url.openConnection().getInputStream();
	      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	      String inputLine;
	   
	      while ((inputLine = in.readLine()) != null) {
	      	if(DEBUG_WIZARDS) IIQPlugin.logDebug("required Jar: "+inputLine);
	      	requiredJars.add(inputLine);
	      }
	   
	      in.close();
	   
	  } catch (IOException e) {
	      IIQPlugin.logException("Reading required Jars List", e);
	  }
	  return requiredJars;
  }

	/*
   * @see Wizard#addPages
   */
  public void addPages() {
    super.addPages();
System.out.println("addPages");
    mainPage = new IIQNewProjectCreationPage(requiredJars);
    mainPage.setTitle(IIQStrings
        .getString("wizard.newproject.main.title"));
    mainPage.setDescription(IIQStrings
        .getString("wizard.newproject.main.description"));

    addPage(mainPage);
  }



  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish() {
    return mainPage.isPageComplete() && super.canFinish();
  }

  /**
   * 
   * @param monitor
   * @throws InterruptedException
   * @throws CoreException
   */
  protected void finishPage(IProgressMonitor monitor)
      throws InterruptedException, CoreException {
    BasicNewProjectResourceWizard.updatePerspective(configElement);
  }

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
    File projectLocationFile = mainPage.getLocationPath().toFile();
    projectLocationExisted = projectLocationFile.exists();

    // The steps necessary for creating the new IIQ Project
    IIQProjectCreationRunnable runnable = new IIQProjectCreationRunnable(mainPage.getLocationPath(),
        mainPage.getCreationType(),
        mainPage.copySSB(),
        mainPage.getProjectName(),
        mainPage.getLocationURI(),
        mainPage.getJarPaths(),
        requiredJars,
        mainPage.getURL(),
        mainPage.getUsername(),
        mainPage.getPassword(),
        mainPage.getDebugTransport(),
        mainPage.getDebugPort()
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
    return mainPage.getProjectHandle();
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

  
}
