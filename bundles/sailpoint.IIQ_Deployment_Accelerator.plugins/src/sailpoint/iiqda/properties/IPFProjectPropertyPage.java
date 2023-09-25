package sailpoint.iiqda.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.framework.Version;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import sailpoint.iiqda.IPFPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.exceptions.ProjectCreationFailedException;

public class IPFProjectPropertyPage extends PropertyPage {

  private static final boolean DEBUG_PROJECT = "true".equalsIgnoreCase(Platform
      .getDebugOption(IPFPlugin.PLUGIN_ID+"/debug/ProjectPropertyPage"));
  
  private org.eclipse.swt.widgets.List list;

  private List<RemovalItem> removals;
  private Map<String,String> additions;

  private Version minFrameworkVersion;

  
  /**
   * Create the property page.
   */
  public IPFProjectPropertyPage() {
  }

  /**
   * Create contents of the property page.
   * @param parent
   */
  @Override
  public Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    container.setLayout(new GridLayout(1, false));
    
    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    TabItem tbtmInstallxmlFiles = new TabItem(tabFolder, SWT.NONE);
    tbtmInstallxmlFiles.setText("Install (XML) Files");
    
    Composite composite = new Composite(tabFolder, SWT.NONE);
    tbtmInstallxmlFiles.setControl(composite);
    composite.setLayout(new GridLayout(2, false));
    
    ListViewer listViewer = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
    list = listViewer.getList();
    list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Composite cmp_buttons = new Composite(composite, SWT.NONE);
    RowLayout rl_cmp_buttons = new RowLayout(SWT.VERTICAL);
    rl_cmp_buttons.fill = true;
    cmp_buttons.setLayout(rl_cmp_buttons);
    
    Button btnUp = new Button(cmp_buttons, SWT.NONE);
    btnUp.setText("Up");
    btnUp.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        // take all the selected items
        // move them to one above the first
        int[] selections=list.getSelectionIndices();
        int minSel=Integer.MAX_VALUE;
        for(int sel: selections) {
          if (sel<minSel) minSel=sel;
        }
        String[] sSelections=list.getSelection();
        
