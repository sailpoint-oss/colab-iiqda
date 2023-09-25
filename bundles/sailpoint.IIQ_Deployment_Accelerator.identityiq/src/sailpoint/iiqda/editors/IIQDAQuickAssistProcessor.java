package sailpoint.iiqda.editors;

import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.text.correction.proposals.MarkerResolutionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.resolutions.ConsiderUsingCDATAResolutionGenerator;
import sailpoint.iiqda.resolutions.MissingCloseCDATAResolutionGenerator;
import sailpoint.iiqda.resolutions.MissingOpenCDATAResolutionGenerator;
import sailpoint.iiqda.resolutions.MissingReferenceResolutionGenerator;

public class IIQDAQuickAssistProcessor implements IQuickAssistProcessor {

  private static final boolean DEBUG_PARSER = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/QuickAssist"));
  
  @Override
  public String getErrorMessage() {
    // TODO Auto-generated method stub
    if(DEBUG_PARSER) IIQPlugin.logDebug("IIQDAQuickAssistProcessor.getErrorMessage:");
    return null;
  }

  @Override
  public boolean canFix(Annotation annotation) {
    // TODO Auto-generated method stub
    if(DEBUG_PARSER) IIQPlugin.logDebug("IIQDAQuickAssistProcessor.canFix:");
    if (annotation instanceof MarkerAnnotation) {
      return true;
    }
    if(DEBUG_PARSER) IIQPlugin.logDebug("IIQDAQuickAssistProcessor.canFix: End");
    return false;
  }

  @Override
  public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
    // TODO Auto-generated method stub
    if(DEBUG_PARSER) IIQPlugin.logDebug("IIQDAQuickAssistProcessor.canAssist:");
    return false;
  }

  @SuppressWarnings("restriction")
  @Override
  public ICompletionProposal[] computeQuickAssistProposals(
      IQuickAssistInvocationContext invocationContext) {
    // TODO Auto-generated method stub
    System.out
    .println("IIQDAQuickAssistProcessor.computeQuickAssistProposals:");
    /**
     * TextInvocationContext - length, offset, and sourceviewer
     */
    ISourceViewer viewer= invocationContext.getSourceViewer();
    int documentOffset= invocationContext.getOffset(); 

    Iterator<?> iter = 
        viewer.getAnnotationModel().getAnnotationIterator();
    while (iter.hasNext()) {
      Annotation annotation = (Annotation) iter.next();
      if (annotation instanceof MarkerAnnotation) {
        IMarker marker = ((MarkerAnnotation) annotation).getMarker();

        // Is this the marker where we hit the button?
        if(DEBUG_PARSER) IIQPlugin.logDebug("docoff: "+documentOffset);
        int charStart = MarkerUtilities.getCharStart(marker);
        if(DEBUG_PARSER) IIQPlugin.logDebug("start: "+charStart);
        int charEnd = MarkerUtilities.getCharEnd(marker);
        if(DEBUG_PARSER) IIQPlugin.logDebug("end:   "+charEnd);
        if(charStart<=documentOffset && charEnd>=documentOffset) {
        try {
          String type = marker.getType();
          if(DEBUG_PARSER) IIQPlugin.logDebug("type:  "+type);
          // Is the marker one of ours
            if(MarkerUtilities.isMarkerType(marker, "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.iiqProblem")) {
              if(DEBUG_PARSER) IIQPlugin.logDebug("---- Is one of ours ----");
              boolean handled=false;
              IMarkerResolutionGenerator gen=null;
              switch (type) {
                case "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.missingReferenceProblem":
                  gen=new MissingReferenceResolutionGenerator();
                  handled=true;
                  break;
                case "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.mismatchedClosingCDATAProblem":
                  gen=new MissingOpenCDATAResolutionGenerator();
                  handled=true;
                  break;
                case "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.mismatchedOpeningCDATAProblem":
                  gen=new MissingCloseCDATAResolutionGenerator();
                  handled=true;
                  break;
                case "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.considerUsingCDATA":
                  gen=new ConsiderUsingCDATAResolutionGenerator();
                  handled=true;
                  break;
              }
              if (handled) {
                IMarkerResolution[] res=gen.getResolutions(marker);
                ICompletionProposal[] props=new ICompletionProposal[res.length];
                for(int i=0;i<res.length;i++) {
                  props[i]=new MarkerResolutionProposal(res[i], marker);
                }
                return props;
              } else {
                if(DEBUG_PARSER) IIQPlugin.logDebug("No specific fix available");
              }
            }
          } catch (Exception e) {}
        }
      }
    }
    if(DEBUG_PARSER) IIQPlugin.logDebug(new Date().toString());

    return null;
  }
}
