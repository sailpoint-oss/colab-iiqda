package sailpoint.iiqda.validation;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.core.internal.text.rules.StructuredTextPartitionerForXML;
import org.eclipse.wst.xml.core.text.IXMLPartitions;

import sailpoint.iiqda.IIQPlugin;


@SuppressWarnings("restriction")
public class StructuredTextPartitionerForArtifact extends
StructuredTextPartitionerForXML {

//  private static final boolean DEBUG_VALIDATION = "true".equalsIgnoreCase(Platform
//      .getDebugOption(CoreActivator.PLUGIN_ID+"/debug/Validation"));

  private final static String[] configuredContentTypes = new String[]{IXMLPartitions.XML_DEFAULT, IXMLPartitions.XML_CDATA, IXMLPartitions.XML_PI, IXMLPartitions.XML_DECLARATION, IXMLPartitions.XML_COMMENT, IXMLPartitions.DTD_SUBSET, IXMLPartitions.PROCESSING_INSTRUCTION_PREFIX, IIQPlugin.BEANSHELL_SOURCE };
  
  private String lastTagName=null;
  private boolean isClosingTag=false;

  protected void initLegalContentTypes() {
    fSupportedTypes = configuredContentTypes;
  }

  public static String[] getConfiguredContentTypes() {
    return configuredContentTypes;
  }

  public String getPartitionType(ITextRegion region, int offset) {
    //if (DEBUG_VALIDATION) CoreActivator.logDebug("getPartitonType("+region.getType()+") at char "+offset);

    IStructuredDocumentRegion regionAtCharOffset = fStructuredDocument.getRegionAtCharacterOffset(offset);
    //if (DEBUG_VALIDATION) CoreActivator.logDebug("type="+region.getType());
    
    if ( region.getType() == DOMRegionContext.XML_END_TAG_OPEN ) {
      // whatever the last tag was, it's closing now, so set to null
      // to avoid spurious BEANSHELL_SOURCE assignments
      lastTagName=null;
      isClosingTag=true;
    }
    if ( region.getType() == DOMRegionContext.XML_TAG_OPEN ) {
      // need to track if we're in an opening tag or a closing tag
      isClosingTag=false;
    }
    if ( region.getType() == DOMRegionContext.XML_TAG_NAME && !isClosingTag) {
      int txtStart = regionAtCharOffset.getStartOffset()+region.getStart();
      lastTagName = fStructuredDocument.getText().substring(txtStart, txtStart+region.getTextLength());
    }
      // This section doesn't work, and I can't figure out why
      // I want CDATA inside <Source> to be flagged as BEANSHELL_SOURCE, but if I do that
      // the error flagging doesn't work properly. The error is underlined initially, but then it isn't
      // de-underlined when the error is fixed (although the marker in the right-hand margin disappears
      // I need to fix this but to get a 1.0 out I am commenting this out. When the region is flagged
      // as XML_CDATA everything works as planned.
    if (region.getType() == DOMRegionContext.XML_CDATA_TEXT
      || region.getType() == DOMRegionContext.XML_CDATA_OPEN
      || region.getType() == DOMRegionContext.XML_CDATA_CLOSE
      || region.getType() == DOMRegionContext.XML_CONTENT
      || region.getType() == DOMRegionContext.XML_ENTITY_REFERENCE ) {
      /**
       * We want to see if the CDATA is inside a <Source> tag.
       * If it is, we want to flag this as a BEANSHELL_SOURCE type 
       */
      // Also flag regions as BEANSHELL_SOURCE if it's just normal XML_CONTENT but inside a <Source> tag
      if("<Source>".equals(getLastTagName(regionAtCharOffset))) {
        // lastTag was <Source>: Flagging this CDATA as sailpoint.iiqda.content.BEANSHELL_SOURCE
        return "org.eclipse.wst.xml.XML_CDATA";
//        return CoreActivator.BEANSHELL_SOURCE;
      }
    }
    String type=super.getPartitionType(region, offset);
    return type;
  }

  private String getLastTagName(IStructuredDocumentRegion regionAtCharOffset) {
    if(regionAtCharOffset==null) return null;
    // go back through the regions until we hit XML_TAG_NAME
    // then return it
    IStructuredDocumentRegion previous = regionAtCharOffset;
    do {
      previous=previous.getPrevious();
    } while(previous!=null && !"XML_TAG_NAME".equals(previous.getType()));
    if(previous==null) return null;
    String tag=previous.getFullText();
    return tag;
  }

}