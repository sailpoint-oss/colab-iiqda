package sailpoint.iiqda.deployer;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.SubstitutingInputStream;

public class DeployArtifactJob extends Job {

  private static final boolean TRACE_DEPLOY = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/trace/Deploy"));

  private List<IFile> filelist;
  private String environment;

  private IProject parentProject;

  private List<String> contentWarnings;
  private boolean trace;

  public DeployArtifactJob(List<IFile> filelist, String environment) throws CoreException {
    super("Deploy Rules");

    this.filelist=filelist;
    this.environment=environment;
    if(filelist!=null) {
      IFile f=filelist.get(0);
      parentProject=f.getProject();
    }
    this.contentWarnings=null;
    setUser(true);
  }



  @Override
  protected void canceling() {
    // TODO Auto-generated method stub
    if(trace) {
      IIQPlugin.logTrace("DeployArtifactJob.canceling:");
    }
    super.canceling();
  }

  public IStatus run(IProgressMonitor monitor) { 

    // TODO: Fix the progress bar to be the right size..

//    MessageConsole console=IIQPlugin.findConsole("Deployer");
//    PrintWriter out = new PrintWriter(console.newMessageStream());

    IIQRESTClient cl=null;
    try {
      cl=new IIQRESTClient(parentProject, environment);
    } catch (CoreException ce) {
      IStatus stat=CoreUtils.toErrorStatus("Unable to deploy - "+ce);
      return stat;
    }

    monitor.beginTask("Deploying jobs...", 2*filelist.size()+1); 
    monitor.worked(1);

    for ( IFile objectFile : filelist ) {

      IPath xmlRuleFileName = objectFile.getFullPath();
      String objectFileName = objectFile.getName();
      objectFile = ResourcesPlugin.getWorkspace().getRoot().getFile(xmlRuleFileName);
      if (objectFile==null||!objectFile.exists()) {
        IStatus stat = CoreUtils.toErrorStatus("Can't find file "+objectFileName);
        return stat;
      }

      monitor.setTaskName("Reading "+objectFile.getName());
      if(TRACE_DEPLOY) {
        IIQPlugin.logTrace("Reading "+objectFile.getName());
      }
      String xml = getContents(objectFile);
      if(contentWarnings!=null && contentWarnings.size()>0) {
        boolean shouldQuit=displayWarnings();
        if(shouldQuit) {
          monitor.done();
          return CoreUtils.toWarningStatus("Operation cancelled by user");
        }
      }
      monitor.worked(1);

      try
      {

        monitor.setTaskName("Deploying "+objectFileName);
        if(TRACE_DEPLOY) {
          IIQPlugin.logTrace("Deploying "+objectFileName);
        }
        // TODO: Take the bits out of Importer so we don't need any of the SailpointConsole code
        try {
          // who should commit, us or the importer?
          if(trace) {
            IIQPlugin.logTrace("DeployArtifactJob.run: --------------------------------");
            IIQPlugin.logTrace(xml);
            IIQPlugin.logTrace("DeployArtifactJob.run: --------------------------------");
          }
          //TODO: Rest call to Importer workflow
          cl.sendFile(xml);
        } catch (Exception e) {
          IIQPlugin.logException("DeployArtifactJob.run: Throwable ", e);
          final String message=e.getMessage();
          PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
              Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
              MessageDialog.openError(
                  activeShell,
                  "IIQ Plugin",
                  "Deploy Failed: "+message);
            }
          });
        }

        monitor.worked(1);
      } catch (Exception e) {
        IStatus stat=CoreUtils.toErrorStatus("Unable to deploy - "+e);
        IIQPlugin.logException("Exception during deploy ", e);
        return stat;

      }

    }

    monitor.done(); 
    return Status.OK_STATUS; 
  }

  private boolean displayWarnings() {
    String targetEnvironment="TODO";
    IStatus[] statii=new IStatus[contentWarnings.size()];
    for(int i=0;i<contentWarnings.size();i++) {
      statii[i]=CoreUtils.toWarningStatus(contentWarnings.get(i));
    }
    IStatus missing=new MultiStatus(IIQPlugin.PLUGIN_ID, 1, statii, 
        "Some substituion macros are not defined for the target environment: "+targetEnvironment+"\n"
            +"Artifacts containing these macros will be deployed with the macro as-is\n"
            +"Do you wish to continue?",
            null);
    AskTheUserAboutMissingSubs subs=new AskTheUserAboutMissingSubs(missing);

    org.eclipse.swt.widgets.Display.getDefault().syncExec( subs );
    return subs.shouldQuit();
  }



  private String getContents(IFile file) {

    if(file==null) return "";
    InputStream is=null;
    try {
      is=file.getContents();
      SubstitutingInputStream sis=new SubstitutingInputStream(parentProject, environment, is);
      String ret=getContents(sis);
      if(sis.hasWarnings()) {
        this.contentWarnings=sis.getWarnings();
      }
      return ret;
    } catch (CoreException ce) {
      IIQPlugin.logException("DeployArtifactCommandHandler.getContents("+file.getName()+") : CoreException ", ce);
      return "";
    }


  }

  private String getContents(InputStream is) {
    StringBuffer buf=new StringBuffer();
    try {
      byte[] bytes=new byte[1024];
      int bytesread=-1;
      while( (bytesread=is.read(bytes)) !=-1 ) {
        buf.append(new String(bytes, 0, bytesread));
      }
    } catch (Exception e) {
      IIQPlugin.logException("DeployArtifactCommandHandler.getContents: Exception reading from InputStream : ", e);
    }
    try {
      is.close();
    } catch (Exception ioe) {}
    return buf.toString();

  }

}
