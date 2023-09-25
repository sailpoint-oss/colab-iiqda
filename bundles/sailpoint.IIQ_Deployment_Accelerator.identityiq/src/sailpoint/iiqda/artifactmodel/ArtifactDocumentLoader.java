package sailpoint.iiqda.artifactmodel;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser;
import org.eclipse.wst.sse.core.internal.provisional.document.IEncodedDocument;
import org.eclipse.wst.xml.core.internal.encoding.XMLDocumentLoader;

import sailpoint.iiqda.validation.StructuredTextPartitionerForArtifact;

@SuppressWarnings("restriction")
public class ArtifactDocumentLoader extends XMLDocumentLoader {
  
  @Override
  public RegionParser getParser() {
    return super.getParser();
  }

  @Override
  public IDocumentLoader newInstance() {
    return super.newInstance();
  }

  @Override
  protected void setDocumentContentsFromReader(
      IEncodedDocument structuredDocument, Reader reader) throws IOException {
    super.setDocumentContentsFromReader(structuredDocument, reader);
  }

  public IDocumentPartitioner getDefaultDocumentPartitioner() {
    return new StructuredTextPartitionerForArtifact();
  }

}
