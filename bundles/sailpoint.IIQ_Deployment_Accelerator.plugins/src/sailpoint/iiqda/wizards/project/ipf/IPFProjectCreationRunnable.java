package sailpoint.iiqda.wizards.project.ipf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.PartInitException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import sailpoint.iiqda.IPFPlugin;
import sailpoint.iiqda.builder.IPFNature;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.SubstitutingInputStream;
import sailpoint.iiqda.exceptions.ProjectCreationFailedException;
import sailpoint.iiqda.wizards.project.Capability;
import sailpoint.iiqda.wizards.project.ListEntry;
import sailpoint.iiqda.wizards.project.RESTEndpoint;
import sailpoint.iiqda.wizards.project.ServiceDescription;
import sailpoint.iiqda.wizards.project.Setting;
import sailpoint.iiqda.wizards.project.SnippetDefinition;
import sailpoint.iiqda.wizards.project.SnippetDefinition.CSSInclude;
import sailpoint.iiqda.wizards.project.SnippetDefinition.JSInclude;
import sailpoint.iiqda.wizards.project.Snippets;
import sailpoint.iiqda.wizards.project.WidgetDescription;

/**
 * @author kev
 *
 */
/**
 * @author kev
 *
 */
/**
 * @author kev
 *
 */
public class IPFProjectCreationRunnable implements IRunnableWithProgress {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IPFPlugin.PLUGIN_ID+"/debug/Wizards"));

  private String projectName;
  private URI locationURI;
  private PageOneData pageOne;
  private PageDataFullPage pdFullPage;
  private Snippets snippets;
  private PageDataREST pdREST;
  private PageDataServices pdServices;
  private PageDataRights pdRights;
  private PageDataLibraries pdLibs;
  private PageDataWidgets pdWidgets;
  
  private List<String> spRights;

  private IProject theProject;

  private List<Capability> caps;

  private List<RESTResource> restResources;

  public IPFProjectCreationRunnable(
      String projectName,
      URI locationURI,
      PageOneData pageOne,
      PageDataFullPage pdFullPage,
      Snippets snippets,
      PageDataServices pdServices,
      List<Capability> caps,
      PageDataREST pdREST,
      PageDataWidgets pdWidgets,
      PageDataLibraries pdLibs) {

    this.projectName=projectName;
    this.locationURI=locationURI;
    this.pageOne=pageOne;
    this.pdFullPage=pdFullPage;
    this.snippets=snippets;
    this.pdServices=pdServices;
    this.caps=caps;
    this.pdREST=pdREST;
    this.pdWidgets=pdWidgets;
    this.pdLibs=pdLibs;

    spRights=new ArrayList<String>();
  }

  @Override
  public void run(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {

    if (DEBUG_WIZARDS) {
      IPFPlugin.logDebug("Project Creation:");
    }

    SubMonitor subMonitor=SubMonitor.convert(monitor, 100);
    IProject project=null;
    try {
      project=createProjectFromProject(projectName,
          locationURI,
          subMonitor.newChild(10)); // Take 10 of our 100 ticks for this
      createProjectContents(project, subMonitor.newChild(85));

      project.refreshLocal(IProject.DEPTH_INFINITE, subMonitor.newChild(5)); // the last 5 ticks

      // if all is well, persist the IIQ location for next time..
      String iiqloc=pdLibs.getIIQLocation();
      IPFPlugin.getDefault().setPreference(IPFPlugin.LAST_USED_IIQ_LOCATION, iiqloc);

    } catch (ProjectCreationFailedException ce) {
      System.out.println("Project Creation Failed: "+ce);
      throw new InterruptedException(); // this does the same as cancel
    } catch (CoreException ce) {
      throw new InterruptedException();
    } catch (OperationCanceledException oce) {
      throw new InterruptedException();
    } finally {
      monitor.done();
    }
    this.theProject=project;
  }

  private void createProjectContents(IProject project, IProgressMonitor monitor) throws ProjectCreationFailedException {

    SubMonitor subMonitor=SubMonitor.convert(monitor, 42);
    
    subMonitor.subTask("Creating Manifest");
    IFolder x=getFolder(project, "web");

    Document doc;
    try {
      // 1 tick
      doc = createDocument("Plugin");
    } catch (ParserConfigurationException e1) {
      throw new ProjectCreationFailedException("Can't create document 'Plugin': "+e1);
    }
    subMonitor.worked(1);
    
    restResources = new ArrayList<RESTResource>();
    
    try {
      // 1 tick
      Element attrMap = setPluginAttributes(monitor, doc);
      spRights.add(pageOne.getPluginRight());
      subMonitor.worked(1);

      // 5 ticks
      subMonitor.subTask("creating full page");
      if (pdFullPage.isEnableFull()) {
        addFullPageEntry(doc, attrMap, pdFullPage.getFullPageName());
        createFullPage(project, pdFullPage.isUseAngular());
      }
      subMonitor.worked(5);

      if (pdWidgets.getHasWidgets()) {
        subMonitor.subTask("Adding widget configuration");
        addWidgetConfiguration();
        createWidgetsFile(project);
      }

      // 5 ticks
      subMonitor.subTask("Adding Snippet Configuration");
      addSnippetConfiguration(project, doc, attrMap);
      subMonitor.worked(5);

      // 5 ticks
      subMonitor.subTask("Adding Settings Configuration");
      addSettingsConfiguration(doc, attrMap);      
      subMonitor.worked(5);

      // 5 ticks
      if (pdREST.getHasREST()) {
        subMonitor.subTask("Adding REST configuration");
        addRESTConfiguration();
      }
      if (restResources.size()>0) {
        writeRESTConfiguration(project, attrMap);
      }
      subMonitor.worked(5);

      // 5 ticks
      subMonitor.subTask("Creating permissions file");
      createPermissionsFile(project);
      subMonitor.worked(5);
      
      // 5 ticks
      if (pdServices.getHasServices()) {
        
        subMonitor.subTask("Adding Services configuration");
        addServicesConfiguration(project, attrMap, pdServices.getServiceDescriptions());
        
      }
      subMonitor.worked(5);

      // 5 ticks
      subMonitor.subTask("writing Manifest file");
      writeDoc(x,"manifest.xml", doc, monitor);
      subMonitor.worked(5);
      
      // 5 ticks
      subMonitor.subTask("Copying DB files");
      copyDBFiles(project);
      subMonitor.worked(5);
          
    } catch (CoreException ce) {
      throw new ProjectCreationFailedException(ce.getMessage());
    }

    monitor.done();

  }
  
  
  /**
   * Widgets are composed of <Widget> SailPoint objects, JS/CSS includes (references from a snippet definition in the manifest)
   * and a REST endpoint to get/send data. This method adds snippet and REST definitions so that when those files are generated
   * later, they will be generated alongside standalone Snippets or REST endpoints 
   */
  private void addWidgetConfiguration() {
    
    for (WidgetDescription wd: pdWidgets.getWidgetDescriptions()) {
      
      // Define the basic snippet
      SnippetDefinition sd=new SnippetDefinition();
      sd.setRegex(".*\\/home\\.?(jsf|xhtml)");
      String widgetName = wd.getWidgetName();
      sd.setRightRequired(widgetName+"Right");
      
      Properties props=new Properties();
      props.put("%%widgetName%%", widgetName);
      props.put("%%UpperWidgetName%%", CoreUtils.capitalize(widgetName));
      
      List<JSInclude> includes=new ArrayList<JSInclude>();
      includes.add(new JSInclude("widgets/bundle.js", widgetName+"Bundle.js", props));
      
      sd.setScriptIncludes(includes);
      
      snippets.addSnippet(sd);
      
      List<RESTEndpoint> endpoints=new ArrayList<RESTEndpoint>();
      RESTEndpoint rp=new RESTEndpoint("GET", "message", "getMessage",
        "String", false, null); // false, null is for no specific SPRight for this endpoint
      endpoints.add(rp);
      
      RESTResource rr=new RESTResource(wd.getWidgetName().toLowerCase(),
          CoreUtils.capitalize(wd.getWidgetName())+"Resource",
          wd.getWidgetName(),
          wd.getWidgetName()+"Right",
          endpoints
      );
      rr.setSampleMessage(wd.getWidgetDescription());
      restResources.add(rr);

    }
  }

  private void copyDBFiles(IProject project) throws CoreException {
    String dbFldr = "platform:/plugin/"+IPFPlugin.PLUGIN_ID+"/resources/db";
    try {
      File f=IPFPlugin.getResource("resources/db");
      System.out.println("copyDBFiles: f="+f);
      IFolder f2=project.getFolder("db");
      System.out.println("copyDBFiles: f2="+f2);
      f2.create(true, true, null);
      recurseCopy(f, f2);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void addRESTConfiguration() throws ProjectCreationFailedException {

      RESTResource rr=new RESTResource(pageOne.getUniqueName().toLowerCase(),
          pdREST.getBaseClazzName(),
          pdREST.getBaseEndpoint(),
          pdREST.getBaseEndpointRight(),
          pdREST.getEndpoints()
      );
      restResources.add(rr);
  }
  
  private void writeRESTConfiguration(IProject project, Element attrMap) throws ProjectCreationFailedException {  
    
    Document doc = attrMap.getOwnerDocument();
    
    Element restListEl=doc.createElement("List");

    for (RESTResource rr: restResources) {
      try {
        addRestResource(project, rr); 
      } catch (CoreException ce) {
        throw new ProjectCreationFailedException("Failed to add rest resource "+rr.getBaseClazzName(), ce);
      }

      Element restName=doc.createElement("String");
      restName.appendChild(doc.createTextNode(getFullRESTPackageName(rr.getPackageName())+"."+rr.getBaseClazzName()));
      restListEl.appendChild(restName);
      
      spRights.add(rr.getBaseEndpointRight());
      for(RESTEndpoint ep: rr.getEndpoints()) {
        if (ep.hasSpecificRight()) {
          spRights.add(ep.getSpRight());
        }
      }
    }
    
    addElementAttribute(attrMap, "restResources", restListEl);

  }


  private void addRestResource(IProject project, RESTResource rr) throws CoreException, ProjectCreationFailedException {
    
    // Create the java package
    IPackageFragment pack=createPackage(project, getFullRESTPackageName(rr.getPackageName()));
    
    // Create the Java class
    CompilationUnit comp =createPluginRESTClass(project, rr.getPackageName(), rr);            
    writeAST(pack, comp, rr.getBaseClazzName());


  }
  
  private Element setPluginAttributes(IProgressMonitor monitor, Document doc) {
    Element plugin = doc.getDocumentElement();
    Element attrs=doc.createElement("Attributes");
    plugin.appendChild(attrs);
    Element attrMap=doc.createElement("Map");
    attrs.appendChild(attrMap);


    monitor.worked(1);

    monitor.subTask("updating manifest");

    // Set some attributes
    plugin.setAttribute("displayName", pageOne.getDescriptiveName());
    plugin.setAttribute("name", pageOne.getUniqueName());
    plugin.setAttribute("version", pageOne.getVersion());
    plugin.setAttribute("rightRequired", pageOne.getPluginRight());
    plugin.setAttribute("minSystemVersion", pageOne.getMinSystemVersion());

    addAttribute(attrMap, "minUpgradableVersion", pageOne.getMinUpgradeable());
    return attrMap;
  }

  private Document createDocument(String rootElementName) throws ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document doc = builder.newDocument();
    DOMImplementation domImpl = doc.getImplementation();
    DocumentType doctype = domImpl.createDocumentType(rootElementName,
        "sailpoint.dtd",
        "sailpoint.dtd");
    doc.appendChild(doctype);
    Element rootEl=doc.createElement(rootElementName);
    doc.appendChild(rootEl);
    return doc;
  }

  private void addServicesConfiguration(IProject project, Element attrMap,
      List<ServiceDescription> serviceDescriptions) throws ProjectCreationFailedException {
    
    List<String> serviceClasses=new ArrayList<String>();
    Document serviceDefinition=null;
    Element sdRootEl=null;
    try {
      serviceDefinition=createDocument("sailpoint");
      sdRootEl=serviceDefinition.getDocumentElement();
    } catch (ParserConfigurationException pce) {
      throw new ProjectCreationFailedException("Can't create services doc: "+pce);
    }
    for (ServiceDescription sd: serviceDescriptions) {
      serviceClasses.add(sd.getClassName());
      sdRootEl.appendChild(getServiceDefinitionXML(serviceDefinition, sd));
      
      String fClass=sd.getClassName();
      String sPackage=IPackageFragment.DEFAULT_PACKAGE_NAME;
      String sClassName=fClass;
      if (fClass.indexOf(".")!=-1) {
        sPackage=fClass.substring(0, fClass.lastIndexOf("."));
        sClassName=fClass.substring(fClass.lastIndexOf(".")+1);
      }
      IPackageFragment pack=createPackage(project, sPackage);

      CompilationUnit comp =createServicesClass(project, sd);

      writeAST(pack, comp, sClassName);
    }
    addAttribute(attrMap, "serviceExecutors", serviceClasses);
    try {
      writeDoc(project.getFolder("import/install"), "service.xml", serviceDefinition, null);
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Unable to write service file: "+e);
    }
  }
  
  private CompilationUnit createServicesClass(IProject project, ServiceDescription sd) throws ProjectCreationFailedException {

    // Generate a new Compilation Unit
    AST ast=AST.newAST(AST.JLS8);
    CompilationUnit unit=ast.newCompilationUnit();

    String[] splitClass=sd.getClassName().split("\\.");
    
    String[] pack=new String[splitClass.length-1];
    System.arraycopy(splitClass, 0, pack, 0, splitClass.length-1);
    
    // Generate the package declaration
    PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
    packageDeclaration.setName(ast.newName(pack));
    unit.setPackage(packageDeclaration);

    addImport(ast, unit, "org.apache.commons.logging", "Log", false);
    addImport(ast, unit, "org.apache.commons.logging", "LogFactory", false);
    addImport(ast, unit, "sailpoint.api", "SailPointContext", false);
    addImport(ast, unit, "sailpoint.tools", "GeneralException", false);

    // create class
    TypeDeclaration type=ast.newTypeDeclaration();
    String clazzName = splitClass[splitClass.length-1];
    SimpleName sn=ast.newSimpleName(clazzName);
    type.setName(sn);
    Type extendType=ast.newSimpleType(ast.newName(new String[] {"Service"}));
    type.setSuperclassType(extendType);

    unit.types().add(type);

    List mods = type.modifiers();
    
 // Add the public modifier
    mods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    
    addLogVariable(ast, type, clazzName);

    MethodDeclaration method=ast.newMethodDeclaration();
    // Create the annotations and other mods (public)
    List methodMods=method.modifiers();
    List methodExceptions=method.thrownExceptionTypes();
    
    methodMods.add( simpleAnnotation(ast, "Override", null) );
    methodMods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

    // Add the return type
    method.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

    // and the method name
    method.setName(ast.newSimpleName("execute"));
    
    SimpleName ge = ast.newSimpleName("GeneralException");
    methodExceptions.add(ast.newSimpleType(ge));

    // and a parameter
    List methodParms=method.parameters();
    SingleVariableDeclaration svd=ast.newSingleVariableDeclaration();
    svd.setType(ast.newSimpleType(ast.newName(new String[] {"SailPointContext"})));
    svd.setName(ast.newSimpleName("context"));
    methodParms.add(svd);
    
    // add a debug statement
    MethodInvocation logDebug=ast.newMethodInvocation();
    logDebug.setExpression(ast.newSimpleName("log"));
    logDebug.setName(ast.newSimpleName("debug"));
    StringLiteral msg = ast.newStringLiteral();
    msg.setLiteralValue("execute");
    logDebug.arguments().add(msg);
    Block blk=ast.newBlock();
    blk.statements().add(ast.newExpressionStatement(logDebug));

    method.setBody(blk);
    type.bodyDeclarations().add(method);
    
    return unit;
    
  }

  private Node getServiceDefinitionXML(Document doc,
      ServiceDescription sd) {
    Element service=doc.createElement("ServiceDefinition");
    service.setAttribute("name", sd.getServiceName());
    service.setAttribute("executor", sd.getClassName());
    if (sd.getInterval()!=-1) {
      service.setAttribute("interval", ""+sd.getInterval());
    }
    service.setAttribute("hosts", "global");
    if (sd.getDescription()!=null) {
      Element descr=doc.createElement("Description");
      descr.appendChild(doc.createTextNode(sd.getDescription()));
      service.appendChild(descr);
    }
    // create the 'Attributes/Map/entry' for the plugin
    Element entry=doc.createElement("entry");
    entry.setAttribute("key", "pluginName");
    entry.setAttribute("value", pageOne.getUniqueName());
    Element map=doc.createElement("Map");
    map.appendChild(entry);
    Element attrs=doc.createElement("Attributes");
    attrs.appendChild(map);
    service.appendChild(attrs);
    
    return service;
  }

  private void createFullPage(IProject project, boolean useAngular) throws ProjectCreationFailedException {
    // if the box to use Angular was not checked, we can just copy the sample over
    if (!pdFullPage.isUseAngular()) {
      copyResourceToFile( "page.xhtml", getFolder(project, "web/import/install"), "page.xhtml" );
    } else {
      // otherwise, we need to add an include for the JS file that gets build by JSPM
      // it'll look like:
      //     <script src="#{plugins.requestContextPath}/plugin/<plugin name>/ui/js/javascript-deps.js"></script>

      Document doc=readPluginDocument("page.xhtml");

      Element el=findElement(doc.getDocumentElement(), "ui:composition");

      // do these backwards, because we use 'beforeFirstChild'
      
      addscriptelement(el, pageOne.getUniqueName(), "ui/js/app.js");
      addscriptelement(el, pageOne.getUniqueName(), "ui/js/SailPointBundleLibrary.js");
      
      el=findElement(el, "div");
      el.setAttribute("ng-app", pageOne.getUniqueName());
      el.setAttribute("ng-controller", pageOne.getUniqueName()+"Controller");

      Text txt=doc.createTextNode("{{welcomeMessage}}\n");
      
      el.appendChild(txt);
      
      try {
        IFolder fldr = project.getFolder("web/ui");
        writeDoc(getFolder(fldr), "page.xhtml", doc, null);
        
        fldr=fldr.getFolder("js");
        IFile spBundle=fldr.getFile("SailPointBundleLibrary.js");
        File spBundleSrc = new File(pdLibs.getIIQLocation()+"/ui/js/bundles/SailPointBundleLibrary.js");
        try {
          if (!spBundleSrc.exists()) {
            throw new FileNotFoundException();
          }
          InputStream is=new FileInputStream(spBundleSrc);
          spBundle.create(is, true, null);
        } catch (FileNotFoundException fnfe) {
          throw new ProjectCreationFailedException("Can't find SailPointBundleLibrary.js at iiq location "+pdLibs.getIIQLocation());
        }
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  private void addscriptelement(Element el, String uniqueName, String string) {
    Element script=el.getOwnerDocument().createElement("script");
    script.setAttribute("src", "#{plugins.requestContextPath}/plugin/"+uniqueName+"/"+string);
    el.insertBefore(script, el.getFirstChild());

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

  private void addSettingsConfiguration(Document doc, Element attrMap) {
    if (pdFullPage.isEnableSettings()) {

      Element settingsList=doc.createElement("List");

      for (Setting set: pdFullPage.getSettings()) {
        if (set.getName()!=null && set.getName().length()>0) {
          Element setting=doc.createElement("Setting");
          setting.setAttribute("dataType", set.getDataType());
          setting.setAttribute("helpText", set.getHelpText());
          setting.setAttribute("label", set.getLabel());
          setting.setAttribute("name", set.getName());
          if (set.getValue()!=null && set.getValue().length()>0) {
            setting.setAttribute("defaultValue", set.getValue());
          }
          settingsList.appendChild(setting);
        }
      }
      addElementAttribute(attrMap, "settings", settingsList);
    }
  }

  private void addFullPageEntry(Document doc, Element attrMap,
      String fullPageName) {
    Element fp=doc.createElement("FullPage");
    fp.setAttribute("title", fullPageName);
    addElementAttribute(attrMap, "fullPage", fp);
  }

  private void addAttribute(Element attrMap, String key,
      String value) {
    Document doc=attrMap.getOwnerDocument();
    Element entry=doc.createElement("entry");
    entry.setAttribute("key", key);
    entry.setAttribute("value", value);
    attrMap.appendChild(entry);
  }
  
  private void addAttribute(Element attrMap, String key, List<String> value) {
    
    Document doc=attrMap.getOwnerDocument();
    Element list=doc.createElement("List");
    for (String val: value) {
      Element eVal=doc.createElement("String");
      eVal.appendChild(doc.createTextNode(val));
      list.appendChild(eVal);
    }
    addElementAttribute(attrMap, key, list);
    
  }

  private void addElementAttribute(Element attrMap, String key, Element value) {
    Document doc=attrMap.getOwnerDocument();
    Element entry=doc.createElement("entry");
    entry.setAttribute("key", key);
    Element valueEl=doc.createElement("value");
    valueEl.appendChild(value);
    entry.appendChild(valueEl);
    attrMap.appendChild(entry);
  }

  private Document readClasspathDocument(String file) throws ProjectCreationFailedException {
    Document doc=null;
    InputStream is=null;
    // however, this works for everything
    is = getClass().getResourceAsStream(file);

    if (is==null) {
      throw new ProjectCreationFailedException("can't read document "+file);
    }
    return readDocument(is);

  }

  private Document readPluginDocument(String file) throws ProjectCreationFailedException {

    InputStream inputStream=getPluginResource(file);
    if (inputStream==null) {
      throw new ProjectCreationFailedException("can't read document "+file);
    }
    return readDocument(inputStream);
  }
  
  private Document readProjectDocument(IProject project, String filePath) throws ProjectCreationFailedException {
    
    IFile file = project.getFile(filePath);
    if (!file.exists()) {
      throw new ProjectCreationFailedException("Can't find project document "+filePath);
    }
    try {
      InputStream inputStream=file.getContents();
      if (inputStream==null) {
        throw new ProjectCreationFailedException("Can't read project document "+filePath);
      }
      return readDocument(inputStream);
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Can't read project document "+filePath, e);
    }
  }
  
  private InputStream getPluginResource(String file) throws ProjectCreationFailedException{
    URL url;
    String fileURL = "platform:/plugin/"+IPFPlugin.PLUGIN_ID+"/resources/"+file;
    
    try {
      url = new URL(fileURL);
      InputStream inputStream = url.openConnection().getInputStream();
      return inputStream;
    } catch (MalformedURLException mue) {
      throw new ProjectCreationFailedException("File url is malformed: "+fileURL);
    } catch (IOException e) {
      throw new ProjectCreationFailedException("IOException reading "+file+" : "+e);
    }
    
  }
  

  private Document readDocument(InputStream is) throws ProjectCreationFailedException {

    try { 
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);


      //Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();

      //parse using builder to get DOM representation of the XML file
      Document doc = db.parse(is);
      is.close();
      return doc;
    }catch(ParserConfigurationException pce) {
      throw new ProjectCreationFailedException("Can't configure parser to read document from InputStream: "+pce);
    }catch(SAXException se) {
      throw new ProjectCreationFailedException("SAXException reading document from InputStream: "+se);
    }catch(IOException ioe) {
      throw new ProjectCreationFailedException("IOException reading document from InputStream: "+ioe);
    }
  }

  private void writeAST(IPackageFragment pack, CompilationUnit comp, String filename) throws ProjectCreationFailedException {
    try {
      ICompilationUnit iCu = pack.createCompilationUnit(filename+".java", comp.toString(), true, null);

      CodeFormatter cf=ToolFactory.createCodeFormatter(null);
      String code = iCu.getSource();
      TextEdit te=cf.format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, System.getProperty("line.separator"));

      iCu.becomeWorkingCopy(null);
      iCu.applyTextEdit(te, null);
      iCu.commitWorkingCopy(true,  null);
      try {
        JavaUI.openInEditor(iCu);
      } catch (PartInitException pie) {
        IPFPlugin.logError("Can't open new class: "+pie);
      }


    } catch (JavaModelException e) {
      throw new ProjectCreationFailedException("Can't create java file: "+e);
    }
  }

  private void createPermissionsFile(IProject project) throws ProjectCreationFailedException {
    IFolder folder = getInstallFileFolder(project);

    try {

      Element sailpointEl=createSailPointDoc();
      
      for (String spRight: spRights) {
        addSPRight(sailpointEl, spRight);
      }
      for(Capability cap: caps) {
        addCapability(sailpointEl, cap);
      }
      writeDoc(folder, "Permissions.xml", sailpointEl.getOwnerDocument(), null);

    } catch (CoreException ce) {
      throw new ProjectCreationFailedException("unable to write installation doc: "+ce);
    }
  }

  private void createWidgetsFile(IProject project) throws ProjectCreationFailedException {
    IFolder folder = getInstallFileFolder(project);
    
    try {
      Element sailpointEl=createSailPointDoc();
      
      for (WidgetDescription wd: pdWidgets.getWidgetDescriptions()) {
        addWidgetElement(sailpointEl, wd);
        // This is now done in the Wizard page so the rights are available for the Capabilities page
        // spRights.add(wd+"Right");
      }
      writeDoc(folder, "Widgets.xml", sailpointEl.getOwnerDocument(), null);
      
    } catch (CoreException ce) {
      throw new ProjectCreationFailedException("unable to write installation doc: "+ce);
    }
  }
  
//  private void addWidgetREST(IProject project, WidgetDescription wd) throws ProjectCreationFailedException {
//    
//    List<RESTEndpoint> endpoints=new ArrayList<RESTEndpoint>();
//    RESTEndpoint rp=new RESTEndpoint("GET", "message", "getMessage",
//      "String", false, null); // false, null is for no specific SPRight for this endpoint
//    endpoints.add(rp);
//    
//    CompilationUnit comp=createRESTClass(project,
//        "sailpoint.plugin."+wd.getWidgetName()+".rest",
//        CoreUtils.capitalize(wd.getWidgetName())+"Resource",
//        wd.getWidgetName()+"Right",
//        CoreUtils.capitalize(wd.getWidgetName()),
//        endpoints
//    );
//    writeAST(pack, comp, baseClazzName);
//
//  }
  
  /*
   * <Snippet regexPattern=".*\/home\.?(jsf|xhtml)" rightRequired="MostEntitlementUsersPluginRight">
              <Scripts>
                <String>ui/js/AbstractListCtrl.js</String>
                <String>ui/js/MostEntitlementsUser.js</String>
                <String>ui/js/MostEntitlementsUserService.js</String>
                <String>ui/js/MostEntitlementsUserWidgetDirective.js</String>
                <String>ui/js/MostEntitlementsUserWidgetDirectiveCtrl.js</String>
   */
  
  /**
   * Creates a new Document object, with the correct DTD setting, and a 
   * @return the 'sailpoint' element in the created doc
   * @throws ProjectCreationFailedException
   */
  private Element createSailPointDoc() throws ProjectCreationFailedException {

    try {
      // create doc
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
  
      //Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
  
      //parse using builder to get DOM representation of the XML file
      Document doc = db.newDocument();
  
      DOMImplementation domImpl = doc.getImplementation();
      DocumentType doctype = domImpl.createDocumentType("sailpoint",
          "sailpoint.dtd",
          "sailpoint.dtd");
      doc.appendChild(doctype);
  
      Element sailpointEl=doc.createElement("sailpoint");
      doc.appendChild(sailpointEl);
      
      return sailpointEl;
      
    } catch (ParserConfigurationException e) {
      throw new ProjectCreationFailedException("unable to build installation doc: "+e);
    }

  }
  
  private IFolder getInstallFileFolder(IProject project)
      throws ProjectCreationFailedException {
    IFolder folder=getFolder(project, "import/install");
    return folder;
  }

  private void addCapability(Element parent, Capability cap) {
    Document doc=parent.getOwnerDocument();

    Element capEl=doc.createElement("Capability");
    capEl.setAttribute("displayName", cap.getName());
    capEl.setAttribute("name", cap.getName());
    Element rrEl=doc.createElement("RightRefs");
    for (String right: cap.getRights()) {
      Element rEl=doc.createElement("Reference");
      rEl.setAttribute("class", "sailpoint.object.SPRight");
      rEl.setAttribute("name", right);
      rrEl.appendChild(rEl);
    }
    capEl.appendChild(rrEl);
    parent.appendChild(capEl);
  }

  private void addSPRight(Element parent, String spRight) {

    Document doc=parent.getOwnerDocument();

    Element spEl=doc.createElement("SPRight");
    spEl.setAttribute("displayName", spRight);
    spEl.setAttribute("name", spRight);
    parent.appendChild(spEl);
  }

  private void addWidgetElement(Element parent, WidgetDescription wd) {
    
    Document doc=parent.getOwnerDocument();
    
    Element spEl=doc.createElement("Widget");
    spEl.setAttribute("name", wd.getWidgetName());
    spEl.setAttribute("title", wd.getWidgetDescription());
    parent.appendChild(spEl);
  }
  
  // TODO: Refactor this so we just do a generic add here; all specifics are handled at the start of the wizard (convert
  // both snippets defined in the wizard and snippets indirectly required by widgets=
  
  private void addSnippetConfiguration(IProject project, Document doc,
      Element attrMap) throws ProjectCreationFailedException {
    if ( (snippets!=null && snippets.size()>0) ) {
      Element snips=doc.createElement("List");
      for (SnippetDefinition snippet: snippets.getSnippets()) {
        Element snip = createSnippetElement(doc, snippet.getRegex(), snippet.getRightRequired());

        // Add Script snippets
        if (snippet.getScriptIncludes()!=null) {
          Element scriptIncludesEl=doc.createElement("Scripts");
          for (JSInclude scriptInclude: snippet.getScriptIncludes()) {
            addSnippetFileDefinition(project, scriptInclude, scriptIncludesEl);
          }
          snip.appendChild(scriptIncludesEl);
        }
        if (snippet.getCssIncludes()!=null) {
          Element cssIncludesEl=doc.createElement("StyleSheets");
          for (CSSInclude cssInclude: snippet.getCssIncludes()) {
            addSnippetFileDefinition(project, cssInclude, cssIncludesEl);
          }
          snip.appendChild(cssIncludesEl);
        }
        snips.appendChild(snip);
      }

      addElementAttribute(attrMap, "snippets", snips);
    }
  }

  private Element createSnippetElement(Document doc, String regex,
      String rightRequired) {
    Element snip=doc.createElement("Snippet");
    snip.setAttribute("regexPattern", regex);
    snip.setAttribute("rightRequired", rightRequired);
    if (!spRights.contains(rightRequired)) spRights.add(rightRequired);
    return snip;
  }

  private void addSnippetFileDefinition(IProject project, SnippetDefinition.Include include, Element scriptIncludesEl) throws ProjectCreationFailedException {
    
    String fTemplate=include.getSourceFile();
    String targetFolder=include.getIncludeType();
    String targetFileName=include.getDestinationFile();
    Properties props=include.getReplace();
    
    copyPluginResourceToFile(fTemplate, project.getFolder("web/ui/"+targetFolder), targetFileName, props);
    Document doc=scriptIncludesEl.getOwnerDocument();
    
    // Our file structure puts all the web files (JS, CSS) in web/ui/JS|CSS in the project structure, but in deployment there is no 'web' folder
    // so we can hard-core that transformation here
    Text text=doc.createTextNode("ui/"+targetFolder+"/"+targetFileName);
    Element stringEl=doc.createElement("String");
    stringEl.appendChild(text);
    scriptIncludesEl.appendChild(stringEl);

  }
  
  private void setAttributeAndCreateFile(IProject project, Element element,
      String attrName, String folder, String filename, String template) throws ProjectCreationFailedException {
    setAttributeAndCreateFile(project, element, attrName, folder, filename, template, false);
  }
  private void setAttributeAndCreateFile(IProject project, Element element,
      String attrName, String folder, String filename, String template, boolean prependPluginRoot) throws ProjectCreationFailedException {
    System.out.println("IPFProjectCreationRunnable.setAttributeAndCreateFile");
    if (prependPluginRoot) {
      element.setAttribute(attrName, "{plugin_root}/"+folder+"/"+filename);
    } else {
      element.setAttribute(attrName, folder+"/"+filename);
    }
    IFile file=project.getFile("web/"+folder+"/"+filename);
    try {
      createFolder((IFolder)file.getParent());
    } catch (CoreException ce) {
      throw new ProjectCreationFailedException("failed to create folder structure for "+filename);
    }
    InputStream is=getClass().getResourceAsStream(template);
    try {
      file.create(is, false, null);
    } catch (CoreException ce) {
      throw new ProjectCreationFailedException("failed to create file for "+filename);
    }

  }

  private void createFolder(IFolder folder) throws CoreException {
    System.out.println("IPFProjectCreationRunnable.createDir");
    IContainer parent=folder.getParent();
    if (!parent.exists() && parent instanceof IFolder) {
      createFolder((IFolder)parent);
    }
    if (!folder.exists()) {
      folder.create(false, true, null);
    }
  }

  private void writeDoc(IFolder folder, String filename, Document doc, IProgressMonitor monitor) throws CoreException {

    try {
      IFile f=folder.getFile(filename);
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

      f.create(pIn, false, monitor);

    } catch (TransformerException e) {
      throw new CoreException(new Status(IStatus.ERROR, IPFPlugin.PLUGIN_ID, "TransformerException writing doc "+filename));
    }

  }

  private void copyResourceToFile(String resource, IContainer fldr, String target){

    InputStream is=null;
    // however, this works for everything
    is = getClass().getResourceAsStream(resource);

    if (is==null) return;
    IPath path=new Path(target);
    IFile fTarget=fldr.getFile(path);
    if(!fTarget.exists()) {
      try {
        fTarget.create(is, true, null);
      } catch (CoreException ce) {
        // TODO: Do something to alert the user to this problem
        System.out
        .println("NewIIQProjectWizard.createBaseProject: CoreException creating "+target+" : "+ce);
      }
    } else {
      // TODO: Ask to overwrite
    }

    try {
      is.close();
    } catch (IOException ioe){}
  }

  private void copyPluginResourceToFolder(String src, IContainer folder) throws ProjectCreationFailedException {
    copyPluginResourceToFile(src, folder, null, null);
  }
  
  private void copyPluginResourceToFile(String src, IContainer folder, String filename) throws ProjectCreationFailedException {
    copyPluginResourceToFile(src, folder, filename, null);
  }

  private void copyPluginResourceToFolder(String src, IContainer folder, Properties props) throws ProjectCreationFailedException {
    copyPluginResourceToFile(src, folder, null, props);
  }

  private void copyPluginResourceToFile(String src, IContainer folder, String filename, Properties props) throws ProjectCreationFailedException {
    InputStream stream=getPluginResource(src);
    
    if (props!=null) {
      stream=new SubstitutingInputStream(props,  stream);
    }
    
    IPath path=new Path(src);
    if (!folder.exists()) {
      if (!(folder instanceof IFolder)) {
        throw new ProjectCreationFailedException("Can't create non-IFolder "+folder.getName()+" ("+folder.getClass().getName()+")");
      }
      try {
        IFolder fldr=(IFolder)folder;
        createFolder(fldr);
      } catch (CoreException e) {
        throw new ProjectCreationFailedException("Unable to create folder "+folder.getName()+" : "+e);
      }
    }    
    
    IFile file=null;
    if (filename!=null) {
      file=folder.getFile(new Path(filename));
    } else {
      file=folder.getFile(path.removeFirstSegments(path.segmentCount()-1));
    }
    try {
      file.create(stream, true, null);
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Unable to copy "+src+" to "+folder.toString()+" : "+e);
    }
  }

  private void copyResourceToFolder(String src, IFolder folder) throws CoreException {
    IPath path=new Path(src);
    copy(path, folder.getFile(path.lastSegment()).getFullPath(), null);
  }

  private void addNature(IProject project) throws CoreException {
    if (!project.hasNature(JavaCore.NATURE_ID)) {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      String[] newNatures = new String[prevNatures.length+2];
      System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
      newNatures[prevNatures.length] = JavaCore.NATURE_ID;
      newNatures[prevNatures.length+1] = IPFNature.NATURE_ID;
      description.setNatureIds(newNatures);     

      IProgressMonitor monitor = null;
      project.setDescription(description, monitor);
    }
  }

  /**
   * Just do the basics: create a basic project.
   * 
   * @param location
   * @param projectName
   */
  private IProject createBaseProject(String projectName, URI location) {
    // it is acceptable to use the ResourcesPlugin class

    theProject = ResourcesPlugin.getWorkspace().getRoot()
        .getProject(projectName);

    if (!theProject.exists()) {

      try {
        theProject.create(null);
        
        if (!theProject.isOpen()) {
          theProject.open(null);
        }
      } catch (CoreException e) {
        IPFPlugin.logException("createBaseProject: CoreException", e);
      }
    } else {
      // TODO: Ask to overwrite
    }

    return theProject;
  }

  private IProject createBaseProject(String projectName, URI location,
      IProgressMonitor monitor) throws InterruptedException, ProjectCreationFailedException {

    SubMonitor subMonitor=SubMonitor.convert(monitor, 34);
    
    // 1 tick
    IProject project = createBaseProject(projectName, location);
    subMonitor.worked(1);

    IFolder src=null;
    try {
      subMonitor.subTask("Creating base project");
      // 1 tick
      addNature(project);     
      subMonitor.worked(2);


      // We need to copy xmltask.jar to the project, for the build process
      // 2 ticks
      subMonitor.subTask("copying xmltask.jar");
      IFolder libFolder = getFolder(project, "lib");
      copyResourceToFile("/lib/xmltask.jar", libFolder, "xmltask.jar");
      subMonitor.worked(2);
      

      // Now set up the build process
      // 2 ticks
      subMonitor.subTask("copying build.xml");
      libFolder = getFolder(project, "includes");
      copyPluginResourceToFolder("build.xml", project);
      subMonitor.worked(2);

      // now copy the properties file over
      // 1 tick
      subMonitor.subTask("creating build.properties file");
      IFile propFile=project.getFile("build.properties");
      StringBuilder sb=new StringBuilder();
      sb.append("iiq.home=");
      sb.append(pdLibs.getIIQLocation().replace("\\", "\\\\"));
      sb.append("\npluginname=");
      sb.append(pageOne.getUniqueName());
      sb.append("\nversion=");
      sb.append(pageOne.getVersion());
      if (pdFullPage.isUseAngular()) {
        sb.append("\nuseAngular=true");
        subMonitor.subTask("Copying app.js");
        writeAngularFiles(project);
        
      }
      sb.append("\n");
      subMonitor.worked(1);
      
      // 5 ticks
      subMonitor.subTask("writing build.properties");
      propFile.create(new ByteArrayInputStream(sb.toString().getBytes()), true, null);
      subMonitor.worked(5);
      
      src=getFolder(project, "src");
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("CoreException: "+e);
    }

    // 2 ticks
    subMonitor.subTask("Adding Java nature");
    IJavaProject javaProject = JavaCore.create(project);
    List<IClasspathEntry> listCP=new ArrayList<IClasspathEntry>();

    // Add the src folder
    listCP.add(JavaCore.newSourceEntry(src.getFullPath()));
    // Add the JRE
    listCP.add(JavaCore.newContainerEntry(
        new Path("org.eclipse.jdt.launching.JRE_CONTAINER"),
        false)); // not exported 
    try {

      for (ListEntry ref: pdLibs.getReferences()) {
        listCP.add(createCPEntry(ref.getValue(), javaProject, "lib"));
      }
      for (ListEntry include: pdLibs.getIncludes()) {
        listCP.add(createCPEntry(include.getValue(), javaProject, "includes"));
      }
      // Add all the identityiq libs
      String loc=pdLibs.getIIQLocation();
      File f=new File(loc);
      IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(f);
      IFileStore webInfLib=fileStore.getChild("WEB-INF/lib");
      for (IFileStore jarFile: webInfLib.childStores(EFS.NONE, null)) {
        if (jarFile.getName().endsWith(".jar")) {
          listCP.add(JavaCore.newLibraryEntry(new Path(loc+File.separator+"WEB-INF/lib"+File.separator+jarFile.getName()), null, null));
        }
      }
    } catch (CoreException ce) {
      ce.printStackTrace();
      throw new ProjectCreationFailedException("CoreException adding Classpath Entry: "+ce);
    }

    IClasspathEntry[] newClasspath = (IClasspathEntry[]) listCP.toArray(new IClasspathEntry[listCP.size()]);

    try {
      javaProject.setRawClasspath(newClasspath, null);
    } catch (JavaModelException e1) {
      throw new ProjectCreationFailedException("JavaModelException: "+e1);
    }
    monitor.worked(2); // 2

    // Copy needed jars (pluginframework, jaxrs, logging) to lib (for compile but not bundled)


    return project;
  }

  private IClasspathEntry createCPEntry(String src, IJavaProject project, String dest) throws ProjectCreationFailedException {
    try {
      copyResourceToFolder(src, getFolder(project.getProject(), dest));
    } catch (CoreException e) {
      throw new ProjectCreationFailedException("Can't copy resource "+src+" to "+dest+" : "+e);
    }
    return classpathLibEntry(project, dest+File.separator+new Path(src).lastSegment());
  }

  private IClasspathEntry createCPEntryFromResource(String src, IJavaProject project, String dest) throws CoreException {
    copyResourceToFile(src, project.getProject().getFolder(dest), new Path(src).lastSegment());
    return classpathLibEntry(project, dest+File.separator+new Path(src).lastSegment());
  }

  private IClasspathEntry classpathLibEntry(IJavaProject javaProject, String jarFile) {
    IClasspathEntry entry = JavaCore.newLibraryEntry(javaProject.getProject().getFile(jarFile).getFullPath(), null, null);
    return entry;
  }

  private void writeAngularFiles(IProject project) throws ProjectCreationFailedException {

    String page = "web/ui/page.xhtml";
    
    Properties props=new Properties();
    props.put("%%appname%%",  pageOne.getUniqueName());
    copyPluginResourceToFolder("app.js", project.getFolder("web/ui/js"), props);
    
  }
  
  private void copy(IPath from, IPath to, IProgressMonitor subMon) throws CoreException{
    IFile file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(from);
    if(file!=null) {
      file.copy(to, true, subMon);
      return;
    } else {
      IFileStore fileStore = EFS.getLocalFileSystem().getStore(from);
      IFileStore rootStore = EFS.getLocalFileSystem().getStore(ResourcesPlugin.getWorkspace().getRoot().getRawLocation());
      if(fileStore!=null) {
        fileStore.copy(rootStore.getFileStore(to), EFS.NONE, subMon);
      }        
    }
  }

  private IProject createProjectFromProject(String projectName, URI location,
      IProgressMonitor monitor) throws InterruptedException, ProjectCreationFailedException {

    IProject project=createBaseProject(projectName, location, monitor);

    // get identityiq.jar
    //      SubProgressMonitor subMon=new SubProgressMonitor(monitor, oPaths.size()); // 7
    //      List<IPath> paths=new ArrayList<IPath>();
    //      for (IPath path: oPaths) {
    //      	IFile iiqFile=libFolder.getFile(path.lastSegment());
    //      	copy(path, iiqFile.getFullPath(), subMon);
    //      	paths.add(iiqFile.getFullPath());
    //      }

    return project;
  }

  public IProject getProject() {
    return theProject;
  }

  private Element getFirstElement(Node node) {
    node = node.getFirstChild();
    while (node != null && node.getNodeType() != Node.ELEMENT_NODE)
    {
      node = node.getNextSibling();
    }
    return (Element)node;
  }

  private Element getNamedChild(Node node, String name) {
    node = node.getFirstChild();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName()) ) {
        return (Element)node;
      }
      node = node.getNextSibling();
    }
    return null;
  }

  IPackageFragment createPackage(IProject project, String sPackage) throws ProjectCreationFailedException {
    // make sure the package exists
    IJavaProject jProj = JavaCore.create(project);
    IFolder folder = getFolder(project, "src");
    IPackageFragmentRoot srcFolder = jProj
        .getPackageFragmentRoot(folder);
    IPackageFragment fragment;
    try {
      fragment = srcFolder.createPackageFragment(sPackage, true, null);
    } catch (JavaModelException e) {
      throw new ProjectCreationFailedException("Can't create package fragment: "+e);
    }

    return fragment;

  }
  
  private String getFullRESTPackageName(String shortPackage) {
    return "sailpoint.plugin."+shortPackage+".rest";
  }

  private CompilationUnit createPluginRESTClass(IProject project, String sPackage, RESTResource rr) throws CoreException, ProjectCreationFailedException {

    return createRESTClass(project, getFullRESTPackageName(sPackage), rr.getBaseClazzName(), rr.getBaseEndpointRight(), rr.getBaseEndpoint(), rr.getEndpoints(), rr.getSampleMessage());
  
  }
    
  private CompilationUnit createRESTClass(IProject project, String sPackage, String baseClazzName, String baseEndpointRight, String baseEndpoint, List<RESTEndpoint> endpoints, String sampleMessage ) throws ProjectCreationFailedException {

    // Generate a new Compilation Unit
    AST ast=AST.newAST(AST.JLS8);
    CompilationUnit unit=ast.newCompilationUnit();

    // Generate the package declaration
    PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
    packageDeclaration.setName(ast.newName(sPackage.split("\\.")));
    unit.setPackage(packageDeclaration);

    addImports(ast, unit);

    // create class
    TypeDeclaration type=ast.newTypeDeclaration();
    SimpleName sn=ast.newSimpleName(baseClazzName);
    type.setName(sn);
    Type extendType=ast.newSimpleType(ast.newName(new String[] {"BasePluginResource"}));
    type.setSuperclassType(extendType);

    unit.types().add(type);

    List mods = type.modifiers();


    // Add the SPRight Annotation
    NormalAnnotation anno=ast.newNormalAnnotation();
    anno.setTypeName(ast.newSimpleName("RequiredRight"));
    MemberValuePair mvp=ast.newMemberValuePair();
    mvp.setName(ast.newSimpleName("value"));    
    StringLiteral spr=ast.newStringLiteral();
    spr.setLiteralValue(baseEndpointRight);
    mvp.setValue(spr);
    anno.values().add(mvp);
    mods.add(anno);

    // Add the Path annotation
    mods.add(simpleAnnotation(ast, "Path", baseEndpoint));

    // Add the public modifier
    mods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

    addLogVariable(ast, type, baseClazzName);

    for (RESTEndpoint ep: endpoints) {
      MethodDeclaration method=ast.newMethodDeclaration();
      // Create the annotations and other mods (public)
      List methodMods=method.modifiers();
      methodMods.add( simpleAnnotation(ast, ep.getMethod(), null) );
      methodMods.add( simpleAnnotation(ast, "Path", ep.getEndpointName()) );
      methodMods.add( simpleAnnotation(ast, "Produces", new Name[] {ast.newSimpleName("MediaType"), ast.newSimpleName("APPLICATION_JSON")}) );
      if (ep.hasSpecificRight()) {
        NormalAnnotation sr_anno=ast.newNormalAnnotation();
        sr_anno.setTypeName(ast.newSimpleName("RequiredRight"));
        MemberValuePair sr_mvp=ast.newMemberValuePair();
        sr_mvp.setName(ast.newSimpleName("value"));    
        StringLiteral sr_spr=ast.newStringLiteral();
        sr_spr.setLiteralValue(ep.getSpRight());
        sr_mvp.setValue(sr_spr);
        sr_anno.values().add(sr_mvp);
        methodMods.add(sr_anno);
      }
      methodMods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

      // Add the return type
      method.setReturnType2(ast.newSimpleType(ast.newName(ep.getReturnType().split("\\."))));

      // and the method name
      method.setName(ast.newSimpleName(ep.getJavaName()));

      // add a debug statement
      MethodInvocation logDebug=ast.newMethodInvocation();
      logDebug.setExpression(ast.newSimpleName("log"));
      logDebug.setName(ast.newSimpleName("debug"));
      StringLiteral msg = ast.newStringLiteral();
      msg.setLiteralValue(ep.getMethod()+" "+ep.getEndpointName());
      logDebug.arguments().add(msg);
      Block blk=ast.newBlock();
      blk.statements().add(ast.newExpressionStatement(logDebug));

      // <type> ret=null;
      VariableDeclarationFragment frag=ast.newVariableDeclarationFragment();

      SimpleName newsn=ast.newSimpleName("ret");
      frag.setName(newsn);
//      Expression initializer;
      if (sampleMessage==null) {
        frag.setInitializer(ast.newNullLiteral());
      } else {
        StringLiteral smplMsg=ast.newStringLiteral();
        smplMsg.setLiteralValue(sampleMessage);
        frag.setInitializer(smplMsg);
      }

      VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(frag);
      stmt.setType(ast.newSimpleType(ast.newName(ep.getReturnType().split("\\."))));

      blk.statements().add(stmt);

      ReturnStatement retStmt=ast.newReturnStatement();
      retStmt.setExpression(ast.newSimpleName("ret"));
      blk.statements().add(retStmt);

      method.setBody(blk);
      type.bodyDeclarations().add(method);


    }

    // Now add the getPluginName method
    MethodDeclaration method=ast.newMethodDeclaration();
    // Create the annotations and other mods (public)
    List methodMods=method.modifiers();
    methodMods.add( simpleAnnotation(ast, "Override", null) );
    methodMods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    
    // Add the return type
    method.setReturnType2(ast.newSimpleType(ast.newSimpleName("String")));
    
    // and the method name
    method.setName(ast.newSimpleName("getPluginName"));
    
    Block blk=ast.newBlock();
    
    ReturnStatement retStmt=ast.newReturnStatement();
    StringLiteral st=ast.newStringLiteral();
    st.setLiteralValue(pageOne.getUniqueName());
    retStmt.setExpression(st);
    blk.statements().add(retStmt);
    
    method.setBody(blk);
    type.bodyDeclarations().add(method);

    return unit;

  }

  private void addLogVariable(AST ast, TypeDeclaration type, String baseClazzName) {
    List mods;
    // Add a log variable
    // public static final Log log = LogFactory.getLog(<Classname>.class);
    VariableDeclarationFragment logFrag=ast.newVariableDeclarationFragment();
    logFrag.setName(ast.newSimpleName("log"));

    MethodInvocation getLog=ast.newMethodInvocation();
    getLog.setExpression(ast.newSimpleName("LogFactory"));
    getLog.setName(ast.newSimpleName("getLog"));
    TypeLiteral clazz=ast.newTypeLiteral();
    clazz.setType(ast.newSimpleType(ast.newSimpleName(baseClazzName)));
    getLog.arguments().add(clazz);
    logFrag.setInitializer(getLog);

    FieldDeclaration log = ast.newFieldDeclaration(logFrag);
    mods=log.modifiers();
    mods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    mods.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
    mods.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
    log.setType(ast.newSimpleType(ast.newSimpleName("Log")));

    type.bodyDeclarations().add(log);
  }

  private Annotation simpleAnnotation(AST ast, String annoName, Object value) throws ProjectCreationFailedException {
    Annotation anno=null;
    if (value==null) {
      anno=ast.newMarkerAnnotation();
    } else {
      Expression literal=null;
      if (value instanceof String) {
        StringLiteral stringLit = ast.newStringLiteral();
        stringLit.setLiteralValue((String)value);
        literal=stringLit;
      } else if (value instanceof Name[]) {
        Name[] nameLit=(Name[])value;
        if(nameLit.length!=2) {
          throw new ProjectCreationFailedException("Need exactly two Names for Field Access");
        }
        if(!(nameLit[1] instanceof SimpleName)) {
          throw new ProjectCreationFailedException("second Name for Field Access must be SimpleName. Found "+nameLit[1].getClass().getSimpleName());
        }
        FieldAccess fieldLit=ast.newFieldAccess();
        fieldLit.setExpression(nameLit[0]);
        fieldLit.setName((SimpleName)nameLit[1]);
        literal=fieldLit;
      } else {
        throw new ProjectCreationFailedException("Unknown type "+value.getClass().getSimpleName()+" creating simple annotation");
      }
      anno=ast.newSingleMemberAnnotation();
      ((SingleMemberAnnotation)anno).setValue(literal);
    }
    anno.setTypeName(ast.newName(annoName));
    return anno;
  }

  private void addImport(AST ast, CompilationUnit unit, String partA, String partB, boolean onDemand) {
    ImportDeclaration importDeclaration = ast.newImportDeclaration();
    QualifiedName name = 
        ast.newQualifiedName(
            ast.newName(partA.split("\\.")),
            ast.newSimpleName(partB));
    importDeclaration.setName(name);
    importDeclaration.setOnDemand(onDemand);
    unit.imports().add(importDeclaration);
  }

  private void addImports(AST ast, CompilationUnit unit) {

    addImport(ast, unit, "java", "util", true);
    addImport(ast, unit, "javax.ws", "rs", true);
    addImport(ast, unit, "javax.ws.rs.core", "MediaType", false);
    addImport(ast, unit, "org.apache.commons.logging", "Log", false);
    addImport(ast, unit, "org.apache.commons.logging", "LogFactory", false);
    addImport(ast, unit, "sailpoint.rest.plugin", "BasePluginResource", false);
    addImport(ast, unit, "sailpoint.rest.plugin", "RequiredRight", false);

  }

  private IFolder getFolder(IFolder fldr) throws ProjectCreationFailedException {

    if (!fldr.exists()) {
      IContainer parent=fldr.getParent();
      if(!parent.exists()) {
        parent=getFolder((IFolder)parent);
      }
      try {
        fldr.create(0, true, null);
      } catch (CoreException ce) {
        //throw new ProjectCreationFailedException("couldn't create '"+fldr.getName()+"' folder");
      }
    }
    return fldr;
  }

  private IFolder getFolder(IContainer parent, String folder) throws ProjectCreationFailedException {

    IFolder fldr=parent.getFolder(new Path(folder));
    return getFolder(fldr);

  }

  class StreamGobbler extends Thread
  {
    InputStream is;
    String type;
    IProgressMonitor monitor;
    
    StreamGobbler(InputStream is, IProgressMonitor monitor, String type)
    {
      this.is = is;
      this.type = type;
      this.monitor=monitor;
    }

    public void run()
    {
      try
      {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line=null;
        while ( (line = br.readLine()) != null) {
          System.out.println(type + ">" + line);
          if (monitor!=null) {
            monitor.subTask(line);
          }
        }
      } catch (IOException ioe)
      {
        ioe.printStackTrace();  
      }
    }
    public void close() {
      try {
        is.close();
      } catch (Exception e) {
      }
    }
  }

  private void recurseCopy(File f, IFolder f2) throws CoreException {
    
    File[] children=f.listFiles();
    if (children==null) {
      System.out.println("recurseCopy: no children");
      return;
    }
    for (File child: children) {
      if (child.isDirectory()) {
        System.out.println("recurseCopy: child="+child.getName());
        IFolder newFldr=f2.getFolder(child.getName());
        newFldr.create(true,  true,  null);
        recurseCopy(child, newFldr);
      } else {
        IFile newFile=f2.getFile(child.getName());
        try {
          System.out.println("recurseCopyt: copy "+child.getName());
          newFile.create(new FileInputStream(child), true, null);
        } catch (FileNotFoundException e) {
          throw new CoreException(null); // this will never happen because we just asked the filesystem about the files
        }        
      }
    }
    
  }
  
}

