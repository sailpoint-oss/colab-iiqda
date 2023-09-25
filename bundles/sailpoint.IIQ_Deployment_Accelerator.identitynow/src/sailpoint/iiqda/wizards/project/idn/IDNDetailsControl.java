package sailpoint.iiqda.wizards.project.idn;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.deployer.RESTClient;
import sailpoint.iiqda.dialogs.GetTokenDialog;
import sailpoint.iiqda.dialogs.StepUpMethodDialog;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.idn.IDNEnvironment;
import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.idn.IDNRestHandler.APIPair;
import sailpoint.iiqda.idn.StepUpMethod;

public class IDNDetailsControl extends Composite implements Listener {

  private GridData gd_lOrg;
  private Text tOrganisation;
  private Text tAdminUser;
  private Text tAdminPass;
  private Text tAPIKey;
  private Text tAPISecret;

  private IDNPageData pageData;
  private IDNRestHandler restHandler;

  private boolean _validatedAPI=false;

  private boolean _isComplete;

  public IDNDetailsControl(Composite parent, int style) {
    super(parent, style);

    System.out.println("IDNDetailsControl.IDNDetailsControl: ");

    this.pageData=new IDNPageData();
    this.restHandler=new IDNRestHandler(null);
    
    GridLayout layout = new GridLayout();
    layout.numColumns = 4;
    setLayout(layout);


    Label lOrg=new Label(this, SWT.FILL);
    lOrg.setAlignment(SWT.RIGHT);
    lOrg.setText("Organisation name");      
    gd_lOrg = new GridData(GridData.VERTICAL_ALIGN_END);
    gd_lOrg.verticalAlignment = SWT.FILL;
    gd_lOrg.horizontalAlignment = GridData.FILL;
    lOrg.setLayoutData(gd_lOrg);

    tOrganisation = new Text(this, SWT.BORDER);
    tOrganisation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    Label lblidentitynowcom = new Label(this, SWT.NONE);
    lblidentitynowcom.setText(".identitynow.com");

    Label lblNewLabel = new Label(this, SWT.NONE);
    lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblNewLabel.setText("Admin username");

    tAdminUser = new Text(this, SWT.BORDER);
    tAdminUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    new Label(this, SWT.NONE);

    Label lblAdminPassword = new Label(this, SWT.NONE);
    lblAdminPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblAdminPassword.setText("Admin password");

    tAdminPass = new Text(this, SWT.BORDER | SWT.PASSWORD);
    tAdminPass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    Button btnGenerateApiKeysecret = new Button(this, SWT.WRAP);
    btnGenerateApiKeysecret.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
    btnGenerateApiKeysecret.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getAPICredentials();
      }
    });
    btnGenerateApiKeysecret.setText("Get API Key");
    new Label(this, SWT.NONE);
    new Label(this, SWT.NONE);
    new Label(this, SWT.NONE);
    new Label(this, SWT.NONE);

    Label lblApiKey = new Label(this, SWT.NONE);
    lblApiKey.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblApiKey.setText("API Key *");

    tAPIKey = new Text(this, SWT.BORDER);
    GridData gd_tAPIKey = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
    gd_tAPIKey.widthHint = 300;
    tAPIKey.setLayoutData(gd_tAPIKey);

    tAPIKey.addListener(SWT.Modify, this);
    new Label(this, SWT.NONE);

    Label lblApiSecret = new Label(this, SWT.NONE);
    lblApiSecret.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblApiSecret.setText("API Secret *");

    tAPISecret = new Text(this, SWT.BORDER);
    tAPISecret.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    Button btnValidate = new Button(this, SWT.NONE);
    btnValidate.setText("Validate");
    btnValidate.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        validateAPICredentials();
      }
    });

    tOrganisation.addListener(SWT.Modify, this);
    tAPIKey.addListener(SWT.Modify, this);
    tAPISecret.addListener(SWT.Modify, this);

    initDataBindings();
    
  }

  
  
  protected void validateAPICredentials() {
    System.out.println("IDNNewProjectCreationPage.validateAPICredentials:");
    restHandler.setOrganisation(pageData.getOrganisation());
    try {
      _validatedAPI=restHandler.validateAPICredentials(pageData.getAPIKey(), pageData.getAPISecret());
      Shell shell = getShell();
      MessageBox dialog =
          new MessageBox(shell, SWT.ICON_INFORMATION| SWT.OK);
      dialog.setText("Success");
      dialog.setMessage("API credentials validated");

      // open dialog and await user selection
      dialog.open();
    } catch (ConnectionException ce) {
      _validatedAPI=false;
      Shell shell = getShell();
      MessageBox dialog =
          new MessageBox(shell, SWT.ICON_INFORMATION| SWT.OK);
      dialog.setText("Success");
      dialog.setMessage("API credentials not valid");
      
      // open dialog and await user selection
      dialog.open();
    }
    Event e=new Event();
    notifyListeners(SWT.Modify, e);
    
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    System.out.println("Init data bindings");
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeTextTOrganisationObserveWidget = WidgetProperties.text(SWT.Modify).observe(tOrganisation);
    IObservableValue organisationPageDataObserveValue = BeanProperties.value("organisation").observe(pageData);
    bindingContext.bindValue(observeTextTOrganisationObserveWidget, organisationPageDataObserveValue, null, null);
    //
    IObservableValue observeTextTAdminUserObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAdminUser);
    IObservableValue adminUserPageDataObserveValue = BeanProperties.value("adminUser").observe(pageData);
    bindingContext.bindValue(observeTextTAdminUserObserveWidget, adminUserPageDataObserveValue, null, null);
    //
    IObservableValue observeTextTAdminPassObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAdminPass);
    IObservableValue adminPassPageDataObserveValue = BeanProperties.value("adminPass").observe(pageData);
    bindingContext.bindValue(observeTextTAdminPassObserveWidget, adminPassPageDataObserveValue, null, null);
    //
    IObservableValue observeTextTAPIKeyObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAPIKey);
    IObservableValue aPIKeyPageDataObserveValue = BeanProperties.value("APIKey").observe(pageData);
    bindingContext.bindValue(observeTextTAPIKeyObserveWidget, aPIKeyPageDataObserveValue, null, null);
    //
    IObservableValue observeTextTAPISecretObserveWidget = WidgetProperties.text(SWT.Modify).observe(tAPISecret);
    IObservableValue aPISecretPageDataObserveValue = BeanProperties.value("APISecret").observe(pageData);
    bindingContext.bindValue(observeTextTAPISecretObserveWidget, aPISecretPageDataObserveValue, null, null);
    //
    return bindingContext;
  }

  @Override
  public void handleEvent(Event e) {
    _validatedAPI=false;
    _isComplete=pageData.getOrganisation()!=null && pageData.getAPIKey()!=null && pageData.getAPISecret()!=null;
    notifyListeners(SWT.Modify, e);

  }

  public IDNPageData getIDNData() {
    return pageData;
  }
  
  protected void getAPICredentials() {
    System.out.println("IDNNewProjectCreationPage.getAPICredentials:");
    restHandler.setOrganisation(pageData.getOrganisation());
    try {
      APIPair pair=getAPICredentials(restHandler, pageData.getAdminUser(), pageData.getAdminPass());
      pageData.setAPIKey(pair.key);
      pageData.setAPISecret(pair.secret);
    } catch (ConnectionException ce) {
      System.out.println("Should pop a dialog here "+ce);
      _validatedAPI=false;
      Shell shell = getShell();
      MessageBox dialog =
          new MessageBox(shell, SWT.ICON_INFORMATION| SWT.OK);
      dialog.setText("Connection Failed");
      dialog.setMessage("Connection failed:\n"+ce);
      
      // open dialog and await user selection
      dialog.open();
    }
  }
  
  public boolean isValidAPI() {
    return _validatedAPI;
  }
  
  public boolean isComplete() {
    return _isComplete;
  }

  public void setValues(IDNEnvironment input) {
    if (input!=null) {
      pageData.setOrganisation(input.getOrganisation());
      pageData.setAPIKey(input.getApiKey());
      pageData.setAPISecret(input.getApiSecret());
    }
    
  }
  
  private APIPair getAPICredentials(IDNRestHandler handler, String username, String password) throws ConnectionException {

    RESTClient client=new RESTClient(30000);


    handler.getAuthDetails(client, username);
    handler.login(client, username, password);
    List<StepUpMethod> methods=handler.getStepUpMethods(client);
    
    // present methods to user
    StepUpMethodDialog dlg=new StepUpMethodDialog(getShell(), methods);
    int ok=dlg.open();
    if (ok==0) {
      // user selected something
      StepUpMethod method=dlg.getSelectedMethod();
      if (method.getType()==StepUpMethod.Type.CODE) {
        // Selected method is a CODE type method (token to phone, or email, or something)
        handler.sendVerificationToken(client, method);
        // Now get the token, and step up
        GetTokenDialog gtd=new GetTokenDialog(getShell());
        ok=gtd.open();
        if (ok==0) {
          String token=gtd.getToken();
          handler.stepUpAuth(client, token);
        }        
      } else if (method.getType()==StepUpMethod.Type.PASSWORD) {
        handler.stepUpAuth(client, username, password);      
      } else {
        throw new ConnectionException("Don't know how to do step up for "+method.getDescription()+" ("+method.getStrongAuthType()+")");
      }
      
      return handler.createAPIKeySet(client);
    }
    // not ok - user closed the window
    return null;
    
    // deal with chosen method
    // Verification token
    //    https://enterprise104.identitynow.com/api/user/sendVerificationToken
    //    form body: via="<StrongAuthMethod>"
    // Then sent token to stepUpAuth with form body token=xxxx
    
    

  }
}
