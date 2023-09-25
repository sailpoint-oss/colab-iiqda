package sailpoint.iiqda.editors.idn;

import java.io.InputStreamReader;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import flexjson.JSONDeserializer;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.objects.idn.CorrelationConfig;
import sailpoint.iiqda.objects.idn.Health;
import sailpoint.iiqda.objects.idn.Owner;
import sailpoint.iiqda.objects.idn.Source;
import sailpoint.iiqda.widgets.idn.TypeAheadWidget;

public class SourceEditor extends EditorPart {

  public SourceEditor() {
  }

  
  public static final String ID = "sailpoint.IdentityIQ_Deployment_Accelerator.IdentityNow.SourceEditor";
  private Source source;
  private FileEditorInput fileInput;
  private Text tIcon;
  private Text tDescription;
  private Text tLastUpdated;
  private Text tEntitlementsCount;
  private Text t;
  private Text tAccountsCount;
  private Text tID;
  private TypeAheadWidget tOwner;
  private Text tAppCount;
  private Text tDefinitionName;
  private Text tPasswordPolicy;
  private Text tCCAttributeAssignments;
  private Text tCCName;
  private Text tCCID;
  private Text tExternalID;
  private Text tHHostname;
  private Text tHLastSeen;
  private Text tHOrg;
  private Text tHLastChanged;
  private Text tHExternalID;
  private Text tHID;
  private Text tHType;
  private Text tHStatus;
  private Text tSince;
  private Text tIQServiceDownloadURL;
  private Text tAccessProfilesCount;
  private Text tApplicationTemplate;
  private Text tVersion;
  private Text tPasswdPolicyName;
  private Text tUserCount;
  private Text tSourceType;
  private Text tName;
  private Text tScriptName;
  private Button btnUseForProvisioning;
  private Button btnSptsEntAgg;
  private Button btnHasValidAccountProfile;
  private Button btnSourceConnected;
  private Button btnUseForAccounts;
  private Button btnAuthoritative;
  private Button btnUseForPasswdMgmt;
  private Button btnUseForAuthentication;
  private Button btnHHealthy;
  private Button btnHIsAuthoritative;
  
  
  private IProject theProject;

  @Override
  public void init(IEditorSite site, IEditorInput edInput)
      throws PartInitException {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.init:");
    if (!(edInput instanceof FileEditorInput)) {
      throw new RuntimeException("Wrong input");
    }

    this.fileInput = (FileEditorInput) edInput;
    setSite(site);
    setInput(edInput);
    
    IFile theFile=fileInput.getFile();
    theProject=theFile.getProject();
    JSONDeserializer json=new JSONDeserializer<Source>()
        .use(null, Source.class)
        .use("owner", Owner.class)
        .use("correlationConfig", CorrelationConfig.class)
        .use("health", Health.class)
        ;
    try {
      InputStreamReader srcReader = new InputStreamReader(theFile.getContents());
      this.source=(Source)json.deserialize(srcReader);
    } catch (CoreException ce) {
      throw new PartInitException("Can't deserialize file as Source object");
    } catch (Exception e) {
      System.out.println("..");
      e.printStackTrace();
    }
    
    setPartName("Source: " + source.getName());
  
  }

  @Override
  public void doSave(IProgressMonitor arg0) {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.doSave:");

  }

  @Override
  public void doSaveAs() {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.doSaveAs:");

  }


  @Override
  public boolean isDirty() {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.isDirty:");
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.isSaveAsAllowed:");
    return false;
  }