        list.remove(list.getSelectionIndices());
        if (minSel>0) minSel--; //put at the one *before* the first selection
        for (int i=sSelections.length-1; i>=0; i--) {
          list.add(sSelections[i], minSel);
        }
        
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });
    
    Button btnDown = new Button(cmp_buttons, SWT.NONE);
    btnDown.setText("Down");
    btnDown.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        // take all the selected items
        // move them to one below the last
        int[] selections=list.getSelectionIndices();
        int maxSel=0;
        for(int sel: selections) {
          if (sel>maxSel) maxSel=sel;
        }
        
        String[] sSelections=list.getSelection();
        
        list.remove(list.getSelectionIndices());        
        maxSel-=sSelections.length;
        
        for (String val: sSelections) {
          list.add(val, maxSel);
        }
        
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });

    Button btnAdd = new Button(cmp_buttons, SWT.NONE);
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        // select a file from the filesystem
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String [] {"*.xml"});
        String result = dialog.open();
        if (result!=null) {
          File f=new File(result);
          
          list.add(f.getName());
          additions.put(f.getName(), result);
        }
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });
    
    Button btnRemove = new Button(cmp_buttons, SWT.NONE);
    btnRemove.setText("Remove");
    btnRemove.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        // check if we want to remove the file from the project as well
        // then record the removals and remove them from the list
        MessageDialog dialog = new MessageDialog(
            null, "Remove Files?", null, "Remove files as well (WARNING: This cannot be undone!)",
            MessageDialog.QUESTION,
            new String[] {"Yes", "No", "Cancel"},
            0); // yes is the default
         int result = dialog.open();
         if (result !=2) {
           boolean removeFile=(result==0);
           for (String sel: list.getSelection()) {
             RemovalItem itm=new RemovalItem(sel, removeFile);
             removals.add(itm);
           }
         }
         list.remove(list.getSelectionIndices());
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });
    
    populateWidgets();
    
    return container;
  }
  
  private void populateWidgets() {
    
    IProject resource = getProject();
    
    // Populate the list page. For this we'll need to read <project>/web/installation/install.setup.xml
    
    IContainer prjContainer=(IContainer)resource;
    readManifest(resource);
    
    IFile installFile=prjContainer.getFile( new Path("web/installation/install.setup.xml") );
    if(!installFile.exists()) {
//      if(DEBUG_PROJECT) {
        IPFPlugin.logDebug("No install.setup.xml found");
//      }
    }
    
    String[] files=new String[0]; //readInstallSetupXML(installFile);
    list.setItems(files);
    
    // Initialize some variables
    removals=new ArrayList<RemovalItem>();
    additions=new HashMap<String,String>();
  }

  private IProject getProject() {
    Object el=getElement();
    IProject resource=null;
      
    if(el instanceof IJavaProject) {
      IJavaProject jp=(IJavaProject)getElement();
      resource = (IProject) jp.getProject();
    } else {
      resource = (IProject) el;
    }
    return resource;
  }

  private class RemovalItem {
    
    private String item;
    private boolean remove;
    
    public RemovalItem(String item, boolean remove) {
      this.item=item;
      this.remove=remove;
    }
    
    public String getItem() {
      return item;
    }
    
    public boolean isRemove() {
      return remove;
    }
  }
  
  private String[] readInstallSetupXML(IFile installFile) {
    
    List<String> files=new ArrayList<String>();
    
    // NOTE: We are assuming that the file is like:
    // ...
    // <ImportAction name='include' value='{plugin_root}/Configuration-OrgStructurePluginConfiguration.xml'/>
    // ...
    // Anything else will be ignored, since we are essentially regenerating this file.
    
    
    XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
    XMLStreamReader2 stream = null;
    fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    // Doesn't work: fac.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

    try {
      stream = (XMLStreamReader2)fac.createXMLStreamReader( new InputStreamReader(installFile.getContents()) );

      while( stream.hasNext()  ) {
        int evtCode=stream.next();
        if(evtCode==XMLStreamConstants.START_ELEMENT) {
          String tagName = stream.getLocalName();
          if( "ImportAction".equals(tagName) ) {
            String name=stream.getAttributeValue(null, "name");
            if ("include".equals(name)) {
              String value=stream.getAttributeValue(null, "value");
              if(value!=null) {
                files.add(value);
              }
            }
          }
        }
      }
      stream.close();
    } catch (XMLStreamException xmle) {
      IPFPlugin.logException("Parsing install.setup.xml: XMLException", xmle);
    } catch (CoreException ce) {
      IPFPlugin.logException("Parsing install.setup.xml: CoreException", ce);
    }
    
    List<String> newFiles=new ArrayList<String>();
    for (String file: files) {
      // Now again, we're going to make an assumption. the file will either start with {plugin_root} or {plugin_root}/installation/ (>=0.4.9)
      // Anything else will be stripped out, and lost on a save.
      if ( file.startsWith("{plugin_root}/installation/") ) {
        newFiles.add( file.substring("{plugin_root}/installation/".length()) );
      } else if ( file.startsWith("{plugin_root}") ) {
        newFiles.add( file.substring("{plugin_root}".length()) );
      }
    }
    
    return newFiles.toArray(new String[newFiles.size()]);
  }

  private void readManifest(IProject project) {
    
    IFile installFile=project.getFile( new Path("web/manifest.xml") );

    XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
    XMLStreamReader2 stream = null;
    fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    // Doesn't work: fac.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    
    try {
      stream = (XMLStreamReader2)fac.createXMLStreamReader( new InputStreamReader(installFile.getContents()) );
      
      while( stream.hasNext()  ) {
        int evtCode=stream.next();
        if(evtCode==XMLStreamConstants.START_ELEMENT) {
          String tagName = stream.getLocalName();
          if( "Plugin".equals(tagName) ) {
            minFrameworkVersion=new Version( stream.getAttributeValue(null, "minSystemVersion") );
          }
        }
      }
      stream.close();
    } catch (XMLStreamException xmle) {
      IPFPlugin.logException("Parsing manifest.xml: XMLException", xmle);
    } catch (CoreException ce) {
      IPFPlugin.logException("Parsing manifest.xml: CoreException", ce);
    }
  }
  
  @Override
  protected void performApply() {
    // TODO Auto-generated method stub
    System.out.println("IPFProjectPropertyPage.performApply:");
    performOk();
  }

  @Override
  public boolean performOk() {

    IProject prj=getProject();
    // now rewrite the install.setup.xml file
    try {
      //
      // remove any files that were removed from the list if
      // they were flagged to remove
      for (RemovalItem item: removals) {
        if (item.isRemove()) {
          IFile file=prj.getFile("web/installation/"+item.getItem());
          try {
            file.delete(true, null);
          } catch (CoreException e) {
            IPFPlugin.logError("unable to delete "+item.getItem());
          }
        }
      }
      
      // copy in any added files
      for (String addition: additions.keySet()) {
        File f=new File(additions.get(addition));
        IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(f);
        try {
          InputStream is=fileStore.openInputStream(EFS.NONE, null);
          IFile outFile=prj.getFile("web/installation/"+addition);
          outFile.create(is, true, null);
        } catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
      IFile installSetupXml = prj.getFile("web/installation/install.setup.xml");
      Document contentDoc=createInstallSetupXml(prj);
      InputStream docContents=CoreUtils.docAsStream(contentDoc);
      
      StringWriter writer = new StringWriter();
      IOUtils.copy(docContents, writer, "UTF8");
      String theString = writer.toString();
      System.out.println(theString);
      
      installSetupXml.setContents(new ByteArrayInputStream(theString.getBytes()), true, true, null);
      
      try {
        docContents.close();
      } catch (IOException ioe) {
        System.out.println("ioe "+ioe);
      }
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
    
    return true;
  }
  
  private Document createInstallSetupXml(IProject project) throws Exception {

    // create doc
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
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

      for (String sp: list.getItems()) {
        addImport(sailpointEl, sp);
      }

      return doc;
      
    } catch (ParserConfigurationException e) {
      throw new ProjectCreationFailedException("unable to build installation doc: "+e);
    }


  }
  
  private void addImport(Element parent, String filename) {
    Document doc=parent.getOwnerDocument();

    Element impAct=doc.createElement("ImportAction");
    impAct.setAttribute("name", "include");
    
    // build the right filename structure
    Version zeroFourNine=new Version("0.4.9");
    StringBuilder sb=new StringBuilder("{plugin_root}");
    if(minFrameworkVersion.compareTo(zeroFourNine)>=0) {
      sb.append("/installation/");
    }
    sb.append(filename);
    impAct.setAttribute("value", sb.toString());
    
    parent.appendChild(impAct);
  }
}
