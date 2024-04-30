package sailpoint.iiqda.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;

@SuppressWarnings("restriction")
public class IIQProjectPropertyPage extends PropertyPage implements Listener {

  private static final boolean DEBUG_PROJECT = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/ProjectPropertyPage"));

  private List dirList;
  private Button addBtn;
  private Button removeBtn;

  private Map<String,Combo> combos;

  private Label sliderLbl;

  private Composite cmpSlider;
  private Composite cmpTimeoutSlider;

  private Slider slider;

  private Text timeoutBox;
  private Label lblOpenFilesOn;
  private Label lblSubstitutionOnCompare;
  private Composite cmpImport;
  private Button btnOpenOnImport;
  private Button btnSubstitutionOnCompare;
  private boolean openOnImport;
  private boolean substitutionOnCompare;
  // KCS 2023-12-28
  private Label lblCustomFilenamesOn;
  private Button btnCustomFilenames;
  private boolean customFilenames;
  // KCS 2023-12-28

  /**
   * Constructor for SamplePropertyPage.
   */
  public IIQProjectPropertyPage() {
    super();
  }

  /**
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    Composite theComposite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    theComposite.setLayout(layout);
    GridData data = new GridData(GridData.FILL);
    data.grabExcessHorizontalSpace = true;
    theComposite.setLayoutData(data);

    TabFolder tFldr=new TabFolder(theComposite, SWT.NONE);
    TabItem ti1=new TabItem(tFldr, SWT.BORDER);
    ti1.setText("General");
    Composite composite=new Composite(tFldr, SWT.NONE);
    layout = new GridLayout();
    composite.setLayout(layout);
    data = new GridData(GridData.FILL);
    data.grabExcessHorizontalSpace = true;
    composite.setLayoutData(data);

    // Script lines slider
    cmpSlider = new Composite(composite, SWT.NONE);
    layout=new GridLayout();
    layout.numColumns=3;
    cmpSlider.setLayout(layout);
    Label lbl=new Label(cmpSlider, SWT.NONE);
    lbl.setText("Maximum number of script lines before <![CDATA[ ]]> should be used");
    slider = new Slider(cmpSlider, SWT.NONE);
    slider.setIncrement(1);
    slider.setMinimum(0);
    slider.setMaximum(100);
    sliderLbl = new Label(cmpSlider, SWT.NONE);
    sliderLbl.setText("0");
    data=new GridData();
    data.widthHint=50;
    sliderLbl.setLayoutData(data);
    slider.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent arg0) {
        if (DEBUG_PROJECT) {
          IIQPlugin.logDebug("CDATA Lines: "+slider.getSelection());
        }
        sliderLbl.setText(Integer.toString(slider.getSelection()));
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    cmpTimeoutSlider = new Composite(composite, SWT.NONE);
    layout=new GridLayout();
    layout.numColumns=2;
    cmpTimeoutSlider.setLayout(layout);
    lbl=new Label(cmpTimeoutSlider, SWT.NONE);
    lbl.setText("Connection timeout (ms)");
    timeoutBox = new Text(cmpTimeoutSlider, SWT.HORIZONTAL|SWT.BORDER|SWT.FILL);		
    timeoutBox.setText("60000");
    timeoutBox.addListener(SWT.Modify, this);
    data=new GridData();
    data.widthHint=100;
    timeoutBox.setLayoutData(data);

    cmpImport = new Composite(composite, SWT.NONE);
    cmpImport.setLayout(new GridLayout(2, false));
    cmpImport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    lblOpenFilesOn = new Label(cmpImport, SWT.NONE);
    lblOpenFilesOn.setText("Open Files on Import");

    btnOpenOnImport = new Button(cmpImport, SWT.CHECK);
    btnOpenOnImport.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    // Added KCS 2023-12-28 to get filename format
    lblCustomFilenamesOn = new Label(cmpImport, SWT.NONE);
    lblCustomFilenamesOn.setText("Custom filename format");
    btnCustomFilenames = new Button(cmpImport, SWT.CHECK);
    btnCustomFilenames.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
    // KCS 2023-12-28
    
    lblSubstitutionOnCompare = new Label(cmpImport, SWT.NONE);
    lblSubstitutionOnCompare.setText("Perform macro substitution on compare");
    
    btnSubstitutionOnCompare = new Button(cmpImport, SWT.CHECK);
    btnSubstitutionOnCompare.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
    
    lbl=new Label(composite, SWT.NONE);
    lbl.setText("Excluded directories");

    addBtn=new Button(composite, SWT.PUSH);
    addBtn.setText("Add");

    addBtn.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IAdaptable el=getElement();
        IContainer container=null;
        JavaProject asJavaProject = (JavaProject) el.getAdapter(JavaProject.class);
        if(asJavaProject!=null) {
          container=(IContainer)asJavaProject.getProject();
        } else {
          container=(IContainer)el;
        }
        ContainerSelectionDialog dlg=new ContainerSelectionDialog(getShell(), container, false, "Select folder to Exclude");
        int open = dlg.open();
        if (open==ContainerSelectionDialog.OK) {
          Object[] results=dlg.getResult();
          for(Object result: results) {
            IPath path=(IPath)result;
            IPath relativePath=path.makeRelativeTo((IPath)container.getFullPath());
            dirList.add(relativePath.toString());
          }

        };

      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });

    dirList=new List(composite, SWT.BORDER);

    data = new GridData(GridData.FILL);
    data.grabExcessVerticalSpace = true;
    data.grabExcessHorizontalSpace = true;
    data.widthHint=400;
    data.heightHint=300;
    dirList.setLayoutData(data);


    removeBtn=new Button(composite, SWT.NONE|SWT.V_SCROLL);
    removeBtn.setText("Remove Selected");
    removeBtn.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=dirList.getSelection();
        for(String selection: selected) {
          dirList.remove(selection);
        }

      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });
    ti1.setControl(composite);

    TabItem ti2=new TabItem(tFldr, SWT.BORDER);
    ti2.setText("Errors and Warnnings");
    composite=new Composite(tFldr, SWT.NONE);

    GridLayout gl=new GridLayout();
    gl.numColumns=2;
    composite.setLayout(gl);

    combos=new HashMap<String, Combo>();

    addCombo(composite, "On Demand Imports (e.g sailpoint.object.*)", IIQPreferenceConstants.P_ON_DEMAND_IMPORTS);
    addCombo(composite, "No Rule Type Specified", IIQPreferenceConstants.P_NO_RULE_TYPE);
    addCombo(composite, "Unknown Rule Type Specified", IIQPreferenceConstants.P_UNKNOWN_RULE_TYPE);
    addCombo(composite, "Unhandled Exceptions", IIQPreferenceConstants.P_UNHANDLED_EXCEPTIONS);

    ti2.setControl(composite);

    populateWidgets();

    return theComposite;
  }

  private void addCombo(Composite composite, String label, String pref) {
    Label lbl;
    lbl=new Label(composite, SWT.NONE);
    lbl.setText(label);
    Combo cmb=errWarnNoneCombo(composite);    
    combos.put(pref, cmb);
  }

  private Combo errWarnNoneCombo(Composite composite) {

    Combo cmb=new Combo(composite, SWT.NONE);
    cmb.setItems(new String[]{"Error", "Warning", "None"});
    cmb.setData("Error", "error");
    cmb.setData("Warning", "warn");
    cmb.setData("None", "none");
    return cmb;
  }

  private void populateWidgets() {

    try {
      Object el=getElement();
      IResource resource=null;

      if(el instanceof JavaProject) {
        JavaProject jp=(JavaProject)getElement();
        resource = (IResource) jp.getProject();
      } else {
        resource = (IResource) el;
      }
      String sProps=resource.getPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_EXCLUDED_DIRECTORIES));
      if(sProps!=null) {
        String[] dirs=sProps.split(";");
        for(String dir: dirs) {
          dirList.add(dir);
        }
      }

      for(String pref: combos.keySet()) {
        Combo cmb=combos.get(pref);
        if(cmb!=null) {
          String cmbVal=resource.getPersistentProperty(
              new QualifiedName("", pref));
          if(cmbVal==null) cmbVal="error";
          if (DEBUG_PROJECT) {
            IIQPlugin.logDebug("to Combo: "+pref+"="+cmbVal);
          }
          switch(cmbVal) {
            case "warn":
              cmb.setText("Warning");
              break;
            case "none":
              cmb.setText("None");
              break;
            default:
              cmb.setText("Error");
          }
        }
      }

      int numLines=3;
      try {
        numLines=Integer.parseInt(resource.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_NUM_LINES_BEFORE_CDATA)));
      } catch (NumberFormatException nfe) {}
      sliderLbl.setText(Integer.toString(numLines));
      slider.setSelection(numLines);

      int timeout=60000;
      try {
        timeout=Integer.parseInt(resource.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_CONNECTION_TIMEOUT)));
      } catch (NumberFormatException nfe) {}
      timeoutBox.setText(Integer.toString(timeout));

      try {
        String pOpenOnImport = resource.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_OPEN_ON_IMPORT));
        if (pOpenOnImport==null) {
          openOnImport=true;
        } else {
          openOnImport=Boolean.parseBoolean(pOpenOnImport);
        }
        btnOpenOnImport.setSelection(openOnImport);
      } catch (NumberFormatException nfe) {}

      try {
        String pCustomFilenames = resource.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_CUSTOM_FILENAMES));
        if (pCustomFilenames==null) {
          customFilenames=true;
        } else {
          customFilenames=Boolean.parseBoolean(pCustomFilenames);
        }
        btnCustomFilenames.setSelection(customFilenames);
      } catch (NumberFormatException nfe) {}

      try {
        String pSubstitutionOnCompare = resource.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_SUBSTITUTION_ON_COMPARE));
        if (pSubstitutionOnCompare==null) {
          substitutionOnCompare=true;
        } else {
          substitutionOnCompare=Boolean.parseBoolean(pSubstitutionOnCompare);
        }
        btnSubstitutionOnCompare.setSelection(substitutionOnCompare);
      } catch (NumberFormatException nfe) {}
      
      sliderLbl.setText(Integer.toString(numLines));
      slider.setSelection(numLines);

    } catch (CoreException e) {
      // Some kind of exception - just leave the list empty
    }
  }

  //	private Composite createDefaultComposite(Composite parent) {
  //		Composite composite = new Composite(parent, SWT.NULL);
  //		GridLayout layout = new GridLayout();
  //		layout.numColumns = 2;
  //		composite.setLayout(layout);
  //
  //		GridData data = new GridData();
  //		data.verticalAlignment = GridData.FILL;
  //		data.horizontalAlignment = GridData.FILL;
  //		composite.setLayoutData(data);
  //
  //		return composite;
  //	}
  //
  protected void performDefaults() {
    super.performDefaults();
  }

  public boolean performOk() {
    // store the value in the owner text field
    try {
      StringBuilder sb=new StringBuilder();
      boolean first=true;
      for(String item: dirList.getItems()) {
        if(first) first=false;
        else sb.append(";");
        sb.append(item);
      }
      Object el=getElement();
      IResource resource=null;

      if(el instanceof JavaProject) {
        JavaProject jp=(JavaProject)getElement();
        resource = (IResource) jp.getProject();
      } else {
        resource = (IResource) el;
      }
      resource.setPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_EXCLUDED_DIRECTORIES),
          sb.toString());
      resource.setPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_NUM_LINES_BEFORE_CDATA),
          Integer.toString(slider.getSelection()));

      resource.setPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_CONNECTION_TIMEOUT),
          timeoutBox.getText());

      resource.setPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_OPEN_ON_IMPORT),
          Boolean.toString(btnOpenOnImport.getSelection()));

      resource.setPersistentProperty(
          new QualifiedName("", IIQPreferenceConstants.P_CUSTOM_FILENAMES),
          Boolean.toString(btnCustomFilenames.getSelection()));

      for(String pref: combos.keySet()) {
        Combo cmb = combos.get(pref);
        String value=(String)cmb.getData(cmb.getText());
        if (DEBUG_PROJECT) {
          IIQPlugin.logDebug("set "+pref+"="+value);
        }
        resource.setPersistentProperty(
            new QualifiedName("", pref),
            value);

      }

    } catch (CoreException e) {
      return false;
    }
    return true;
  }

  public void handleEvent(Event event) {
    setValid(validatePage());

  }

  public boolean validatePage() {
    // timeout value must be a +ve integer
    boolean valid=true;

    String sValue=timeoutBox.getText();
    int iValue=-1;
    try {
      iValue=Integer.parseInt(sValue);
    } catch (NumberFormatException nfe) {
      // do nothing; iValue is already invalid
    }
    if(iValue<=0) {
      setErrorMessage("Connection timeout must be a positive integer");
      valid=false;
    } else {
      setErrorMessage(null);
    }
    return valid;
  }
}
