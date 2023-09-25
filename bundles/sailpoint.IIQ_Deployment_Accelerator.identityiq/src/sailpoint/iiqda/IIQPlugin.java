package sailpoint.iiqda;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ctc.wstx.evt.WDTD;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.IIQDAConstants;
import sailpoint.iiqda.declaration.DeclarationElement;
import sailpoint.iiqda.declaration.ReferenceDeclaration;
import sailpoint.iiqda.declaration.StepDeclaration;
import sailpoint.iiqda.exceptions.InvalidPreferenceTypeException;
import sailpoint.iiqda.internal.RuleRegistry;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 * The IIQ-specific pieces have been abstracted so that on lazy initialisation
 * They will only be instantiated if we have specified the location of identityiq.jar
 * 
 */

public class IIQPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ"; //$NON-NLS-1$

  public static final QualifiedName RULEREGISTRYTYPE = new QualifiedName(PLUGIN_ID, "RULEREGISTRYTYPE");

  public static final String DEFAULTREGISTRY = "DEFAULTREGISTRY";

  public static final QualifiedName RULE_NAME = new QualifiedName(PLUGIN_ID, "RULE_NAME");

  public static final QualifiedName RULE_LANGUAGE = new QualifiedName(PLUGIN_ID, "RULE_LANGUAGE");

  public static final QualifiedName RULE_TYPE = new QualifiedName(PLUGIN_ID, "RULE_TYPE");

  public static final QualifiedName RULE_DESCRIPTION = new QualifiedName(PLUGIN_ID, "RULE_DESCRIPTION");

  public static final QualifiedName RULE_LIBRARY_REFERENCES = new QualifiedName(PLUGIN_ID, "RULE_LIBRARY_REFERENCES");

  public static final QualifiedName RULE_RETURN_TYPE = new QualifiedName(PLUGIN_ID, "RULE_RETURN_TYPE");

  public static final QualifiedName RULE_RETURN_NAME = new QualifiedName(PLUGIN_ID, "RULE_RETURN_NAME");

  public static final QualifiedName PROPERTY_CERTSTORE = new QualifiedName(PLUGIN_ID, "certStore");
  public static final QualifiedName PROPERTY_CERTSTORE_PASSWORD = new QualifiedName(PLUGIN_ID, "certStorePassword");

  private static final boolean DEBUG_PLUGIN = "true".equalsIgnoreCase(Platform
      .getDebugOption(PLUGIN_ID+"/debug/Plugin"));

  // The shared instance
  private static IIQPlugin plugin;
  private IEclipsePreferences prefs;

  private static RuleRegistry rr;

  public static final String BEANSHELL_SOURCE = PLUGIN_ID+".content.BEANSHELL_SOURCE";

  public static String MISSING_RESOURCE_PROBLEM_TYPE=PLUGIN_ID+".referenceproblem";

  public static final String CONSIDER_USING_CDATA_MARKER_TYPE = PLUGIN_ID+".considerUsingCDATA";  
  public static final String IIQ_PROBLEM_MARKER_TYPE = PLUGIN_ID+".iiqProblem";
  public static final String MISMATCH_CLOSING_CDATA_MARKER_TYPE = PLUGIN_ID+".mismatchedClosingCDATAProblem";
  public static final String MISMATCH_CLOSING_ELEMENT_MARKER_TYPE = PLUGIN_ID+".mismatchedClosingElementProblem";
  public static final String MISMATCH_OPENING_CDATA_MARKER_TYPE = PLUGIN_ID+".mismatchedOpeningCDATAProblem";
  public static final String RULE_PROBLEM_MARKER_TYPE = PLUGIN_ID+".ruleproblem";
  public static final String RULE_REFERENCE_MISSING_MARKER_TYPE = PLUGIN_ID+".missingReferenceProblem";
  public static final String RULE_REFERENCE_INVALID_CLASS = PLUGIN_ID+".invalidRuleReferenceClassProblem";

  // Object Types that can be imported. We take the list from ClassLists.MajorClasses and exclude a few
  // that we don't care about (or don't have names) e.g. Identity or RoleScorecard
  public static final String IMPORTABLE_CLASSES="AccountGroup,ActivityDataSource,Application,AuditConfig,Bundle,BundleArchive,Category,Capability,Certification,CertificationArchive,CertificationDefinition,CertificationGroup,Configuration,CorrelationConfig,Custom,DashboardContent,DashboardLayout,Dictionary,DynamicScope,EmailTemplate,Form,FullTextIndex,GroupFactory,GroupDefinition,IdentityArchive,IdentityRequest,IdentityTrigger,IdentityDashboard,IntegrationConfig,LocalizedAttribute,MessageTemplate,MiningConfig,ObjectConfig,PasswordPolicy,Policy,QuickLink,Request,RequestDefinition,RightConfig,RoleMiningResult,Rule,RuleRegistry,Scope,ScoreConfig,SPRight,ServiceDefinition,ServiceStatus,Target,TargetSource,TaskDefinition,TimePeriod,UIConfig,Workflow,WorkflowCase,WorkflowRegistry,WorkflowTestSuite,Workgroup,WorkItem,WorkItemArchive";

