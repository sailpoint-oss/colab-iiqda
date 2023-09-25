package sailpoint.iiqda.deployer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.IIQDAConstants;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;


@SuppressWarnings("restriction")
public class HotDeployJob extends Job {

  private static final boolean TRACE_DEPLOY = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQDAConstants.PLUGIN_ID+"/trace/Deploy"));

  private List<ICompilationUnit> culist;
  private String environment;

  private IProject parentProject;

  private List<String> contentWarnings;
  private boolean trace;

  public HotDeployJob(List<ICompilationUnit> filelist, String environment) throws CoreException {
    super("Hot-Deploy Classes");

    this.culist=filelist;
    this.environment=environment;
    if(filelist!=null) {
      ICompilationUnit f=filelist.get(0);
      parentProject=f.getJavaProject().getProject();
    }
    this.contentWarnings=null;
    setUser(true);
  }



  @Override
  protected void canceling() {
    // TODO Auto-generated method stub
    if(trace) {
      IIQPlugin.logTrace("HotDeployJob.canceling:");
    }
    super.canceling();
  }

  public IStatus run(IProgressMonitor monitor) { 

    IFile f=parentProject.getFile(environment+IIQDAConstants.TARGET_SUFFIX);
    Properties props=new Properties();
    try {
      props.load(f.getContents());
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (CoreException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    String debugTransport = (String)props.get(IIQPreferenceConstants.P_DEBUG_TRANSPORT);
    if(debugTransport==null) debugTransport="dt_socket";
    String host = (String)props.get(IIQPreferenceConstants.P_URL);
    if(host!=null) {
      try {
        URI uri=new URI(host);
        host=uri.getHost();
      } catch (URISyntaxException e) {
        // TODO pop a message and quit
      }
    }
    String sDebugPort = (String)props.get(IIQPreferenceConstants.P_DEBUG_PORT);

    monitor.beginTask("Deploying classes...", 2*culist.size()+1); 
    monitor.worked(1);
    
    VirtualMachineManager mgr=Bootstrap.virtualMachineManager();
    List<AttachingConnector> atConns=mgr.attachingConnectors();
    com.sun.jdi.VirtualMachine vm=null;
    
    for (AttachingConnector conn: atConns) {
      if (conn.transport().name().equals(debugTransport)) {
        Map<String, Argument> args=conn.defaultArguments();
        // conn passes back a default map of arguments; we have to populate those values
        Argument arg=args.get("port");
        arg.setValue(sDebugPort);
        args.put("port", arg);
        
        arg=args.get("hostname");
        arg.setValue(host);
        args.put("hostname", arg);
        
        try {
          vm=conn.attach(args);
        } catch (Exception e) {
          IIQPlugin.logException("Couldn't connect to debug port: ", e);
          IStatus stat=CoreUtils.toErrorStatus("Couldn't connect to debug port - "+e);
          return stat;
        }
      }
    }
    
    for ( ICompilationUnit objectFile : culist ) {

      // Get the classes for the selected Java files
      IRegion region=JavaCore.newRegion();
      region.add(objectFile);
      IResource[] resources=JavaCore.getGeneratedResources(region, false);
      
      monitor.setTaskName("Reading "+objectFile.getElementName());
      if(TRACE_DEPLOY) {
        IIQPlugin.logTrace("Reading "+objectFile.getElementName());
      }
      try
      {
        Map<ReferenceType,byte[]> replacers=new HashMap<ReferenceType,byte[]>();
        for (int i=0; i<resources.length; i++) {
          // Generate the replacer entry in the map
          
          IResource res=resources[i];
          IClassFile clazzFile=JavaCore.createClassFileFrom((IFile) res);
          byte[] clazz=clazzFile.getBytes();
          monitor.worked(1);
  
  
          monitor.setTaskName("Deploying "+objectFile.getElementName());
          if(TRACE_DEPLOY) {
            IIQPlugin.logTrace("Deploying "+objectFile.getElementName());
          }
          // Get the fully qualified class name
  
          String fullyQualifiedName = objectFile.getAllTypes()[i].getFullyQualifiedName(); // TODO fix this for multiple classes in file?
          List<ReferenceType> types=vm.classesByName(fullyQualifiedName);
          if (types.size()>0) {
            replacers.put(types.get(0), clazz);
          }
        }
        
        // Send the definition
        
        vm.redefineClasses(replacers);
        monitor.worked(1);
      } catch (Exception e) {
        IStatus stat=CoreUtils.toErrorStatus("Unable to deploy - "+e);
        IIQPlugin.logException("Exception during deploy ", e);
        return stat;

      }

    }

    vm.dispose();
    monitor.done(); 
    return Status.OK_STATUS; 
  }

}
