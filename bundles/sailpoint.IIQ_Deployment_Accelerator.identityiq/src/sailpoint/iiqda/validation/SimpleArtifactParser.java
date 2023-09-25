package sailpoint.iiqda.validation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.DTDInfo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class SimpleArtifactParser {

  private Reader inReader;
  
  private Map<String,List<String>> systemArtifacts;
  private Map<String,List<String>> passedInList;

  private String publicId;

  private String rootName;

  private String systemId;
  
  public SimpleArtifactParser(Reader reader) {
    this(reader, null);
  }
  
  public SimpleArtifactParser(InputStream inStream) {
    this(inStream, null);
  }
  
  public SimpleArtifactParser(InputStream inStream, Map<String,List<String>> passed) {
    this.inReader=new InputStreamReader(inStream);
    this.passedInList=passed;
    systemArtifacts=new HashMap<String,List<String>>();
  }

  public SimpleArtifactParser(Reader inReader, Map<String,List<String>> passed) {
    this.inReader=inReader;
    this.passedInList=passed;
    systemArtifacts=new HashMap<String,List<String>>();
  }

  public Map<String,List<String>> getArtifactList() {
    return systemArtifacts;
  }

  public void parse() throws XMLArtifactParserException {
    parse(false);
  }
  
  public void parse(boolean stopAtFirstArtifact) throws XMLArtifactParserException {
    XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
    XMLStreamReader2 stream = null;
    fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    // Doesn't work: fac.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

    try {
      stream = (XMLStreamReader2) fac.createXMLStreamReader( inReader );

      String artifactName=null;
      boolean inArtifact=false;
      boolean finished=false;
      String artTag=null;
      
      while( stream.hasNext() && !finished ) {
        int evtCode=stream.next();
        if(evtCode==XMLStreamConstants.DTD) {
          DTDInfo dtd=stream.getDTDInfo();
          publicId=dtd.getDTDPublicId();
          rootName=dtd.getDTDRootName();
          systemId=dtd.getDTDSystemId();
        }
        if(evtCode==XMLStreamConstants.START_ELEMENT) {
          String tagName = stream.getLocalName();
          if(!tagName.equals("sailpoint") && !tagName.equals("ImportAction") && !inArtifact) {
            inArtifact=true;
            artTag=tagName;
            artifactName=stream.getAttributeValue(null, "name");
            if(artifactName!=null) {
              addToList(systemArtifacts, artTag, artifactName);
              if(passedInList!=null) addToList(passedInList, artTag, artifactName);
              // if we only want to see what artifact is in this file, we can stop now..
              if (stopAtFirstArtifact) finished=true;
            }
          }
        }
        if(evtCode==XMLStreamConstants.END_ELEMENT) {
          if(inArtifact && stream.getLocalName().equals(artTag)) {
            inArtifact=false;
            artTag=null;
          }
        }
      }
      stream.close();
    } catch (XMLStreamException xmle) {
      IIQPlugin.logException("SimpleArtifactParser.parse: XMLException", xmle);
      throw new XMLArtifactParserException(xmle);
    }
  }
  
  private void addToList(Map<String,List<String>> theList, String type, String name) {
    List<String> names=theList.get(type);
    if(names==null) names=new ArrayList<String>();
    if (!names.contains(name)) {
      names.add(name);
      theList.put(type,  names);
    }
  }

  public String getPublicId() {
    return publicId;
  }

  public String getRootName() {
    return rootName;
  }

  public String getSystemId() {
    return systemId;
  }

}
