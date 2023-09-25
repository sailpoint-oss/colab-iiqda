package sailpoint.iiqda.wizards.project.iiq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.IIQNature;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.IIQDAConstants;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.exceptions.ProjectCreationFailedException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.project.iiq.NewIIQProjectWizard.CreationType;

public class IIQProjectCreationRunnable implements IRunnableWithProgress {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));

  private IPath locationPath;
  private CreationType creationType;
  private boolean ssb;
  private String projectName;
  private URI locationURL;
  private List<IPath> jarPaths;
  private List<String> requiredJars;
  private String url;
  private String username;
  private String password;
  private IProject project;
  private IFolder libFolder;
  private IJavaProject javaProject;
  private String debugTransport;
  private String debugPort;

  public IIQProjectCreationRunnable(IPath locationPath, CreationType creationType, boolean ssb,
      String projectName, URI locationURI, List<IPath> jarPaths, List<String> requiredJars, String url,
      String username, String password, String debugTransport, String debugPort) {
    this.locationPath=locationPath;
    this.creationType=creationType;
    this.ssb=ssb;
    this.projectName=projectName;
    this.locationURL=locationURI;
    this.jarPaths=jarPaths;
    this.requiredJars=requiredJars;
    this.url=url;
    this.username=username;
    this.password=password;
    this.debugTransport=debugTransport;
    this.debugPort=debugPort;
  }

  @Override
  public void run(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {

    if (DEBUG_WIZARDS) {
      IIQPlugin.logDebug("Project Creation:");
    }
    
    boolean currentSSBState=IIQPlugin.getDefault().getBooleanPreference(IIQPreferenceConstants.P_USE_SSB_TEMPLATE);
    if(currentSSBState!=ssb) {
      confirmSSBStateChange(ssb);
    }

    monitor.beginTask("Creating Project", 1);
    IProject project=null;
    try {
      switch(creationType) {
        case REMOTE:
          project=createProjectFromRemote(projectName,
              locationURL,
              requiredJars,
              url,
              username,
              password,
              new SubProgressMonitor(monitor, 1));
          break;
        case PROJECT:
        case FILESYSTEM:
          project=createProjectFromProject(projectName,
              locationURL,
              jarPaths,
              url,
              username,
              password,
              new SubProgressMonitor(monitor, 1));
          break;
      }
      copyResourceToFile("/resources/reverse.target.properties", project, "reverse.target.properties");
      copyResourceToFile("/resources/Workflow-Importer.xml", project, "Workflow-Importer.xml");
      copyResourceToFile("/lib/SSAppender.jar", project, "lib/SSAppender.jar");
      if (ssb) {
        // I'd like to do this by copying a filesystem structure from the jar
        // but I can't find an example of that right now
        try {
          IFolder f=project.getFolder("config");
          f.create(true, true, null);
          String[] subfolders= new String[] {"Application", "LocalizedAttribute", "Rule", "TaskDefinition", "TaskSchedule"};
          for(String fldr: subfolders) {
            IFolder subF=f.getFolder(fldr);
            subF.create(true,  true,  null);
          }
        } catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      // Set the project defaults:
      try {
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_EXCLUDED_DIRECTORIES),
            "build");
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_ON_DEMAND_IMPORTS), "error");
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_NUM_LINES_BEFORE_CDATA), Integer.toString(3));
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_NO_RULE_TYPE), "error");
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants. P_UNKNOWN_RULE_TYPE), "error");
        project.setPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants. P_UNHANDLED_EXCEPTIONS), "error");
      } catch (CoreException e) {
        IIQPlugin.logException("Trying to set properties on new project:", e);
      }
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
      if (is!=null) {
        is.close();
      }
    } catch (IOException ioe){}

  }

  private void addNature(IProject project) throws CoreException {
    if (!project.hasNature(IIQNature.NATURE_ID)) {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      String[] newNatures = new String[prevNatures.length+2];
      System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
      newNatures[prevNatures.length] = IIQNature.NATURE_ID;
      newNatures[prevNatures.length+1] = JavaCore.NATURE_ID;
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
        IIQPlugin.logException("createBaseProject: CoreException", e);
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
      libFolder=project.getFolder("lib");
      libFolder.create(false, true, null);
      src=project.getFolder("src");
      src.create(true, true, null);
      monitor.worked(1); // 1
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("CoreException: "+e);
    }

    monitor.subTask("Adding Java nature");
    javaProject = JavaCore.create(project);
    IClasspathEntry source= JavaCore.newSourceEntry(src.getFullPath());
    IClasspathEntry entry = JavaCore.newContainerEntry(
        new Path("org.eclipse.jdt.launching.JRE_CONTAINER"), 
        false); // not exported 
    IClasspathEntry[] newClasspath = { source, entry };

    try {
      javaProject.setRawClasspath(newClasspath, null);
    } catch (JavaModelException e1) {
      throw new ProjectCreationFailedException("JavaModelException: "+e1);
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

  private IProject createProjectFromProject(String projectName, URI location,
      List<IPath> oPaths, String url, String username, String password,
      IProgressMonitor monitor) throws ProjectCreationFailedException {

    createBaseProject(projectName, location, monitor);

    monitor.subTask("Retrieving identityiq.jar from existing project");
    try {
      // get identityiq.jar
      SubProgressMonitor subMon=new SubProgressMonitor(monitor, oPaths.size()); // 7
      List<IPath> paths=new ArrayList<IPath>();
      for (IPath path: oPaths) {
      	IFile iiqFile=libFolder.getFile(path.lastSegment());
      	copy(path, iiqFile.getFullPath(), subMon);
      	paths.add(iiqFile.getFullPath());
      }

      finalizeCreateProject(url, username, password, monitor, paths);
    } catch (CoreException ce) {
      confirmContinueAfterJarFailure(ce.getMessage());
    }

    return project;
  }

  private void confirmContinueAfterJarFailure(String message) throws ProjectCreationFailedException {


    JarFailShower shower=new JarFailShower();
    Display.getDefault().syncExec(shower);
    if (shower.returnCode != Window.OK) {
      throw new ProjectCreationFailedException("Couldn't download JARs - user cancelled");
    }
  }


  private void finalizeCreateProject(String url, String username,
      String password, IProgressMonitor monitor, List<IPath> jarPaths)
          throws JavaModelException, ProjectCreationFailedException {
    monitor.subTask("Adding jars to build path");
    
    IClasspathEntry[] cp=javaProject.getRawClasspath();
    List<IClasspathEntry> newcp=new ArrayList<IClasspathEntry>();

    if(cp!=null) {
    	for (IClasspathEntry cpe: cp) {
    		newcp.add(cpe);
    	}
    }
    
    for (IPath file: jarPaths) {
	    IClasspathEntry iiqLibEntry = JavaCore.newLibraryEntry(
	        file, 
	        null, //no source
	        null, //no source
	        false); //not exported
	    newcp.add(iiqLibEntry);
    }
    cp=newcp.toArray(new IClasspathEntry[newcp.size()]);
    javaProject.setRawClasspath(cp, null);

    monitor.worked(1); //13


    monitor.subTask("Adding IdentityIQ properties to project");
    // Per Services requirements, these go into a properties file
    // default.target.properties
    // as %%ECLIPSE_URL%%, %%ECLIPSE_USER%%, %%ECLIPSE_PASS%%
    //
    // This is to maintain consistency with current SSB xx.target.properties
    // standards

    Properties projectPreferences=new Properties();

    projectPreferences.put(IIQPreferenceConstants.P_URL, url);
    projectPreferences.put(IIQPreferenceConstants.P_USERNAME, username);
    projectPreferences.put(IIQPreferenceConstants.P_PASSWORD, password);
    projectPreferences.put(IIQPreferenceConstants.P_DEBUG_TRANSPORT, debugTransport);
    projectPreferences.put(IIQPreferenceConstants.P_DEBUG_PORT, debugPort);
    try {
      // Urgh.. properties only outputs to an outputstream,
      // and file.setContents only inputs from an inputstream
      PipedInputStream pis=new PipedInputStream();
      PipedOutputStream pos=new PipedOutputStream(pis);

      projectPreferences.store(pos, "Default tokens");
      pos.close();

      IFile f=project.getFile("default"+IIQDAConstants.TARGET_SUFFIX);
      f.create(pis, IFile.FORCE, null);

    } catch (CoreException e) {
      throw new ProjectCreationFailedException(e.getMessage());      
    } catch (IOException e) {
      throw new ProjectCreationFailedException(e.getMessage());      
    }
    SubProgressMonitor subMon=new SubProgressMonitor(monitor, 1);
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, subMon);
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Refresh failed: "+e);
    }

    monitor.done();
  }

  private IProject createProjectFromRemote(String projectName, URI location,
      List<String> jarPaths, String url, String username, String password, IProgressMonitor monitor) throws ProjectCreationFailedException, InterruptedException {

    createBaseProject(projectName, location, monitor);

    monitor.subTask("Retrieving jar files from IdentityIQ");
    IIQRESTClient client=new IIQRESTClient(url, username, password);
    
    List<IPath> paths=new ArrayList<IPath>();
    
    for (String path: jarPaths) {
	    try {
	      // get identityiq.jar
	      SubProgressMonitor subMon=new SubProgressMonitor(monitor, 5); // 7
	      byte[] jar=client.getJarFile(path, subMon);
	      IFile theFile=libFolder.getFile(path);
	      theFile.create(new ByteArrayInputStream(jar), false, null);
	      paths.add(theFile.getFullPath());
	    } catch (ConnectionException e1) {
	      confirmContinueAfterJarFailure(e1.getMessage());
	    } catch (CoreException ce) {
	      confirmContinueAfterJarFailure(ce.getMessage());
	    }
    }
    try {
    	finalizeCreateProject(url, username, password, monitor, paths);
    } catch (JavaModelException jme) {
    	IIQPlugin.logException("finalizing project", jme);
    	throw new ProjectCreationFailedException("From Remote");
    }
    return project;
  }

  private void confirmSSBStateChange(final boolean ssb) {

    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

      public void run() {
        MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Confirm Preference Change", null,
            "Do you wish to save '"+(ssb?"Yes":"No")+"' as your default for 'Use SSB Template'",
            MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);

        int returnCode = dialog.open();

        if (returnCode == Window.OK) {
          IIQPlugin.getDefault().setBooleanPreference(IIQPreferenceConstants.P_USE_SSB_TEMPLATE, ssb);
        }
      }
    });

  }

  public IProject getProject() {
    return project;
  }
  private class JarFailShower implements Runnable {
    public int returnCode;
    @Override
    public void run() {
      StringBuilder sb=new StringBuilder();
      sb.append("Downloading required JARs from IdentityIQ failed. ");
      sb.append("Do you wish to continue? You will need to manually ");
      sb.append("copy identityiq.jar and commons-logging-1.1.jar to ");
      sb.append("your project and add them to the build path.");

      MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "JAR Download Failed", null,
          sb.toString(), MessageDialog.QUESTION, new String[] { "Continue", "Cancel" }, 0);

      returnCode = dialog.open();
    }
  };
}

