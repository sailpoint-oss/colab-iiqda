package sailpoint.iiqda.editors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.assist.ArtifactContentAssistProcessor;


public class ArtifactSourceViewerConfiguration extends
StructuredTextViewerConfigurationXML {

  private static final boolean DEBUG_CONFIGURATION = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/ArtifactSourceViewerConfiguration"));

  static IEditorPart editor;
  
  
  public ArtifactSourceViewerConfiguration() {
    super();
  }
  
  public ArtifactSourceViewerConfiguration(IEditorPart part) {
    super();
    editor=part;
  }
  
  @Override
  public IQuickAssistAssistant getQuickAssistAssistant(
      ISourceViewer sourceViewer) {
    //IQuickAssistAssistant qa = super.getQuickAssistAssistant(sourceViewer);
    IQuickAssistAssistant qa = new QuickAssistAssistant();
    qa.setQuickAssistProcessor(new IIQDAQuickAssistProcessor());
    qa.setInformationControlCreator(getInformationControlCreator(sourceViewer));
    return qa;
  }

  @Override
  protected IContentAssistProcessor[] getContentAssistProcessors(
      ISourceViewer sourceViewer, String partitionType) {
    if(DEBUG_CONFIGURATION) {
      IIQPlugin.logDebug("ArtifactSourceViewerConfiguration.getContentAssistProcessors: ("+partitionType+")");
    }
    IContentAssistProcessor[] procs=super.getContentAssistProcessors(sourceViewer, partitionType);
    if(IIQPlugin.BEANSHELL_SOURCE.equals(partitionType)
        // See comments in StructuredTextPartitionerForArtifact#getPartitionType for vvv
        || "org.eclipse.wst.xml.XML_CDATA".equals(partitionType)) {
      
      ContentAssistant assistant=this.getContentAssistant();
      assistant.enableAutoActivation(true);
      assistant.enableAutoInsert(true);
      assistant.setAutoActivationDelay(500);
      
      //IContentAssistListener listener
      //assistant.set
      
      IContentAssistProcessor[] newprocs=new IContentAssistProcessor[procs.length+1];
      System.arraycopy(procs, 0, newprocs, 0, procs.length);
      newprocs[procs.length]=new ArtifactContentAssistProcessor(editor);
      return newprocs;
    }
    return procs;
  }

  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    String[] parentTypes = super.getConfiguredContentTypes(sourceViewer);
    String[] types=new String[parentTypes.length+1];
    types[0]=IIQPlugin.BEANSHELL_SOURCE;
    System.arraycopy(parentTypes, 0, types, 1, parentTypes.length);
    return types;
  }

//  
//  public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
//    return IDocumentExtension3.DEFAULT_PARTITIONING;
//  }
  
}
