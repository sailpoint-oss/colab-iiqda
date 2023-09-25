package sailpoint.iiqda.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;
import org.eclipse.core.runtime.CoreException;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.validation.BSIssue.IssueSeverity;

public class BaseXMLValidator {

  private boolean hasErrors=false;

  public boolean hasErrors() {
    return hasErrors;
  }
  
  protected List<BSIssue> validateStream(InputStream inStream) throws CoreException {
    
    List<BSIssue> issues=new ArrayList<BSIssue>();
    
    XMLInputFactory fac = XMLInputFactory2.newInstance();
    XMLStreamReader stream = null;
    fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    Stack<Location> locations=new Stack<Location>();
    Stack<String> elements=new Stack<String>();
    
    try {
      stream = fac.createXMLStreamReader( new InputStreamReader(inStream) );
      while(stream.hasNext()) {
        int evtCode=stream.next();
        if(evtCode==XMLStreamConstants.START_ELEMENT) {
          locations.push(stream.getLocation());
          elements.push(stream.getLocalName());
        } else if(evtCode==XMLStreamConstants.END_ELEMENT) {
            locations.pop();
            elements.pop();
        }        
      }
      stream.close();
      try {
        inStream.close();
      } catch (IOException ioe) {
        // something went wrong closing the stream. i don't care
      }
    } catch (XMLStreamException xmle) {
      IIQPlugin.logException("TestStaxErrorHandling", xmle);
      // Grr.. Now we have to figure out what the error was from the damn message,
      // and then figure out how 'long' the error is
      Location loc = xmle.getLocation();
      String message=xmle.getMessage();
      int startChar=0;
      int length=0;
      
      String markerType=null;
      // sometimes we want to put extra attributes on the marker
      Map<String,Object> extraAttributes=new HashMap<String,Object>();
      
      if(loc!=null) {
        startChar=loc.getCharacterOffset();
      }
      if(message.startsWith("Unexpected close tag")) {
        // message looks like
        //    Unexpected close tag </Script>; expected </Source>.
        // so do some maths
        int openEl=message.indexOf('<');
        int closeEl=message.indexOf('>');
        int openSecondEl=message.lastIndexOf('<');
        int closeSecondEl=message.lastIndexOf('>');
        length=closeEl-openEl;
        startChar=startChar-length; // char offset refers to end of element
        length+=2; //+2 for lt/gt
        markerType=IIQPlugin.MISMATCH_CLOSING_ELEMENT_MARKER_TYPE;
        String expectedElement = message.substring(openSecondEl+2, closeSecondEl);
        String foundElement = message.substring(openEl+2, closeEl);
        message="Missing close tag: expected </"+expectedElement+"> - found </"+foundElement+">";
        extraAttributes.put("expectedElement", expectedElement);
        extraAttributes.put("foundElement", foundElement);
        extraAttributes.put("location", startChar);
      } else if (message.startsWith("String ']]>'")) {
        markerType=IIQPlugin.MISMATCH_CLOSING_CDATA_MARKER_TYPE;
        startChar-=2; // go back to start of thingy
        length=3;
        message="found closing CDATA tag ']]>' without opening tag '<![CDATA['";
        extraAttributes.put("previousOpeningElementlocation", locations.peek().getCharacterOffset());
        extraAttributes.put("previousOpeningElementName", elements.peek());
        extraAttributes.put("location", startChar);
      } else if (xmle instanceof WstxUnexpectedCharException) {
        length=1;
      } else if (xmle instanceof WstxEOFException && message.contains("CDATA")) {
        markerType=IIQPlugin.MISMATCH_OPENING_CDATA_MARKER_TYPE;
        startChar=locations.peek().getCharacterOffset()+2+elements.peek().length();
        length=9; // for <![CDATA[
        message="Unmatched opening <![CDATA[ after element <"+elements.peek()+">";
        extraAttributes.put("previousOpeningElementlocation", locations.peek().getCharacterOffset());
        extraAttributes.put("previousOpeningElementName", elements.peek());
        
      }  else {
        message="Unknown XML Error: "+message;
        length=0;
      } 
      
      
      if(markerType==null) markerType=IIQPlugin.RULE_PROBLEM_MARKER_TYPE;
      BSIssue issue=new BSIssue(markerType, IssueSeverity.ERR, startChar, length, loc.getLineNumber(), message);
      // add the extra attributes (if any) {
      for (String key: extraAttributes.keySet()) {
        Object value=extraAttributes.get(key);
        issue.setAttribute(key, value);
      }
      issues.add(issue);

      hasErrors=true;
    }
    return issues;
  }

}
