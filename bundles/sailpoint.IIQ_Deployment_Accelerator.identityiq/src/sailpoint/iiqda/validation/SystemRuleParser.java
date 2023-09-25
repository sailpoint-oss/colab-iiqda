package sailpoint.iiqda.validation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;

public class SystemRuleParser {

	private InputStream inStream;

	private Map<String,String> systemRules;

	public SystemRuleParser(InputStream inStream) {
		this.inStream=inStream;
		systemRules=new HashMap<String,String>();
	}
	
	public Map<String,String> getRuleList() {
		return systemRules;
	}

	public void parse() throws XMLArtifactParserException {
		XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
		XMLStreamReader2 stream = null;
		fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
		// Doesn't work: fac.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

		try {
			stream = (XMLStreamReader2)fac.createXMLStreamReader( new InputStreamReader(inStream) );
			boolean inSource=false;
			StringBuilder sourceCode=null;
			boolean isCDATA=false;

			String artifactName=null;

			while( stream.hasNext()  ) {
				int evtCode=stream.next();
				if(evtCode==XMLStreamConstants.START_ELEMENT) {
					String tagName = stream.getLocalName();
          if( "Rule".equals(tagName) ) {
						artifactName=stream.getAttributeValue(null, "name");
					} else if("Source".equals(tagName)) {
						inSource=true;
						sourceCode=new StringBuilder();
					}

				}
				if((evtCode==XMLStreamConstants.CDATA||evtCode==XMLStreamConstants.CHARACTERS)
						&& inSource) {
					// Don't take CDATA-ness from last CDATA section.. Take it from any text section
					// within the Source section - this covers the case where there is white space before
					// or after the CDATA section.
					isCDATA=(evtCode==XMLStreamConstants.CDATA)||isCDATA;

					String text = stream.getText();
					sourceCode.append( text );
				}
				if(evtCode==XMLStreamConstants.END_ELEMENT) {
					if(inSource) {
						inSource=false;
						if (artifactName!=null) {
						  // if artifactName is not null, it means that this <Source> was found in a Rule
						  // otherwise, it was a <Source> from some object we're not interested in (e.g.
						  // an <OptionsScript> in a report
						  systemRules.put( artifactName, sourceCode.toString());
						  // So that we don't overwrite a real Rule entry with whatever non-rule script comes
						  // next
						  artifactName=null;
						}
					}
				}
			}
			stream.close();
		} catch (XMLStreamException xmle) {
		  IIQPlugin.logException("SystemRuleParser.parse: XMLException", xmle);
			throw new XMLArtifactParserException(xmle);
		} catch (Exception e) {
		  e.printStackTrace();
		  throw e;
		}
	}
}