//  private IResourceChangeListener _changeListener;

  public static RuleRegistry getRuleRegistry() {
    if(rr!=null) {
      return rr;
    }
    rr=new RuleRegistry();
    return rr;
  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    plugin = this;
    if(DEBUG_PLUGIN) {
      IIQPlugin.logDebug("IIQPlugin.start: ");
    }
    super.start(context);
    //iiqLoader=new IIQClassLoader((org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader)this.getClass().getClassLoader());

    // load Plugin settings
    prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
    
//    _changeListener = new MyResourceChangeReporter();
//    ResourcesPlugin.getWorkspace().addResourceChangeListener(
//       _changeListener, IResourceChangeEvent.POST_CHANGE);

  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    if(DEBUG_PLUGIN) {
      IIQPlugin.logDebug("IIQPlugin.stop: ");
    }
    plugin = null;
//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(_changeListener);
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static IIQPlugin getDefault() {
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

  public static String getDTD(IFile file) {
    XMLInputFactory fac = XMLInputFactory.newInstance();
    XMLEventReader stream = null;

    String dtd=null;
    XMLEvent event;
    try {
      fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
      stream = fac.createXMLEventReader( file.getContents(true) );
      while(stream.hasNext()) {
        event=stream.nextEvent();
        if(event.getEventType()==XMLStreamConstants.DTD) {
          DTD theDtd=(DTD)event;          
          dtd=theDtd.getDocumentTypeDeclaration();
          WDTD wdtd=(WDTD)theDtd;
          dtd=wdtd.getSystemId();
          break;
        }
      }
    } catch (XMLStreamException xmle) {
      IIQPlugin.logException("IIQPlugin.getDTD: XMLException", xmle);
      return null;
    } catch (CoreException ce) {
      IIQPlugin.logException("IIQPlugin.getDTD: CoreException", ce);
      return null;
    } finally {
      try { 
        stream.close();
      } catch (Throwable t){}
    }
    return dtd;
  }

  public static DeclarationElement getElementAt(String content, int offset) {
    XMLInputFactory fac = XMLInputFactory.newInstance();
    XMLEventReader stream = null;

    DeclarationElement artifactName=null;
    XMLEvent event;
    try {
      fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
      stream = fac.createXMLEventReader( new StringReader(content) );
      while(stream.hasNext()) {
        event=stream.nextEvent();
        int eventLoc = event.getLocation().getCharacterOffset();
        if(event.isStartElement()) {
          // Is our offset before the start of the next event?
          if(stream.hasNext()) stream.next(); // Start Element always followed by close start element?
          int eventEnd = stream.peek().getLocation().getCharacterOffset();
          if(eventLoc<=offset && offset<eventEnd) {
            //Is it a reference? If so get the "name" attribute
            StartElement s=event.asStartElement();
            String localPart = s.getName().getLocalPart();
            if("Reference".equals(localPart)) {
              Attribute attr=s.getAttributeByName(new QName("name"));
              if(DEBUG_PLUGIN) {
                IIQPlugin.logDebug("name of reference is "+attr.getValue());
              }
              return new ReferenceDeclaration(attr.getValue());
            } else if ("Transition".equals(localPart)) {
              Attribute attr=s.getAttributeByName(new QName("to"));
              if(DEBUG_PLUGIN) {
                IIQPlugin.logDebug("name of transition to step is "+attr.getValue());
              }
              return new StepDeclaration(attr.getValue());
            }
          }
        }
      }
    } catch (XMLStreamException xmle) {
      IIQPlugin.logException("IIQPlugin.getElementAt: XMLException", xmle);
      return null;
    } finally {
      try { 
        stream.close();
      } catch (Throwable t){}
    }
    return artifactName;
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
      IIQPlugin.logException("IIQPlugin.getElementAt: XMLException", xmle);
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
          if(name.endsWith(IIQDAConstants.TARGET_SUFFIX) && !name.equals("reverse.target.properties")) {
            String target = name.substring(0, name.indexOf(IIQDAConstants.TARGET_SUFFIX));
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

  public static String[] getExcludedDirectories(IResource resource) throws CoreException {
    IResource prjResource=(IResource)resource.getProject();
    String sProps=prjResource.getPersistentProperty(
        new QualifiedName("", IIQPreferenceConstants.P_EXCLUDED_DIRECTORIES));
    if(sProps==null) return new String[0];
    return sProps.split(";");
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
    IIQPlugin plug = getDefault();
	if (plug!=null) {
		ILog log = plug.getLog();
		log.log(new Status(sev, PLUGIN_ID, Status.OK, msg, e));
	}
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
}
