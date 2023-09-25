package sailpoint.iiqda.artifactmodel;

import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.modelhandler.XMLModelLoader;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.IIQDAConstants;

@SuppressWarnings("restriction")
public class ArtifactModelLoader extends XMLModelLoader {

  private static final boolean TRACE_CREATEMODEL = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQDAConstants.PLUGIN_ID+"/trace/ArtifactModelLoader/createModel"));

  @Override
  public IStructuredModel createModel() {
    return super.createModel();
  }

  @Override
  public IStructuredModel createModel(IStructuredDocument structuredDocument,
      String baseLocation, IModelHandler handler) {
    
    if(TRACE_CREATEMODEL) {
      IIQPlugin.logTrace("ArtifactModelLoader.createModel(IStructuredDocument, String, IModelHandler):");
      IStructuredDocumentRegion[] regions=structuredDocument.getStructuredDocumentRegions();
      for(IStructuredDocumentRegion region: regions) {
        IIQPlugin.logTrace("------------------------------------------------");
        IIQPlugin.logTrace(region.getText());
      }
      IIQPlugin.logTrace("------------------------------------------------");
      IIQPlugin.logTrace("BaseLocation="+baseLocation);
    }
    
    IStructuredModel model=super.createModel(structuredDocument, baseLocation, handler);
    return model;
  }

  @Override
  public IStructuredModel createModel(IStructuredModel oldModel) {
    return super.createModel(oldModel);
  }

}
