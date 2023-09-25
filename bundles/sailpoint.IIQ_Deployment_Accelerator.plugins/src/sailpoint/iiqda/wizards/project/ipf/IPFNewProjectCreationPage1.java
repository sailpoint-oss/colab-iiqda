package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.IPFPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.RightConverter;

public class IPFNewProjectCreationPage1 extends WizardPage implements Listener {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IPFPlugin.PLUGIN_ID+"/debug/Wizards"));

  public static final String pageName="New IPF Project";

  private PageOneData data;
  
  private Text txtDescriptiveName;
  private boolean tDescriptiveNameChanged;
  
  private Text txtUniqueName;
  private boolean tUniqueNameChanged;
  
  private Text txtVersion;
  private Text txtMinSystemVersion;

  private GridData gd_tDescriptiveName;
  private Label l_1;
  private GridData gd_tUniqueName;
  private Label lblPluginRightRequired;
  private Text txtPluginRight;

	private NewIPFProjectWizard wiz;

  // This page just subclasses WizardNewProjectCreationPage
  // In order to get the new Project name

	private String oldDescriptiveName;
	private String oldUniqueName;
	private Label lblMinimumUpgradable;
	private Text txtMinUpgrade;
	private boolean minUpModified;
	private String tempMinUp;
	private GridData gd_tMinSystemVersion;
	
  public IPFNewProjectCreationPage1(NewIPFProjectWizard wiz) {

    super("Basic Information");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.basicInfo"));
    setPageComplete(false);
    data=new PageOneData();
    this.wiz=wiz;
    

  }
  
  public PageOneData getData() {
  	return data;
  }
  
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // page is being moved into
      if (!tDescriptiveNameChanged) {
        txtDescriptiveName.setText(wiz.getProjectPage().getProjectName());
      }
      oldDescriptiveName=txtDescriptiveName.getText();

      if (!tUniqueNameChanged) {
        txtUniqueName.setText(CoreUtils.toCamelCase(wiz.getProjectPage().getProjectName()));
      }
      oldUniqueName=txtUniqueName.getText();
      
    } else {
      // page is being moved out of
      if (data.getPluginRight()!=null &&
          data.getPluginRight().length()>0 ) {
        wiz.addRight(data.getPluginRight());			
      }
    }
    super.setVisible(visible);
  }

  public String getRightName() {
    return txtPluginRight.getText();
  }
  
	@Override
  public void createControl(Composite parent) {
    if (DEBUG_WIZARDS) IPFPlugin.logDebug("parent="+Integer.toHexString(parent.hashCode()));

    //Composite thecontrol = (Composite)getControl();
    Composite projectGroup = new Composite(parent, SWT.NONE);
    if (DEBUG_WIZARDS) IPFPlugin.logDebug("projectGroup="+Integer.toHexString(projectGroup.hashCode()));

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    projectGroup.setLayout(layout);
    
    GridData gridData = new GridData();
    gridData.horizontalAlignment=GridData.FILL;
    gridData.verticalAlignment=GridData.FILL;
    gridData.grabExcessVerticalSpace=true;    
    projectGroup.setLayoutData(gridData);

    Label l=new Label(projectGroup, SWT.NONE);
    l.setText("Descriptive Name:");			
    txtDescriptiveName=new Text(projectGroup, SWT.FILL);
    gd_tDescriptiveName = new GridData(GridData.VERTICAL_ALIGN_END);
    gd_tDescriptiveName.horizontalAlignment = GridData.FILL;
    txtDescriptiveName.setLayoutData(gd_tDescriptiveName);
    txtDescriptiveName.addListener(SWT.CHANGED, this);
    tDescriptiveNameChanged=false;
    data.setDescriptiveName(wiz.getProjectPage().getName());

    l_1=new Label(projectGroup, SWT.NONE);
    l_1.setText("Unique Name:");
    txtUniqueName=new Text(projectGroup, SWT.FILL);
    gd_tUniqueName = new GridData(GridData.VERTICAL_ALIGN_END);
    gd_tUniqueName.horizontalAlignment = GridData.FILL;
    txtUniqueName.setLayoutData(gd_tUniqueName);
    txtUniqueName.addListener(SWT.CHANGED, this);
    tUniqueNameChanged=false;
    data.setUniqueName(CoreUtils.toCamelCase(data.getDescriptiveName()));
    
    l=new Label(projectGroup, SWT.NONE);
    l.setText("Version Number:");			
    txtVersion=new Text(projectGroup, SWT.FILL);
    data.setVersion("0.1");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    txtVersion.setLayoutData(gridData);

    l=new Label(projectGroup, SWT.NONE);
    l.setText(IPFStrings.getString("IPFNewProjectCreationPage1.l.text")); //$NON-NLS-1$
    txtMinSystemVersion=new Text(projectGroup, SWT.FILL);
    data.setMinSystemVersion("7.1");
    gd_tMinSystemVersion = new GridData(GridData.VERTICAL_ALIGN_END);
    gd_tMinSystemVersion.horizontalAlignment = GridData.FILL;
    txtMinSystemVersion.setLayoutData(gd_tMinSystemVersion);

    setControl(projectGroup);
    
    lblMinimumUpgradable = new Label(projectGroup, SWT.NONE);
    lblMinimumUpgradable.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblMinimumUpgradable.setText(IPFStrings.getString("IPFNewProjectCreationPage1.lblMinimumUpgradable.text")); //$NON-NLS-1$
    
    txtMinUpgrade = new Text(projectGroup, SWT.BORDER);
    data.setMinUpgradeable("0.1");
    txtMinUpgrade.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    txtMinUpgrade.addFocusListener(new FocusListener() {
      
      
      @Override
      public void focusLost(FocusEvent e) {
        if (tempMinUp.equals(txtMinUpgrade.getText())) {
          // do nothing
        } else {
          minUpModified=true;
        }
      }
      
      @Override
      public void focusGained(FocusEvent e) {
        tempMinUp = txtVersion.getText();
      }
    });
    
    lblPluginRightRequired = new Label(projectGroup, SWT.NONE);
    lblPluginRightRequired.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblPluginRightRequired.setText("Plugin Right Required:");
    
    txtPluginRight = new Text(projectGroup, SWT.BORDER);
    txtPluginRight.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    data.setPluginRight("");
    initDataBindings();

    ModifyListener modLsnr=new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        
        // If we've modified the method or the REST Endpoint name
        // and we haven't modified the java method name manually then generate it
        if (e.getSource()==txtVersion) {
          if (!minUpModified 
              && ( txtVersion.getText()!=null && txtVersion.getText().length()>0) ) {
            txtMinUpgrade.setText( txtVersion.getText() );
          }
        }
        
      }
    };
    txtVersion.addModifyListener(modLsnr);
  }

  public void handleEvent(Event event) {
    setPageComplete(isPageComplete());
  }

  @Override
  public boolean isPageComplete() {
    // if any of these is false, we can return straight away
    boolean 
    ret=data.getDescriptiveName().length()>0 &&
        data.getUniqueName().length()>0 &&
        data.getVersion().length()>0;
    if(!ret) return false;
    ret=ret&&isVersionNumber(data.getVersion())
        &&isVersionNumber(data.getMinSystemVersion());
    
    return ret;
  }

  private boolean isVersionNumber(String text) {
    for (char c: text.toCharArray()) {
      if ( c!='.' && ( c<'0' || c>'9') ) {
        return false;
      }
    }
    return true;
  }
  //CoreActivator.toCamelCase(txtDescriptiveName.getText())
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    UpdateValueStrategy strategy = new UpdateValueStrategy();
    //
    IObservableValue observeTextTDescriptiveNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtDescriptiveName);
    IObservableValue descriptiveNameDataObserveValue = BeanProperties.value("descriptiveName").observe(data);
    bindingContext.bindValue(observeTextTDescriptiveNameObserveWidget, descriptiveNameDataObserveValue);
    //
    IObservableValue observeTextTUniqueNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtUniqueName);
    IObservableValue uniqueNameDataObserveValue = BeanProperties.value("uniqueName").observe(data);
    bindingContext.bindValue(observeTextTUniqueNameObserveWidget, uniqueNameDataObserveValue, null, null);

    //
    IObservableValue observeTextTVersionObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtVersion);
    IObservableValue versionDataObserveValue = BeanProperties.value("version").observe(data);
    bindingContext.bindValue(observeTextTVersionObserveWidget, versionDataObserveValue, null, null);
    //
    IObservableValue textTxtPluginRightObserveValue = PojoProperties.value("text").observe(txtPluginRight);
    UpdateValueStrategy strategy_1 = new UpdateValueStrategy();
    strategy_1.setConverter(new RightConverter());
    bindingContext.bindValue(observeTextTUniqueNameObserveWidget, textTxtPluginRightObserveValue, strategy_1, strategy);
    //
    IObservableValue textTxtUniqueNameObserveValue = PojoProperties.value("text").observe(txtUniqueName);
    UpdateValueStrategy strategy_2 = new UpdateValueStrategy();
    strategy_2.setConverter(new sailpoint.iiqda.wizards.CamelConverter());
    bindingContext.bindValue(observeTextTDescriptiveNameObserveWidget, textTxtUniqueNameObserveValue, strategy_2, strategy);
    //
    IObservableValue observeTextTxtPluginRightObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtPluginRight);
    IObservableValue pluginRightDataObserveValue = BeanProperties.value("pluginRight").observe(data);
    bindingContext.bindValue(observeTextTxtPluginRightObserveWidget, pluginRightDataObserveValue, null, null);
    //
    IObservableValue observeTextTMinUpgradeObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMinUpgrade);
    IObservableValue minUpgradeableDataObserveValue = BeanProperties.value("minUpgradeable").observe(data);
    bindingContext.bindValue(observeTextTMinUpgradeObserveWidget, minUpgradeableDataObserveValue, null, null);
    //
    IObservableValue observeTextTMinSystemVersionObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMinSystemVersion);
    IObservableValue minSystemVersionDataObserveValue = BeanProperties.value("minSystemVersion").observe(data);
    bindingContext.bindValue(observeTextTMinSystemVersionObserveWidget, minSystemVersionDataObserveValue, null, null);
    //
    return bindingContext;
  }
}


