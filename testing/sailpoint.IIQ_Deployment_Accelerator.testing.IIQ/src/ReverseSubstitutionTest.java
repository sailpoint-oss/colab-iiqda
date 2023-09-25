import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sailpoint.iiqda.core.CoreUtils;

public class ReverseSubstitutionTest {

  Reader reader;
  Properties properties;
  
  public static void main(String[] args) throws Exception {
    ReverseSubstitutionTest rst=new ReverseSubstitutionTest();
    rst.testTargetedAttributeChange();
  }
  
  
  private void prepareReaders(String xmlFile, String propFile) throws Exception {
    if (reader!=null) {
      try {
        reader.close();
      } catch (Exception e) {}
    }
    InputStream is=new FileInputStream("resources/"+xmlFile);
    reader=new InputStreamReader(is);
    
    InputStream propsIS=new FileInputStream("resources/"+propFile);
    properties=new Properties();
    try {
      properties.load(propsIS);
      propsIS.close();
    } catch (IOException ioe) {
      throw new Exception("Can't load properties file");
    }
  }
  
  @Test
  public void testTargetedAttributeChange() throws Exception {
    
    // This was added for Bug #174
    // You should be able to do 
    // /Application[@name\='Active\ Directory']/@featuresString=%%FEATURES%%
    
    prepareReaders("Application-AD.xml", "TargetedFeaturesString.properties");
//    prepareReaders("Application-AD.xml", "keith.target.properties");
    
    Enumeration<Object> keys=properties.keys();
    while (keys.hasMoreElements()) {
      String key=(String) keys.nextElement();
      System.out.println(key+"  :  "+properties.getProperty(key));
    }
    Reader rdr=CoreUtils.doReverseSubstitution(reader, properties);
    
    // Read the file back in
    DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
    // Don't validate against the DTD here.. That can be done as part of the builder
    // Besides, if we're importing from IdentityIQ, it won't go into Hibernate unless
    // it's valid according to the DTD..
    dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    DocumentBuilder builder = dbfact.newDocumentBuilder();
    Document docApplication = builder.parse(new InputSource(rdr));
    
    // Test for Issue #174
    // Check Application/@featuresString is %%FEATURES%%
    Element app=findElement(docApplication.getDocumentElement(), "Application");
    if (app==null) {
      throw new Exception("no <Application> element found in result document");
    }

    String featStr=app.getAttribute("featuresString");
    System.out.println("featuresString="+featStr);
    assertEquals("%%FEATURES%%", featStr);

    
    System.out.println("------ Result -------");
    printDocument(docApplication, System.out);
    System.out.println("---------------------");
    // Test for Issue #175
    // See if we took out <value> element and replaced with @value
    String testXPath="/Application[@name='Active Directory']/Attributes/Map/entry[@key='authSearchAttributes']/value";    
    XPathFactory xPathfactory = XPathFactory.newInstance();
    // Set up the XPath
    XPath xpath = xPathfactory.newXPath();
    String result=(String)xpath.evaluate(testXPath, docApplication.getDocumentElement(), XPathConstants.STRING);
    assertEquals("%%AUTHSEARCHATTRIBUTES%%", result); 
    
  }
  
  private Element findElement(Element el, /*String namespace,*/ String tag) {

    System.out.println("el.getNodeName="+el.getNodeName());
    if(el.getNodeName().equals(tag)) {
      return el;
    }
    NodeList childs=el.getChildNodes();
    if (childs!=null) {
      for (int i=0; i<childs.getLength(); i++) {
        Node n=childs.item(i);
        if(n instanceof Element) {
          Element test=(Element)n;
          if (tag.equals(el.getNodeName())) {
            return el;
          } else {
            Element child=findElement(test, tag);
            if (child!=null) {
              return child;
            }
          }
        }
      }
    }

    return null;
  }
 
  private void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    transformer.transform(new DOMSource(doc), 
         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
}
  
}
