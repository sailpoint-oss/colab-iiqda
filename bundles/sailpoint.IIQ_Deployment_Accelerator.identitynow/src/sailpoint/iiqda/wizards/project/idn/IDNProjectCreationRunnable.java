package sailpoint.iiqda.wizards.project.idn;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.builder.IDNNature;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.exceptions.ProjectCreationFailedException;
import sailpoint.iiqda.preferences.IDNPreferenceConstants;

public class IDNProjectCreationRunnable implements IRunnableWithProgress {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/Wizards"));

  private String projectName;
  private String organisation;
  private String username;
  private String password;
  private IProject project;

  public IDNProjectCreationRunnable(String projectName, String organisation,
      String username, String password) {
    this.projectName=projectName;
    this.organisation=organisation;
    this.username=username;
    this.password=password;
  }

  @Override
  public void run(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {

    if (DEBUG_WIZARDS) {
      IDNPlugin.logDebug("Project Creation:");
    }
   

    monitor.beginTask("Creating Project", 1);
    try {
      createBaseProject(projectName, null, monitor);
      finalizeCreateProject(organisation, username, password, monitor);
    } catch (ProjectCreationFailedException e) {
      final Exception ex=e;
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          ErrorDialog.openError(
              Display.getDefault().getActiveShell(),
              "IIQ Plugin",
              "Project Creation Failed:",
              CoreUtils.toErrorStatus(ex.getMessage())
              );
        }
      });
      throw new InterruptedException(); // this does the same as cancel
    } finally {
      monitor.done();
    }
  }

  private void copyResourceToFile(String resource, IContainer fldr, String target){

    InputStream is=null;
    // however, this works for everything
    is = getClass().getResourceAsStream(resource);

    if(is!=null) {
      IPath path=new Path(target);
      IFile fTarget=fldr.getFile(path);
      if(!fTarget.exists()) {
        try {
          fTarget.create(is, true, null);
        } catch (CoreException ce) {
          // TODO: Do something to alert the user to this problem
          System.out
          .println("NewIIQProjectWizard.createBaseProject: CoreException creating "+target+" : "+ce);
        }
      } else {
        // TODO: Ask to overwrite
      }

    }
    try {
      is.close();
    } catch (IOException ioe){}

  }

  private void addNature(IProject project) throws CoreException {
    if (!project.hasNature(IDNNature.NATURE_ID)) {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      String[] newNatures = new String[prevNatures.length+1];
      System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
      newNatures[prevNatures.length] = IDNNature.NATURE_ID;
      description.setNatureIds(newNatures);     

      IProgressMonitor monitor = null;
      project.setDescription(description, monitor);
    }
  }

  /**
   * Just do the basics: create a basic project.
   * 
   * @param location
   * @param projectName
   */
  private static IProject createBaseProject(String projectName, URI location) {
    // it is acceptable to use the ResourcesPlugin class

    IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
        .getProject(projectName);

    if (!newProject.exists()) {

      try {
        newProject.create(null);
        if (!newProject.isOpen()) {
          newProject.open(null);
        }
      } catch (CoreException e) {
        IDNPlugin.logException("createBaseProject: CoreException", e);
      }
    } else {
      // TODO: Ask to overwrite
    }

    return newProject;
  }

  private void createBaseProject(String projectName, URI location,
      IProgressMonitor monitor) throws ProjectCreationFailedException {
    project = createBaseProject(projectName, location);
    monitor.beginTask("Creating Project", 15);

    IFolder src=null;
    try {
      monitor.subTask("Creating base project");
      addNature(project);     
      src=project.getFolder("src");
      src.create(true, true, null);
      monitor.worked(1); // 1
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("CoreException: "+e);
    }

    monitor.worked(1); // 2
  }
  
  private void copy(IPath from, IPath to, IProgressMonitor subMon) throws CoreException{
    IFile file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(from);
    if(file!=null) {
      file.copy(to, true, subMon);
      return;
    } else {
      IFileStore fileStore = EFS.getLocalFileSystem().getStore(from);
      IFileStore rootStore = EFS.getLocalFileSystem().getStore(ResourcesPlugin.getWorkspace().getRoot().getRawLocation());
      if(fileStore!=null) {
        fileStore.copy(rootStore.getFileStore(to), EFS.NONE, subMon);
      }        
    }
  }


  private void finalizeCreateProject(String organisation, String username,
      String password, IProgressMonitor monitor)
          throws ProjectCreationFailedException {
    monitor.worked(1); //13


    monitor.subTask("Adding IdentityNow properties to project");

    // format is name|org|apiKey|apiSecret,name|org|apiKey|apiSecret etc. 
    
    try {
      project.setPersistentProperty(
        new QualifiedName("", IDNPreferenceConstants.P_IDN_ENDPOINTS), "default|"+organisation+"|"+username+"|"+password);
    } catch (CoreException ce) {
      
    }
    SubProgressMonitor subMon=new SubProgressMonitor(monitor, 1);
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, subMon);
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Refresh failed: "+e);
    }

    monitor.done();
  }
  
  public IProject getProject() {
    return project;
  }
}

