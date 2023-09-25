package sailpoint.iiqda.contenttypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.validation.SimpleArtifactParser;

public class ContentDescriberForArtifacts implements ITextContentDescriber, IExecutableExtension {

  private static final boolean DEBUG_CONTENTTYPE = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/ContentType"));
  
  private static final QualifiedName ARTIFACT_TYPE=new QualifiedName("sailpoint.iiqda.contenttype", "artifactType");
  private static final QualifiedName[] SUPPORTED_OPTIONS = { 
    ARTIFACT_TYPE
  };
  
  private List<String> artifactTypes;
  
  @Override
  public int describe(InputStream contents, IContentDescription description)
      throws IOException {
    return describe(new InputStreamReader(contents), description);
  }

  @Override
  public QualifiedName[] getSupportedOptions() {
    return SUPPORTED_OPTIONS;
  }

  @Override
  public int describe(Reader contents, IContentDescription description)
      throws IOException {

    if(DEBUG_CONTENTTYPE) {
      IIQPlugin.logDebug("ContentDescriberForArtifacts.describe: ("+this+")");
    }
    int result = IContentDescriber.INVALID;
    if(description==null) {
      if(DEBUG_CONTENTTYPE) {
        IIQPlugin.logDebug("No description provided");
      }
    } else {
      Object artType = description.getProperty(SUPPORTED_OPTIONS[0]);
      if(DEBUG_CONTENTTYPE && artType!=null) {
        IIQPlugin.logDebug("artType="+artType.getClass().getName());
      }
    }
    
    SimpleArtifactParser parser=new SimpleArtifactParser(contents);
    try {
      parser.parse(true);
    } catch (XMLArtifactParserException e) {
      IIQPlugin.logException("Parsing artifact failed", e);
      return IContentDescriber.INDETERMINATE;
    }
    Map<String, List<String>> arts = parser.getArtifactList();
    // Let's assume here that if any object in the artifact list matches one of our types,
    // then it is valid. So just check the keys (since the keys of this map are the object types)
    if (artifactTypes==null||artifactTypes.size()==0) {
      // No Artifact type was provided in the describer
      // Do a generic check for DTD publicId = 'sailpoint.dtd'
      if( "sailpoint.dtd".equals(parser.getPublicId()) ) {
        result=IContentDescriber.VALID;
        if (description!=null) description.setProperty(ARTIFACT_TYPE, "sailpoint");        
      }
    } else {
      for(String key: arts.keySet()) {
        if(artifactTypes.contains(key)) {
          result=IContentDescriber.VALID;
          if (description!=null) description.setProperty(ARTIFACT_TYPE, key);
        }
      }
    }
    return result;
  }

  @Override
  public void setInitializationData(IConfigurationElement config,
      String propertyName, Object data) throws CoreException {
    // TODO Auto-generated method stub
    if(DEBUG_CONTENTTYPE) {
      IIQPlugin.logDebug("setInitializationData ("+this+" , "+propertyName+" , "+data);
    }
    if(artifactTypes==null) artifactTypes=new ArrayList<String>();
    if(propertyName.equals("describer")) {
      // The elements inside the describer appear to come as a hashmap of name value pairs
      // cannot duplicate the element name so needs to be csv string
      if(data instanceof Map) {
        String attrTypes=(String) ((Map)data).get("artifactType");
        if(attrTypes!=null) {
          String[] types=attrTypes.split(",");
          for(String type: types) {
            artifactTypes.add(type);
          }
        }
      }
    }
  }

}
