package sailpoint.iiqda.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.validation.IArtifactElement;
import sailpoint.iiqda.validation.IArtifactRootElement;
import sailpoint.iiqda.validation.RuleElement;
import sailpoint.iiqda.validation.SimpleArtifactListVisitor;
import sailpoint.iiqda.validation.SimpleRuleListVisitor;
import sailpoint.iiqda.validation.XMLIFileArtifactParser;
import sailpoint.iiqda.validation.XMLIFileValidator;

public class IIQArtifactBuilder extends IncrementalProjectBuilder {

  public static final String BUILDER_ID = IIQPlugin.PLUGIN_ID+".iiqArtifactBuilder";

  private static final boolean TRACE_BUILDER = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/trace/Builder"));

  private Map<String,List<String>> allTheArtifacts=null;
  private Map<String,String> allTheRules=null;
  
  class SampleDeltaVisitor implements IResourceDeltaVisitor {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean visit(IResourceDelta delta) throws CoreException {

      if(TRACE_BUILDER) {
        IIQPlugin.logTrace("IIQArtifactBuilder.delta.visit");
      }
      IResource resource = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          if(TRACE_BUILDER) {
            IIQPlugin.logTrace("Delta.added");
          }
          // handle added resource
          checkResource(resource);
          break;
        case IResourceDelta.REMOVED:
          if(TRACE_BUILDER) {
            IIQPlugin.logTrace("Delta.removed");
          }
          // handle removed resource
          break;
        case IResourceDelta.CHANGED:
          if(TRACE_BUILDER) {
            IIQPlugin.logTrace("Delta.changed");
          }
          // handle changed resource
          checkResource(resource);
          break;
      }
      //return true to continue visiting children.
      return true;
    }
  }

  class SampleResourceVisitor implements IResourceVisitor {
    public boolean visit(IResource resource) {
      try {
        checkResource(resource);
      } catch (CoreException ce) {
        IIQPlugin.logException("IIQArtifactBuilder.SampleResourceVisitor.visit", ce);
        return false; //TODO: Do something clever here
      }
      //return true to continue visiting children.
      return true;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
   *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
      throws CoreException {

    if(TRACE_BUILDER) {
      IIQPlugin.logTrace("IIQArtifactBuilder.build");
    }
    if (kind == FULL_BUILD) {
      if(TRACE_BUILDER) {
        IIQPlugin.logTrace("IIQArtifactBuilder.build: FULL");
      }
      // Clear out our cache of artifacts
      allTheArtifacts=null;
      allTheRules=null;
      long startTime=System.currentTimeMillis();
      fullBuild(monitor);
      long endTime=System.currentTimeMillis();
      if(TRACE_BUILDER) {
        IIQPlugin.logTrace("Full Build Time: "+(endTime-startTime)+" ms");
      }
    } else {
      if(TRACE_BUILDER) {
        IIQPlugin.logTrace("IIQArtifactBuilder.build: Incremental");
      }
      IResourceDelta delta = getDelta(getProject());
      if (delta == null) {
        fullBuild(monitor);
      } else {
        incrementalBuild(delta, monitor);
      }
    }
    return null;
  }

  void checkResource(IResource resource) throws CoreException {

    // prefilter
    String lcName = resource.getName().toLowerCase();
    IIQPlugin.logDebug("checkResource: "+resource.getName());
    if(!lcName.endsWith(".xml")) {
      return;
    }
    if(lcName.endsWith("hbm.xml")) {
      // Ignore Hibernate definition files; they are not real XML anyway (no root element)
      return;
    }

    String[] dirs=IIQPlugin.getExcludedDirectories(resource);
    for(String dir: dirs) {
      if(resource.getProjectRelativePath().toString().startsWith(dir)) {
        // it's in the excluded dirs
        return;
      }
    }

    IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
    if((resource instanceof IFile)) {

      IFile fResource=(IFile)resource;
      IJavaProject project=JavaCore.create(resource.getProject());
      InputStream fStream=fResource.getContents();
      IContentType[] types=null;
      try {
        types=contentTypeManager.findContentTypesFor(fStream, fResource.getName());
        fStream.close();
      } catch (IOException ioe) {
        IIQPlugin.logException("Finding Content Types for "+fResource.getName(),ioe);
        return;
      }

      boolean ourType=false;
      for(IContentType type: types) {
        if(type.getId().equals(IIQPlugin.PLUGIN_ID+".content.Artifact")) {
          ourType=true;
          break;
        }
      }
      if(!ourType) {
        // Not an artifact. Skip out now
        return;
      }

      // Get rid of current markers
      try {
        resource.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
      } catch (CoreException e2) {
        // Finding markers failed
      }
      // Make sure we have a list of all the resources in the current project
      //if(allTheArtifacts==null) {
        SimpleArtifactListVisitor salv=new SimpleArtifactListVisitor(IIQPlugin.getExcludedDirectories(resource));
        resource.getProject().accept(salv);
        salv.clearExcludedDirs();
        for (IProject prj: resource.getProject().getReferencedProjects()) {
          if (prj.exists()) {
            prj.accept(salv);
          }
        }
        allTheArtifacts=salv.allTheArtifacts();
      //}
      // And the rules
      //if(allTheRules==null) {
        SimpleRuleListVisitor srlv=new SimpleRuleListVisitor(IIQPlugin.getExcludedDirectories(resource));
        resource.getProject().accept(srlv);
        srlv.clearExcludedDirs();
        for (IProject prj: resource.getProject().getReferencedProjects()) {
          if (prj.exists()) {
            prj.accept(srlv);
          }
        }
        allTheRules=srlv.allTheRules();
      //}
      
      // So, what we need to do here is:
      // Validate the XML
      // Check for Source elements

      
      
      // ok, now we need to read the file in
      // including line numbers
      XMLIFileValidator v=new XMLIFileValidator(fResource);
      v.validate();			
      if(v.hasErrors()) {
        // XML errors: we can't trust the document's integrity
        // so don't bother doing beanshell checking
        return;
      }

      XMLIFileArtifactParser p=new XMLIFileArtifactParser(fResource, allTheArtifacts, allTheRules);
      try {
        p.parse();
        p.validateReferences(project);
      } catch (final XMLArtifactParserException pe) {
        final String path=fResource.getFullPath().toString();
        // TODO: Throw up some kind of error message
        Display.getDefault().asyncExec(new Runnable() {
          @Override
          public void run() {
            IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = iw.getShell();
            ErrorDialog.openError(shell,
              "Build Error",
              null, 
              new Status(IStatus.ERROR, IIQPlugin.PLUGIN_ID,
                  "Build Error in "+path+"\n"+pe.getMessage()
              )
            );
          }
        });
        return;
      } catch (NullPointerException npe) {
        IIQPlugin.logException("npe", npe);
      }

      List<IArtifactRootElement> artifacts=p.getArtifacts();
      // There may be more than one source element in this artifact
      for (IArtifactRootElement artifact: artifacts) {
        if (artifact instanceof RuleElement) {
          // Don't parse Rules if they are not beanshell
           if (!"beanshell".equals( ((RuleElement)artifact).getRuleLanguage()) ){
            fResource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            continue;
          }
        }
        // For each source element, do the compile and mark all the problems
        List<SourceElement> elements=((IArtifactElement)artifact).getSourceElements();
        if(elements==null) return;
        for (SourceElement se: elements) {
          if(!"ConnectorAfterCreate".equals(se.getRuleType())) {
            /*List<IMarker> problems=*/p.parseSourceElement(se, project);
          }
        }
      }
    }
  }

  protected void fullBuild(final IProgressMonitor monitor)
      throws CoreException {
    try {
      getProject().accept(new SampleResourceVisitor());
    } catch (CoreException e) {
    }
  }

  protected void incrementalBuild(IResourceDelta delta,
      IProgressMonitor monitor) throws CoreException {
    // the visitor does the work.
    delta.accept(new SampleDeltaVisitor());
  }

  protected IJavaProject getJavaProject(IFile file) throws CoreException {
    if (file != null) {
      IProject p= file.getProject();
      if (p.getNature(JavaCore.NATURE_ID) != null) {
        return JavaCore.create(p);
      }
    }
    return null;
  }
}


