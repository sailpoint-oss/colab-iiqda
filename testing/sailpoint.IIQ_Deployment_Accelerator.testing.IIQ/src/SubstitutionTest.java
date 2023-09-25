import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

import sailpoint.iiqda.core.SubstitutingInputStream;

public class SubstitutionTest {

  InputStream inStream;
  Properties properties;
  
  public static void main(String[] args) throws Exception {
    SubstitutionTest rst=new SubstitutionTest();
    rst.testSubstitution(); ;
  }
  
  
  private void prepareReaders(String xmlFile, String propFile) throws Exception {
    if (inStream!=null) {
      try {
        inStream.close();
      } catch (Exception e) {}
    }
    inStream=new FileInputStream("resources/"+xmlFile);
    
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
  public void testSubstitution() throws Exception {

    Document docApplication = null;

    // Issue #175
    // Substitute back complex xml. attribute->element of same name
    try {
      prepareReaders("Application-AD.substituted.xml", "Substitution.properties");
      
      Enumeration<Object> keys=properties.keys();
      while (keys.hasMoreElements()) {
        String key=(String) keys.nextElement();
        System.out.println(key+"  :  "+properties.getProperty(key));
      }
      InputStream substIS=new SubstitutingInputStream(properties, inStream);
      
      // Read the file back in
      DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
      // Don't validate against the DTD here.. That can be done as part of the builder
      // Besides, if we're importing from IdentityIQ, it won't go into Hibernate unless
      // it's valid according to the DTD..
      dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
      DocumentBuilder builder = dbfact.newDocumentBuilder();
      docApplication = builder.parse(new InputSource(substIS));
      
      // Test for Issue #174
      // Check Application/@featuresString is %%FEATURES%%
      Element app=findElement(docApplication.getDocumentElement(), "Application");
      if (app==null) {
        throw new Exception("no <Application> element found in result document");
      }
      
      // Test for Issue #175
      // See if we took out <value> element and replaced with @value
      String testXPath="/Application[@name='Active Directory']/Attributes/Map/entry[@key='authSearchAttributes']/value";    
      XPathFactory xPathfactory = XPathFactory.newInstance();
      // Set up the XPath
      XPath xpath = xPathfactory.newXPath();
      Node valueNode=(Node)xpath.evaluate(testXPath, docApplication.getDocumentElement(), XPathConstants.NODE);
      
      if (valueNode==null) {
        throw new Exception("no <value> element found in result document");      
      }
      Element valueEl=(Element)valueNode;
      NodeList listnodes=valueEl.getElementsByTagName("List");
      if (listnodes==null) {
        throw new Exception("No <List> found in <value> element in result document");
      }
      assertEquals(1, listnodes.getLength());
      
      listnodes=((Element)listnodes.item(0)).getElementsByTagName("String");
      if (listnodes==null) {
        throw new Exception("No <String> found in <List> element in result document");
      }
      assertEquals(3, listnodes.getLength());
    } catch (Exception e) {
      System.out.println("------ Result -------");
      printDocument(docApplication, System.out);
      System.out.println("---------------------");
      throw e;
    }
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