  @Override
  public void createPartControl(Composite arg0) {
    arg0.setLayout(new GridLayout(2, false));
    

    Composite column1=new Composite(arg0, SWT.NONE);
    column1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    column1.setLayout(new GridLayout(2, false));
    Composite column2=new Composite(arg0, SWT.NONE);
    column2.setLayout(new GridLayout(1, false));
    Composite col1Top=new Composite(column1, SWT.NONE);
    col1Top.setLayout(new GridLayout(3, false));
    col1Top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    
    
    
    btnUseForProvisioning = new Button(col1Top, SWT.CHECK);
    btnUseForProvisioning.setText("Use for Provisioning");
    
    btnSptsEntAgg = new Button(col1Top, SWT.CHECK);
    btnSptsEntAgg.setText("Supports Entitlement Aggregation");

    btnHasValidAccountProfile = new Button(col1Top, SWT.CHECK);
    btnHasValidAccountProfile.setText("Has Valid Account Profile");
    
    btnSourceConnected = new Button(col1Top, SWT.CHECK);
    btnSourceConnected.setText("Source Connected");
    
    btnUseForAccounts = new Button(col1Top, SWT.CHECK);
    btnUseForAccounts.setText("Use for Accounts");
    
    btnAuthoritative = new Button(col1Top, SWT.CHECK);
    btnAuthoritative.setText("Authoritative");
    
    btnUseForPasswdMgmt = new Button(col1Top, SWT.CHECK);
    btnUseForPasswdMgmt.setText("Use for password management");
    
    btnUseForAuthentication = new Button(col1Top, SWT.CHECK);
    btnUseForAuthentication.setText("Use for Authentication");
    
    Label lblIcon = new Label(column1, SWT.NONE);
    lblIcon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblIcon.setText("icon");
    
    tIcon = new Text(column1, SWT.BORDER);
    tIcon.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblDescription = new Label(column1, SWT.NONE);
    lblDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblDescription.setText("Description");
    
    tDescription = new Text(column1, SWT.BORDER);
    tDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblLastUpdated = new Label(column1, SWT.NONE);
    lblLastUpdated.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblLastUpdated.setText("Last Updated");
    
    tLastUpdated = new Text(column1, SWT.BORDER);
    tLastUpdated.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblEntitlementsCount = new Label(column1, SWT.NONE);
    lblEntitlementsCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblEntitlementsCount.setText("Entitlements Count");
    
    tEntitlementsCount = new Text(column1, SWT.BORDER);
    tEntitlementsCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblConnectorFeatures = new Label(column1, SWT.NONE);
    lblConnectorFeatures.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblConnectorFeatures.setText("Connector Features");
    
    t = new Text(column1, SWT.BORDER);
    t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblAccountsCount = new Label(column1, SWT.NONE);
    lblAccountsCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblAccountsCount.setText("Accounts Count");
    
    tAccountsCount = new Text(column1, SWT.BORDER);
    tAccountsCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblId = new Label(column1, SWT.NONE);
    lblId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblId.setText("ID");
    
    tID = new Text(column1, SWT.BORDER);
    tID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblOwner = new Label(column1, SWT.NONE);
    lblOwner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblOwner.setText("Owner");
    
    try {
    tOwner = new TypeAheadWidget(column1, SWT.BORDER, new IDNRestHandler(IDNHelper.getEnvironment(theProject, "default")));
    tOwner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    } catch (CoreException ce) {
      System.out.println("SourceEditor.createPartControl: CoreException trying to make tOwner: "+ce);

    }
    Label lblApplicationCount = new Label(column1, SWT.NONE);
    lblApplicationCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblApplicationCount.setText("Application Count");
    
    tAppCount = new Text(column1, SWT.BORDER);
    tAppCount.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblDefinitionName = new Label(column1, SWT.NONE);
    lblDefinitionName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblDefinitionName.setText("Definition Name");
    
    tDefinitionName = new Text(column1, SWT.BORDER);
    tDefinitionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblPasswordPolicy = new Label(column1, SWT.NONE);
    lblPasswordPolicy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblPasswordPolicy.setText("Password Policy");
    
    tPasswordPolicy = new Text(column1, SWT.BORDER);
    tPasswordPolicy.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Group grpCorrelationConfig = new Group(column2, SWT.NONE);
    grpCorrelationConfig.setLayout(new GridLayout(2, false));
    grpCorrelationConfig.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    grpCorrelationConfig.setText("Correlation Config");
    
    Label lblAttributeAssignments = new Label(grpCorrelationConfig, SWT.NONE);
    lblAttributeAssignments.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblAttributeAssignments.setText("Attribute Assignments");
    
    tCCAttributeAssignments = new Text(grpCorrelationConfig, SWT.BORDER);
    tCCAttributeAssignments.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblName = new Label(grpCorrelationConfig, SWT.NONE);
    lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblName.setText("Name");
    
    tCCName = new Text(grpCorrelationConfig, SWT.BORDER);
    tCCName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblId_1 = new Label(grpCorrelationConfig, SWT.NONE);
    lblId_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblId_1.setText("ID");
    
    tCCID = new Text(grpCorrelationConfig, SWT.BORDER);
    tCCID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblExternalId = new Label(column1, SWT.NONE);
    lblExternalId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblExternalId.setText("External ID");
    
    tExternalID = new Text(column1, SWT.BORDER);
    tExternalID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblIqserviceDownloadUrl = new Label(column1, SWT.NONE);
    lblIqserviceDownloadUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblIqserviceDownloadUrl.setText("IQService Download URL");
    
    tIQServiceDownloadURL = new Text(column1, SWT.BORDER);
    tIQServiceDownloadURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblAccessProfilesCount = new Label(column1, SWT.NONE);
    lblAccessProfilesCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblAccessProfilesCount.setText("Access Profiles Count");
    
    tAccessProfilesCount = new Text(column1, SWT.BORDER);
    tAccessProfilesCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblApplicationTemplate = new Label(column1, SWT.NONE);
    lblApplicationTemplate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblApplicationTemplate.setText("Application Template");
    
    tApplicationTemplate = new Text(column1, SWT.BORDER);
    tApplicationTemplate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblVersion = new Label(column1, SWT.NONE);
    lblVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblVersion.setText("Version");
    
    tVersion = new Text(column1, SWT.BORDER);
    tVersion.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblPasswordPolicyName = new Label(column1, SWT.NONE);
    lblPasswordPolicyName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblPasswordPolicyName.setText("Password Policy Name");
    
    tPasswdPolicyName = new Text(column1, SWT.BORDER);
    tPasswdPolicyName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblUserCount = new Label(column1, SWT.NONE);
    lblUserCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblUserCount.setText("User Count");
    
    tUserCount = new Text(column1, SWT.BORDER);
    tUserCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblSourceType = new Label(column1, SWT.NONE);
    lblSourceType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblSourceType.setText("Source Type");
    
    tSourceType = new Text(column1, SWT.BORDER);
    tSourceType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblName_1 = new Label(column1, SWT.NONE);
    lblName_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblName_1.setText("Name");
    
    tName = new Text(column1, SWT.BORDER);
    tName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblScriptName = new Label(column1, SWT.NONE);
    lblScriptName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblScriptName.setText("Script Name");
    
    tScriptName = new Text(column1, SWT.BORDER);
    tScriptName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Group grpHealth = new Group(column2, SWT.NONE);
    grpHealth.setLayout(new GridLayout(2, false));
    grpHealth.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    grpHealth.setText("Health");
    
    Label lblHostname = new Label(grpHealth, SWT.NONE);
    lblHostname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblHostname.setText("Hostname");
    
    tHHostname = new Text(grpHealth, SWT.BORDER);
    tHHostname.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblLastSeen = new Label(grpHealth, SWT.NONE);
    lblLastSeen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblLastSeen.setText("Last Seen");
    
    tHLastSeen = new Text(grpHealth, SWT.BORDER);
    tHLastSeen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblOrg = new Label(grpHealth, SWT.NONE);
    lblOrg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblOrg.setText("Org");
    
    tHOrg = new Text(grpHealth, SWT.BORDER);
    tHOrg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblHealthy = new Label(grpHealth, SWT.NONE);
    lblHealthy.setText("Healthy");
    
    btnHHealthy = new Button(grpHealth, SWT.CHECK);
    btnHHealthy.setText("Check Button");
    
    Label lblLastChanged = new Label(grpHealth, SWT.NONE);
    lblLastChanged.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblLastChanged.setText("Last Changed");
    
    tHLastChanged = new Text(grpHealth, SWT.BORDER);
    tHLastChanged.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblAuthoritative = new Label(grpHealth, SWT.NONE);
    lblAuthoritative.setText("Authoritative");
    
    btnHIsAuthoritative = new Button(grpHealth, SWT.CHECK);
    btnHIsAuthoritative.setText("Check Button");
    
    Label lblExternalId_1 = new Label(grpHealth, SWT.NONE);
    lblExternalId_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblExternalId_1.setText("External ID");
    
    tHExternalID = new Text(grpHealth, SWT.BORDER);
    tHExternalID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblId_2 = new Label(grpHealth, SWT.NONE);
    lblId_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblId_2.setText("ID");
    
    tHID = new Text(grpHealth, SWT.BORDER);
    tHID.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblType = new Label(grpHealth, SWT.NONE);
    lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblType.setText("Type");
    
    tHType = new Text(grpHealth, SWT.BORDER);
    tHType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblStatus = new Label(grpHealth, SWT.NONE);
    lblStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblStatus.setText("Status");
    
    tHStatus = new Text(grpHealth, SWT.BORDER);
    tHStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblSince = new Label(grpHealth, SWT.NONE);
    lblSince.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblSince.setText("Since");
    
    tSince = new Text(grpHealth, SWT.BORDER);
    tSince.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.createPartControl:");
    initDataBindings();

  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
    System.out.println("SourceEditor.setFocus:");

  }
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeSelectionBtnUseForProvisioningObserveWidget = WidgetProperties.selection().observe(btnUseForProvisioning);
    IObservableValue useForProvisioningSourceObserveValue = PojoProperties.value("useForProvisioning").observe(source);
    bindingContext.bindValue(observeSelectionBtnUseForProvisioningObserveWidget, useForProvisioningSourceObserveValue, null, null);
    //
    IObservableValue observeTextTIconObserveWidget = WidgetProperties.text(SWT.Modify).observe(tIcon);
    IObservableValue iconSourceObserveValue = PojoProperties.value("icon").observe(source);
    bindingContext.bindValue(observeTextTIconObserveWidget, iconSourceObserveValue, null, null);
    //
    IObservableValue observeTextTDescriptionObserveWidget = WidgetProperties.text(SWT.Modify).observe(tDescription);
    IObservableValue descriptionSourceObserveValue = PojoProperties.value("description").observe(source);
    bindingContext.bindValue(observeTextTDescriptionObserveWidget, descriptionSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnSptsEntAggObserveWidget = WidgetProperties.selection().observe(btnSptsEntAgg);
    IObservableValue supportsEntitlementAggregationSourceObserveValue = PojoProperties.value("supportsEntitlementAggregation").observe(source);
    bindingContext.bindValue(observeSelectionBtnSptsEntAggObserveWidget, supportsEntitlementAggregationSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnHasValidAccountProfileObserveWidget = WidgetProperties.selection().observe(btnHasValidAccountProfile);
    IObservableValue hasValidAccountProfileSourceObserveValue = PojoProperties.value("hasValidAccountProfile").observe(source);
    bindingContext.bindValue(observeSelectionBtnHasValidAccountProfileObserveWidget, hasValidAccountProfileSourceObserveValue, null, null);
    //
    IObservableValue observeTextTLastUpdatedObserveWidget = WidgetProperties.text(SWT.Modify).observe(tLastUpdated);
    IObservableValue lastUpdatedSourceObserveValue = PojoProperties.value("lastUpdated").observe(source);
    bindingContext.bindValue(observeTextTLastUpdatedObserveWidget, lastUpdatedSourceObserveValue, null, null);
    //
    IObservableValue observeTextTEntitlementsCountObserveWidget = WidgetProperties.text(SWT.Modify).observe(tEntitlementsCount);
    IObservableValue entitlementsCountSourceObserveValue = PojoProperties.value("entitlementsCount").observe(source);
    bindingContext.bindValue(observeTextTEntitlementsCountObserveWidget, entitlementsCountSourceObserveValue, null, null);
    //
    IObservableValue observeTextTObserveWidget = WidgetProperties.text(SWT.Modify).observe(t);
    IObservableValue connector_featuresStringSourceObserveValue = PojoProperties.value("connector_featuresString").observe(source);
    bindingContext.bindValue(observeTextTObserveWidget, connector_featuresStringSourceObserveValue, null, null);
    //
    IObservableValue observeTextTAccountsCountObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAccountsCount);
    IObservableValue accountsCountSourceObserveValue = PojoProperties.value("accountsCount").observe(source);
    bindingContext.bindValue(observeTextTAccountsCountObserveWidget, accountsCountSourceObserveValue, null, null);
    //
    IObservableValue observeTextTIDObserveWidget = WidgetProperties.text(SWT.Modify).observe(tID);
    IObservableValue idSourceObserveValue = PojoProperties.value("id").observe(source);
    bindingContext.bindValue(observeTextTIDObserveWidget, idSourceObserveValue, null, null);
    //
//    IObservableValue observeTextTOwnerObserveWidget = WidgetProperties.text(SWT.Modify).observe(tOwner);
//    IObservableValue ownerSourceObserveValue = PojoProperties.value("owner").observe(source);
//    bindingContext.bindValue(observeTextTOwnerObserveWidget, ownerSourceObserveValue, null, null);
    //
    IObservableValue observeTextTAppCountObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAppCount);
    IObservableValue appCountSourceObserveValue = PojoProperties.value("appCount").observe(source);
    bindingContext.bindValue(observeTextTAppCountObserveWidget, appCountSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnSourceConnectedObserveWidget = WidgetProperties.selection().observe(btnSourceConnected);
    IObservableValue sourceConnectedSourceObserveValue = PojoProperties.value("sourceConnected").observe(source);
    bindingContext.bindValue(observeSelectionBtnSourceConnectedObserveWidget, sourceConnectedSourceObserveValue, null, null);
    //
    IObservableValue observeTextTDefinitionNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tDefinitionName);
    IObservableValue definitionNameSourceObserveValue = PojoProperties.value("definitionName").observe(source);
    bindingContext.bindValue(observeTextTDefinitionNameObserveWidget, definitionNameSourceObserveValue, null, null);
    //
    IObservableValue observeTextTPasswordPolicyObserveWidget = WidgetProperties.text(SWT.Modify).observe(tPasswordPolicy);
    IObservableValue passwordPolicySourceObserveValue = PojoProperties.value("passwordPolicy").observe(source);
    bindingContext.bindValue(observeTextTPasswordPolicyObserveWidget, passwordPolicySourceObserveValue, null, null);
    //
    IObservableValue observeTextTExternalIDObserveWidget = WidgetProperties.text(SWT.Modify).observe(tExternalID);
    IObservableValue externalIdSourceObserveValue = PojoProperties.value("externalId").observe(source);
    bindingContext.bindValue(observeTextTExternalIDObserveWidget, externalIdSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnUseForAccountsObserveWidget = WidgetProperties.selection().observe(btnUseForAccounts);
    IObservableValue useForAccountsSourceObserveValue = PojoProperties.value("useForAccounts").observe(source);
    bindingContext.bindValue(observeSelectionBtnUseForAccountsObserveWidget, useForAccountsSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnAuthoritativeObserveWidget = WidgetProperties.selection().observe(btnAuthoritative);
    IObservableValue authoritativeSourceObserveValue = PojoProperties.value("authoritative").observe(source);
    bindingContext.bindValue(observeSelectionBtnAuthoritativeObserveWidget, authoritativeSourceObserveValue, null, null);
    //
    IObservableValue observeTextTIQServiceDownloadURLObserveWidget = WidgetProperties.text(SWT.Modify).observe(tIQServiceDownloadURL);
    IObservableValue iqServiceDownloadUrlSourceObserveValue = PojoProperties.value("iqServiceDownloadUrl").observe(source);
    bindingContext.bindValue(observeTextTIQServiceDownloadURLObserveWidget, iqServiceDownloadUrlSourceObserveValue, null, null);
    //
    IObservableValue observeTextTAccessProfilesCountObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAccessProfilesCount);
    IObservableValue accessProfilesCountSourceObserveValue = PojoProperties.value("accessProfilesCount").observe(source);
    bindingContext.bindValue(observeTextTAccessProfilesCountObserveWidget, accessProfilesCountSourceObserveValue, null, null);
    //
    IObservableValue observeTextTApplicationTemplateObserveWidget = WidgetProperties.text(SWT.Modify).observe(tApplicationTemplate);
    IObservableValue applicationTemplateSourceObserveValue = PojoProperties.value("applicationTemplate").observe(source);
    bindingContext.bindValue(observeTextTApplicationTemplateObserveWidget, applicationTemplateSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnUseForPasswdMgmtObserveWidget = WidgetProperties.selection().observe(btnUseForPasswdMgmt);
    IObservableValue useForPasswordManagementSourceObserveValue = PojoProperties.value("useForPasswordManagement").observe(source);
    bindingContext.bindValue(observeSelectionBtnUseForPasswdMgmtObserveWidget, useForPasswordManagementSourceObserveValue, null, null);
    //
    IObservableValue observeTextTVersionObserveWidget = WidgetProperties.text(SWT.Modify).observe(tVersion);
    IObservableValue versionSourceObserveValue = PojoProperties.value("version").observe(source);
    bindingContext.bindValue(observeTextTVersionObserveWidget, versionSourceObserveValue, null, null);
    //
    IObservableValue observeTextTPasswdPolicyNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tPasswdPolicyName);
    IObservableValue passwordPolicyNameSourceObserveValue = PojoProperties.value("passwordPolicyName").observe(source);
    bindingContext.bindValue(observeTextTPasswdPolicyNameObserveWidget, passwordPolicyNameSourceObserveValue, null, null);
    //
    IObservableValue observeTextTUserCountObserveWidget = WidgetProperties.text(SWT.Modify).observe(tUserCount);
    IObservableValue userCountSourceObserveValue = PojoProperties.value("userCount").observe(source);
    bindingContext.bindValue(observeTextTUserCountObserveWidget, userCountSourceObserveValue, null, null);
    //
    IObservableValue observeTextTSourceTypeObserveWidget = WidgetProperties.text(SWT.Modify).observe(tSourceType);
    IObservableValue sourceTypeSourceObserveValue = PojoProperties.value("sourceType").observe(source);
    bindingContext.bindValue(observeTextTSourceTypeObserveWidget, sourceTypeSourceObserveValue, null, null);
    //
    IObservableValue observeTextTNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tName);
    IObservableValue nameSourceObserveValue = PojoProperties.value("name").observe(source);
    bindingContext.bindValue(observeTextTNameObserveWidget, nameSourceObserveValue, null, null);
    //
    IObservableValue observeTextTScriptNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tScriptName);
    IObservableValue scriptNameSourceObserveValue = PojoProperties.value("scriptName").observe(source);
    bindingContext.bindValue(observeTextTScriptNameObserveWidget, scriptNameSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnUseForAuthenticationObserveWidget = WidgetProperties.selection().observe(btnUseForAuthentication);
    IObservableValue useForAuthenticationSourceObserveValue = PojoProperties.value("useForAuthentication").observe(source);
    bindingContext.bindValue(observeSelectionBtnUseForAuthenticationObserveWidget, useForAuthenticationSourceObserveValue, null, null);
    //
    IObservableValue observeTextTCCAttributeAssignmentsObserveWidget = WidgetProperties.text(SWT.Modify).observe(tCCAttributeAssignments);
    IObservableValue correlationConfigattributeAssignmentsSourceObserveValue = PojoProperties.value("correlationConfig.attributeAssignments").observe(source);
    bindingContext.bindValue(observeTextTCCAttributeAssignmentsObserveWidget, correlationConfigattributeAssignmentsSourceObserveValue, null, null);
    //
    IObservableValue observeTextTCCNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tCCName);
    IObservableValue correlationConfignameSourceObserveValue = PojoProperties.value("correlationConfig.name").observe(source);
    bindingContext.bindValue(observeTextTCCNameObserveWidget, correlationConfignameSourceObserveValue, null, null);
    //
    IObservableValue observeTextTCCIDObserveWidget = WidgetProperties.text(SWT.Modify).observe(tCCID);
    IObservableValue correlationConfigidSourceObserveValue = PojoProperties.value("correlationConfig.id").observe(source);
    bindingContext.bindValue(observeTextTCCIDObserveWidget, correlationConfigidSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHHostnameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHHostname);
    IObservableValue healthhostnameSourceObserveValue = PojoProperties.value("health.hostname").observe(source);
    bindingContext.bindValue(observeTextTHHostnameObserveWidget, healthhostnameSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHLastSeenObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHLastSeen);
    IObservableValue healthlastSeenSourceObserveValue = PojoProperties.value("health.lastSeen").observe(source);
    bindingContext.bindValue(observeTextTHLastSeenObserveWidget, healthlastSeenSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHOrgObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHOrg);
    IObservableValue healthorgSourceObserveValue = PojoProperties.value("health.org").observe(source);
    bindingContext.bindValue(observeTextTHOrgObserveWidget, healthorgSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnHHealthyObserveWidget = WidgetProperties.selection().observe(btnHHealthy);
    IObservableValue healthhealthySourceObserveValue = PojoProperties.value("health.healthy").observe(source);
    bindingContext.bindValue(observeSelectionBtnHHealthyObserveWidget, healthhealthySourceObserveValue, null, null);
    //
    IObservableValue observeTextTHLastChangedObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHLastChanged);
    IObservableValue healthlastChangedSourceObserveValue = PojoProperties.value("health.lastChanged").observe(source);
    bindingContext.bindValue(observeTextTHLastChangedObserveWidget, healthlastChangedSourceObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnHIsAuthoritativeObserveWidget = WidgetProperties.selection().observe(btnHIsAuthoritative);
    IObservableValue healthauthoritativeSourceObserveValue = PojoProperties.value("health.authoritative").observe(source);
    bindingContext.bindValue(observeSelectionBtnHIsAuthoritativeObserveWidget, healthauthoritativeSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHExternalIDObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHExternalID);
    IObservableValue healthexternalIdSourceObserveValue = PojoProperties.value("health.externalId").observe(source);
    bindingContext.bindValue(observeTextTHExternalIDObserveWidget, healthexternalIdSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHIDObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHID);
    IObservableValue healthidSourceObserveValue = PojoProperties.value("health.id").observe(source);
    bindingContext.bindValue(observeTextTHIDObserveWidget, healthidSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHTypeObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHType);
    IObservableValue healthtypeSourceObserveValue = PojoProperties.value("health.type").observe(source);
    bindingContext.bindValue(observeTextTHTypeObserveWidget, healthtypeSourceObserveValue, null, null);
    //
    IObservableValue observeTextTHStatusObserveWidget = WidgetProperties.text(SWT.Modify).observe(tHStatus);
    IObservableValue healthstatusSourceObserveValue = PojoProperties.value("health.status").observe(source);
    bindingContext.bindValue(observeTextTHStatusObserveWidget, healthstatusSourceObserveValue, null, null);
    //
    IObservableValue observeTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(tSince);
    IObservableValue healthsinceSourceObserveValue = PojoProperties.value("health.since").observe(source);
    bindingContext.bindValue(observeTextTextObserveWidget, healthsinceSourceObserveValue, null, null);
    //
    return bindingContext;
  }
}
