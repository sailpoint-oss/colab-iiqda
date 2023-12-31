package sailpoint.iiqda.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.exceptions.DetailedConnectionException;

public class CoreUtils {

  private static final boolean DEBUG_UTILS = "true".equalsIgnoreCase(Platform
      .getDebugOption(CorePlugin.PLUGIN_ID+"/debug/CoreUtils"));

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator"; //$NON-NLS-1$

  private static ImageRegistry imageRegistry;
  
  public enum Preference {
    USE_SSB_TEMPLATE("Use_SSB_Template");

    private String type;

    private Preference(String type) {
      this.type=type;
    }

    public String getType() {  
      return type;  
    }

  }
  
  public ImageRegistry getImageRegistry() {
    if (imageRegistry == null) {
      imageRegistry = createImageRegistry();
    }
    return imageRegistry;
  }

  protected ImageRegistry createImageRegistry() { 
    //If we are in the UI Thread use that
    if(Display.getCurrent() != null) {
      return new ImageRegistry(Display.getCurrent());
    }

    if(PlatformUI.isWorkbenchRunning()) {
      return new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
    }

    //Invalid thread access if it is not the UI Thread 
    //and the workbench is not created.
    throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
  }

  public static InputStream docAsStream(Document doc) throws CoreException {
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
  
      DocumentType doctype = doc.getDoctype();
      if (doctype!=null) {
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
      }
      
      ByteArrayOutputStream pOut=new ByteArrayOutputStream();
  
      Result result=new StreamResult(pOut);     
      DOMSource domSource = new DOMSource(doc);
  
      transformer.transform(domSource, result);
      byte[] transformed=pOut.toByteArray();
      ByteArrayInputStream pIn=new ByteArrayInputStream(transformed);
      
      return pIn;
          
    } catch (TransformerException e) {
      throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "TransformerException writing DOM Document"));
    }
  
  }

  /**
   * @since 2.1
   */
  public static Reader doReverseSubstitution(Reader stream, IProject project) throws CoreException, IOException {
    IFile f=project.getFile("reverse"+IIQDAConstants.TARGET_SUFFIX);
    Properties props=new Properties();
    if(f.exists()) {
      props.load(f.getContents());
    }

    return doReverseSubstitution(stream, props);
  }

  public static Reader doReverseSubstitution(Reader stream, Properties props) throws CoreException, IOException {
    try {
      XPathFactory xPathfactory = XPathFactory.newInstance();
      DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
      // Don't validate against the DTD here.. That can be done as part of the builder
      // Besides, if we're importing from IdentityIQ, it won't go into Hibernate unless
      // it's valid according to the DTD..
      dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
      DocumentBuilder builder = dbfact.newDocumentBuilder();
      Document indexname_input = builder.parse(new InputSource(stream));
      indexname_input.setXmlStandalone(true);
  
      // Set up the XPath
      XPath xpath = xPathfactory.newXPath();
      for (Object xp: props.keySet()) {
  
        String sXPathExpr=(String)xp;
        String xpathValue=props.getProperty((String)xp);
        // Set up the input
        Object result=xpath.evaluate(sXPathExpr, indexname_input.getDocumentElement(), XPathConstants.NODESET);
        if (DEBUG_UTILS) CorePlugin.logDebug("result="+result.getClass().getName());
        if(result instanceof NodeList) {
          NodeList nl=(NodeList)result;
          if (DEBUG_UTILS) CorePlugin.logDebug(sXPathExpr+" : found "+nl.getLength()+" nodes");
          for(int i=0;i<nl.getLength();i++) {
            if (DEBUG_UTILS) CorePlugin.logDebug(sXPathExpr+" : "+i);
            
            Node n=nl.item(i);

            // Issue #175
            // If XPath ends in /value, replace the <value> element with @value={substitution string} 
//            if (sXPathExpr.endsWith("/value")) {
//              if (DEBUG_UTILS) {
//                CorePlugin.logDebug("Replacing <value> with @value");
//              }
//              Node parent = n.getParentNode();
//              parent.removeChild(n);
//              ((Element)parent).setAttribute("value", xpathValue);
            if (n.getNodeType()==Node.ELEMENT_NODE) {
              if (DEBUG_UTILS) {
                CorePlugin.logDebug("Replacing Element contents with text");
              }
              Text neuContent=n.getOwnerDocument().createTextNode(xpathValue);
              while (n.hasChildNodes())
                n.removeChild(n.getFirstChild());
              n.appendChild(neuContent);
              
            } else {
              n.setNodeValue(xpathValue);
            }
          }
        }
      }
      return documentAsStream(indexname_input, false);
    } catch (XPathExpressionException | ParserConfigurationException e) {
      IStatus status=toErrorStatus("XPathExpressionException "+e);
      throw new CoreException(status);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    return null;
  
  
  }

  public static String join(Collection<String> c, String delimiter) {
    if (null == c)
      return null;
  
    StringBuffer buf = new StringBuffer();
    Iterator<String> iter = c.iterator();
    while ( iter.hasNext() ) {
      buf.append(iter.next());
      if ( iter.hasNext() )
        buf.append(delimiter);
    }
    return buf.toString();
  
  }

  public static Reader stringDocumentAsStream(String obj, boolean shouldCDATASource) {
    DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
    // Don't validate against the DTD here.. That can be done as part of the builder
    // Besides, if we're importing from IdentityIQ, it won't go into Hibernate unless
    // it's valid according to the DTD..
    dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    try {
      DocumentBuilder builder = dbfact.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(obj)));
      return documentAsStream(doc, shouldCDATASource);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static Reader documentAsStream(Document doc, boolean shouldCDATASource) {
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    try {
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DocumentType doctype = doc.getDoctype();
      if(doctype != null) {
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
      }
      if(shouldCDATASource) {
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "Source");
      }
      // TODO: Should be able to do this here:
      // transformer.setOutputProperty("{http://xml.apache.org/xalan}line-separator","\n");
      // but it doesn't work. Come back and look into this when we run out of real problems
      System.setProperty("line.separator", "\n");
      transformer.transform(domSource, result);
      return new StringReader(writer.toString());
    } catch (TransformerConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  
  }

  public static MessageConsole findConsole(String name) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++)
      if (name.equals(existing[i].getName()))
        return (MessageConsole) existing[i];
    //no console found, so create a new one
    MessageConsole myConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[]{myConsole});
    return myConsole;
  }

  public static StringBuilder readFile(IFile file) throws CoreException{
    StringBuilder buf=new StringBuilder();
    InputStream is = null;
    try {
      is=file.getContents();
      byte[] cbuf=new byte[1024];
      int numread=-1;
      while((numread=is.read(cbuf)) !=-1 ) {
        buf.append(new String(cbuf, 0, numread));
      }
    } catch (IOException ioe) {
      //IDNActivator.logException("IDNActivator.ReadFile: IOException", ioe);
      throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "IOException reading "+file.getName()));
    }
    try {
      if(is!=null) is.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return buf;
  }

  public static IFile getActiveWorkbenchFile() {
    // There seems to be no other way to get the active editor (and by extension
    // the IPath
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = 
        workbench == null ? null : workbench.getActiveWorkbenchWindow();
    IWorkbenchPage activePage = 
        window == null ? null : window.getActivePage();

    IEditorPart editor = 
        activePage == null ? null : activePage.getActiveEditor();
    IEditorInput input = 
        editor == null ? null : editor.getEditorInput();
    IFile file = input instanceof FileEditorInput 
        ? ((FileEditorInput)input).getFile()
            : null;
        return file;        
  }

  public static IStatus toErrorStatus(String message) {
    IStatus stat=new Status(
        IStatus.ERROR,
        "IIQ Plugin",
        message);
    return stat;
  }

  public static IStatus toWarningStatus(String message) {
    IStatus stat=new Status(
        IStatus.WARNING,
        "IIQ Plugin",
        message);
    return stat;
  }

  public static void throwCE(String msg) throws CoreException{
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, 
        msg, null);
    throw new CoreException(status);

  }

  public static void throwAsCE(String msg, Throwable t) throws CoreException{
    IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, 
        msg, t);
    throw new CoreException(status);

  }

  public static String toCamelCase(String name) {
    return toCamelCase(name, false);
  }
  public static String toCamelCase(String name, boolean isUpper) {
    if(name==null) return null;

    StringBuilder camel=new StringBuilder();
    boolean upper=isUpper;
    for(int i=0;i<name.length();i++) {
      char c=name.charAt(i);
      if(c==' ') upper=true;
      else {
        if(upper) {
          camel.append(Character.toUpperCase(c));
          upper=false;
        } else {
          camel.append(Character.toLowerCase(c));
        }
      }
    }
    return camel.toString();
  }

  public static void showConnectionError(Shell shell, ConnectionException e) {
    IStatus errors=null;      
    if (e instanceof DetailedConnectionException) {
      List<String> errs=((DetailedConnectionException)e).getErrors();
      IStatus[] statii=new IStatus[errs.size()];
      for(int i=0;i<errs.size();i++) {
        statii[i]=toErrorStatus(errs.get(i));
      }
      errors=new MultiStatus(PLUGIN_ID, 1, statii, "Server-side error", null);
    } else {
      errors=toErrorStatus(e.getMessage());
    }
    ErrorDialog.openError(
        shell,
        "IIQ Plugin",
        "Connection Error:",
        errors);
    return;
  }

  public static String camelCase(String name) {

    if(name==null) return null;

    StringBuffer camel=new StringBuffer();
    boolean upper=false;
    for(int i=0;i<name.length();i++) {
      char c=name.charAt(i);
      if(c==' ') upper=true;
      else {
        if(upper) {
          camel.append(Character.toUpperCase(c));
          upper=false;
        } else {
          camel.append(Character.toLowerCase(c));
        }
      }
    }
    return camel.toString();
  }

  public static String capitalize(String str) {
    return str.substring(0,1).toUpperCase()+str.substring(1);
  }


}
