package sailpoint.iiqda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.exceptions.InvalidPreferenceTypeException;

/**
 * The activator class controls the plug-in life cycle
 * The IIQ-specific pieces have been abstracted so that on lazy initialisation
 * They will only be instantiated if we have specified the location of identityiq.jar
 * 
 */

public class IDNPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator.IdentityNow"; //$NON-NLS-1$

  public static final QualifiedName PROPERTY_CERTSTORE = new QualifiedName(PLUGIN_ID, "certStore");
  public static final QualifiedName PROPERTY_CERTSTORE_PASSWORD = new QualifiedName(PLUGIN_ID, "certStorePassword");

  private static final boolean DEBUG_PLUGIN = "true".equalsIgnoreCase(Platform
      .getDebugOption(PLUGIN_ID+"/debug/Plugin"));

  // The shared instance
  private static IDNPlugin plugin;
  private IEclipsePreferences prefs;

  public static final String TARGET_SUFFIX = ".target.properties";

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    if(DEBUG_PLUGIN) {
      IDNPlugin.logDebug("IDNPlugin.start: ");
    }
    super.start(context);
    //iiqLoader=new IIQClassLoader((org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader)this.getClass().getClassLoader());

    // load Plugin settings
    prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
    plugin = this;
    
//    _changeListener = new MyResourceChangeReporter();
//    ResourcesPlugin.getWorkspace().addResourceChangeListener(
//       _changeListener, IResourceChangeEvent.POST_CHANGE);

  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    if(DEBUG_PLUGIN) {
      IDNPlugin.logDebug("IDNPlugin.stop: ");
    }
//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(_changeListener);
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static IDNPlugin getDefault() {
    return plugin;
  }

  public void setPreference(CoreUtils.Preference preferenceName, Object preferenceValue) {
    if(preferenceValue instanceof String) {
      prefs.put(preferenceName.getType(), (String)preferenceValue);
    }
    else throw new InvalidPreferenceTypeException(preferenceValue.getClass().getName());
  }

  /**
   * Get an ImageDescriptor for the for specified plugin
   * image.
   */
  public static ImageDescriptor getIconImageDescriptor(String imageName) {
    ImageDescriptor img = null;

    try {
      URL pluginURL = getDefault().getBundle().getEntry("/");
      URL imgURL = new URL(pluginURL, "icons/" + imageName);

      img = ImageDescriptor.createFromURL(imgURL);
    } catch (MalformedURLException e) {
      //Activator.log(IStatus.INFO, "getIconImage", e);
    }

    return img;
  }

  // simpleParse generates a List of XML elements with their attributes
  // the elements in the list have the name of the element, and a map of attribute key/value pairs
  public static class SimpleElement {
    private String elementName;
    private Map<String,String> attributes;
    private int charOffset;

    public SimpleElement(String name, Map<String,String> attributes) {
      this(name, attributes, -1);
    }

    public SimpleElement(String name, Map<String,String> attributes, int charOffset) {
      this.elementName=name;
      this.attributes=attributes;
      this.charOffset=charOffset;
    }

    public String getName() {
      return elementName;
    }

    public String getAttribute(String key) {
      return attributes.get(key);
    }

    public int getOffset() {
      return charOffset;
    }
  }

  @SuppressWarnings("unchecked")
  public static List<SimpleElement> simpleParse(String content) {
    XMLInputFactory fac = XMLInputFactory.newInstance();
    XMLEventReader stream = null;

    //DeclarationElement artifactName=null;
    XMLEvent event;

    List<SimpleElement> elements=new ArrayList<SimpleElement>();
    try {
      fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
      stream = fac.createXMLEventReader( new StringReader(content) );
      while(stream.hasNext()) {
        event=stream.nextEvent();
        int eventLoc = event.getLocation().getCharacterOffset();
        if(event.isStartElement()) {
          StartElement s=event.asStartElement();
          String localPart = s.getName().getLocalPart();
          Iterator<Attribute> attrIter=s.getAttributes();
          Map<String,String> attrs=new HashMap<String,String>();
          if(attrIter!=null) {
            while(attrIter.hasNext()) {
              Attribute attr=(Attribute)attrIter.next();
              attrs.put(attr.getName().getLocalPart(), attr.getValue());
            }
          }
          elements.add(new SimpleElement(localPart, attrs, eventLoc));
        }
      }
    } catch (XMLStreamException xmle) {
      IDNPlugin.logException("IDNPlugin.getElementAt: XMLException", xmle);
      return null;
    } finally {
      try { 
        stream.close();
      } catch (Throwable t){}
    }
    return elements;
  }

  public static List<String>getTargetEnvironments(IProject prj) {

    List<String>environments=new ArrayList<String>();

    try {
      IResource[] members=prj.members();
      for(IResource member: members) {
        if(member instanceof IFile) {
          IFile fMember=(IFile)member;
          String name = fMember.getName();
          if(name.endsWith(IDNPlugin.TARGET_SUFFIX) && !name.equals("reverse.target.properties")) {
            String target = name.substring(0, name.indexOf(IDNPlugin.TARGET_SUFFIX));
            environments.add(target);
          }
        }
      }
    } catch (CoreException e) {
      // do an error
    }
    return environments;
  }

  public boolean getBooleanPreference(String pref) {
    IPreferenceStore store=plugin.getPreferenceStore();
    return store.getBoolean(pref);
  }

  public void setBooleanPreference(String prefName, boolean value) {
    IPreferenceStore store=plugin.getPreferenceStore();
    store.setValue(prefName, value);
    plugin.savePluginPreferences();
  }

  public static int countLF(String region) {
    int lastIndex=0;
    int count=0;
    while(lastIndex != -1){
      lastIndex = region.indexOf('\n', lastIndex);
      if( lastIndex != -1){
        count ++;
        lastIndex++;
      }
    }
    return count;

  }

  // I've made logDebug and logTrace separate, in case we want to do something
  // different with them later

  public static void logDebug(String msg) {
    log(Status.INFO, msg, null);
  }

  public static void logTrace(String msg) {
    log(Status.INFO, msg, null);
  }

  public static void logError(String msg) {
    log(Status.ERROR, msg, null);
  }

  public static void logException(String msg, Exception e) {
    log(Status.ERROR, msg, e);
  }

  public static void log(int sev, String msg, Exception e) {
    getDefault().getLog().log(new Status(sev, PLUGIN_ID, Status.OK, msg, e));
  }

  public static String getStringValue(Node source) {
		StringBuilder sb=new StringBuilder();
		NodeList childNodes = source.getChildNodes();
		if (childNodes!=null) {
			for(int i=0;i<childNodes.getLength(); i++) {
				Node text=childNodes.item(i);
				if(text.getNodeType()==Node.TEXT_NODE) {
					sb.append( ((Text)text).getTextContent() );
				} else if (text.getNodeType()==Node.CDATA_SECTION_NODE) {
					sb.append( ((CDATASection)text).getTextContent() );
				}
			}
		}
	  return sb.toString();
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
      throw new CoreException(new Status(IStatus.ERROR, IDNPlugin.PLUGIN_ID, "TransformerException writing DOM Document"));
    }

  }
}
