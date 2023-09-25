package sailpoint.iiqda.dialogs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.idn.IDNEnvironment;
import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.idn.IDNDetailsControl;
import sailpoint.iiqda.wizards.project.idn.IDNPageData;

public class IDNDetailsDialog extends Dialog implements Listener {

  private Text tName;
  private IDNDetailsControl deets;

  public class DlgData extends AbstractModelObject {
    private String sName;
    public void setName(String name) {
      this.sName=name;
    }
    
    public String getName() {
      return sName;
    }
  }
  
  private DlgData dlgData;
  private IDNEnvironment envData;
  private Composite compName;

  public IDNDetailsDialog(Shell parent) {
    super(parent);
    dlgData=new DlgData();
  }
  

  @Override
  protected Control createDialogArea(Composite parent) {
    System.out.println("IDNDetailsDialog.createDialogArea:");
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.numColumns = 1;
    
    compName = new Composite(container, SWT.NONE);
    compName.setLayout(new GridLayout(2, false));
    compName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
   
    
    Label lblName = new Label(compName, SWT.NONE);
    lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblName.setText("Name");
    
    tName = new Text(compName, SWT.BORDER);
    tName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    deets = new IDNDetailsControl(container, SWT.NONE);
    deets.addListener(SWT.Modify, this);
    deets.setValues(envData);
    initDataBindings();
    
    return container;
  }

  public IDNEnvironment open(IDNEnvironment input) {
    System.out.println("IDNDetailsDialog.open: ");

    if (input!=null) {
      dlgData.setName(input.getName());
      this.envData=input;
    }

    int open=this.open();
    System.out.println("open="+open+"("+SWT.OK+")");
    if (open==0) {
      IDNPageData pd=deets.getIDNData();
      IDNEnvironment output=new IDNEnvironment(dlgData.getName(), pd.getOrganisation(), pd.getAPIKey(), pd.getAPISecret());
      return output;
    }
    return null;
  }

  @Override
  public void handleEvent(Event arg0) {    
    System.out.println("Listener.handleEvent:");
    boolean ok=true;
    if (tName.getText()==null||tName.getText().length()==0) ok=false;
    if (deets.getIDNData().getOrganisation()==null) ok=false;
    if (deets.getIDNData().getAPIKey()==null) ok=false;
    if (deets.getIDNData().getAPISecret()==null) ok=false;
      
    Button button = getButton(IDialogConstants.OK_ID);
    if (button!=null) button.setEnabled(ok);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeTextTNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(tName);
    IObservableValue nameDlgDataObserveValue = BeanProperties.value("name").observe(dlgData);
    bindingContext.bindValue(observeTextTNameObserveWidget, nameDlgDataObserveValue, null, null);
    //
    return bindingContext;
  }
}
