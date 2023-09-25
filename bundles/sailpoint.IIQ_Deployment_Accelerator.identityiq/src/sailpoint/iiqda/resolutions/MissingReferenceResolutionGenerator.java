package sailpoint.iiqda.resolutions;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;

public class MissingReferenceResolutionGenerator implements IMarkerResolutionGenerator {

  private static final boolean DEBUG_RESOLUTIONS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Resolutions"));
  
  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {

    
    try {
      IProject project=(IProject)marker.getAttribute("project");
      List<String> environments=IIQPlugin.getTargetEnvironments(project);
      IMarkerResolution[] resolutions=new IMarkerResolution[environments.size()];
      
      for(int i=0;i<environments.size();i++) {
        resolutions[i]=new GetMissingReferenceResolution(project, (String)marker.getAttribute("refName"), (String)marker.getAttribute("refType"), environments.get(i));
      }
      return resolutions;
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException generating Resolutions", e);
      return null;
      
    }
    
    
  }

  public class GetMissingReferenceResolution implements IMarkerResolution {

    private String refName;
    private String refType;
    private String environment;
    private IProject project;

    public GetMissingReferenceResolution(IProject project, String refName, String refType, String environment) {
      this.project=project;
      this.refName=refName;
      this.refType=refType;
      this.environment=environment;
    }

    @Override
    public String getLabel() {
      return "Attempt to get "+refType+" '"+refName+"' from "+environment+" environment";
    }

    @Override
    public void run(IMarker marker) {

      IFile f=project.getFile(refType+"-"+CoreUtils.toCamelCase(refName, true)+".xml");
      try {
        IIQRESTClient client=new IIQRESTClient(project, environment);
        String obj=client.getObject(refType, refName);
        if(obj!=null) {
          f.create(new ByteArrayInputStream(obj.getBytes()), true, null);
          System.out.println("Rebuilding..");
          // remove markers from object
          marker.getResource().deleteMarkers(IIQPlugin.PLUGIN_ID+".iiqProblem", true, IResource.DEPTH_INFINITE);
          project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        } else {
          if (DEBUG_RESOLUTIONS) {
            IIQPlugin.logDebug("Object Not found "+refName+" on "+environment);            
          }
        }
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ConnectionException e) {
        // TODO: Is this the best way to do it? Should/can we abstract this out to a more generic dialog
        // runner, that we can pass a run method to, and do get/set for the attributes required by the run?
        class DlgRunner implements Runnable {
          private ConnectionException e;
          public DlgRunner(ConnectionException e) {
            this.e=e;}
          public void run() {
            Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            CoreUtils.showConnectionError(activeShell, e);
          }
        }
        DlgRunner dlg=new DlgRunner(e);
        PlatformUI.getWorkbench().getDisplay().asyncExec(dlg);
      }
    } 
  }

}
