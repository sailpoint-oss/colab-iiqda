package sailpoint.iiqda.artifactmodel;

import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader;
import org.eclipse.wst.xml.core.internal.modelhandler.ModelHandlerForXML;

@SuppressWarnings("restriction")
public class ModelHandlerForArtifact extends ModelHandlerForXML {
  
  private static final String ASSOCIATED_CONTENT_TYPE_ID = "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.content.Artifact";
  private static final String MODEL_HANDLER_ID = "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.content.Artifact.modelhandler";

  public ModelHandlerForArtifact() {
    //super();
    setId(MODEL_HANDLER_ID);
    setAssociatedContentTypeId(ASSOCIATED_CONTENT_TYPE_ID);
  }

  @Override
  public IDocumentLoader getDocumentLoader() {
    return new ArtifactDocumentLoader();
  }

  @Override
  public IModelLoader getModelLoader() {
    return new ArtifactModelLoader();
  }
  
}
