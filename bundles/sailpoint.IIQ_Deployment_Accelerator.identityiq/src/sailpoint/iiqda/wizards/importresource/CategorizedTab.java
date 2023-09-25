package sailpoint.iiqda.wizards.importresource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.wizards.IImportResourceWizardTab;
import sailpoint.iiqda.wizards.ImportResourceWizardTab;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class CategorizedTab extends ImportResourceWizardTab implements IImportResourceWizardTab {

  private List<String> objectTypes;
  private Combo objectTypeSelector;
  private org.eclipse.swt.widgets.List objectSelector;
  private final ImportResourceWizardPage wizardPage;
  private IIQRESTClient client;
  private boolean initializedOK=false;

  public CategorizedTab(ImportResourceWizardPage wizardPage, IIQRESTClient client, TabFolder fldr) {
    super(fldr, SWT.NULL);
    this.client=client;
    this.wizardPage=wizardPage;
    // TODO Auto-generated method stub
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 9;
    layout.horizontalSpacing=10;
    setLayout(layout);

    Label objectTypeLabel = new Label(this, SWT.NONE);
    objectTypeLabel.setText("Object Type");
    objectTypeSelector = new Combo (this, SWT.DROP_DOWN|SWT.READ_ONLY);

    GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
    objectTypeSelector.setLayoutData(gd);
    objectTypeSelector.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        objectTypeChanged();
        objectSelector.setFocus();
      }
    });

    Label objectNamesLabel = new Label(this, SWT.NONE);
    objectNamesLabel.setText("Object Name");
    gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
    objectNamesLabel.setLayoutData(gd);

    objectSelector = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    //gd.horizontalSpan=2;
    objectSelector.setLayoutData(gd);		
    objectSelector.addSelectionListener(new CatSelectionListener(wizardPage));		
    objectTypeSelector.select(0);
  }

  private void objectTypeChanged() {
    List<String> objects=new ArrayList<String>();
    int iObjectType=objectTypeSelector.getSelectionIndex();
    if(iObjectType!=0) {
      String sObjectType=objectTypeSelector.getItem(iObjectType);
      try {
        objects=(List<String>)client.getObjects(sObjectType);
      } catch (ConnectionException ce) {
        CoreUtils.showConnectionError(getShell(), ce);
      }
      objectSelector.removeAll();
      for(String obj:objects) {
        objectSelector.add(obj);
      }
    }
  }

  public String getObjectType() {
    if(objectTypeSelector.getSelectionIndex()<1) {
      // -1 if nothing is selected
      // 0 if "--Select--"is selected
      return null;
    }
    return objectTypeSelector.getItem(objectTypeSelector.getSelectionIndex());
  }

  public String getObjectName() {
    if(objectSelector.getSelectionIndex()==-1) {
      // -1 if nothing is selected
      return null;
    }
    return objectSelector.getItem(objectSelector.getSelectionIndex());
  }

  @Override
  public boolean canFinish() {
    return (objectSelector.getSelectionIndex()!=-1);
  }

  @Override
  public void initialize() {

    try {
      objectTypes=client.getObjectTypes();
      objectTypeSelector.add("--Select--");
      for(String ot: objectTypes) {
        objectTypeSelector.add(ot);
      }
      objectTypeSelector.select(0);
    } catch (ConnectionException e) {
      CoreUtils.showConnectionError(wizardPage.getShell(), e);
      initializedOK=false;
      return;
    }
    initializedOK=true;
    //wizardPage.getWizard().performCancel();
  }

  public List<ObjectDefinition> getSelectedObjects() {
    String[] items=objectSelector.getSelection();
    String type=objectTypeSelector.getText();
    List<ObjectDefinition> objects=new ArrayList<ObjectDefinition>();
    for(String item: items) {
      objects.add(new ObjectDefinition(type, item, null));
    }
    return objects;
  }

  @Override
  public boolean initializedOK() {
    return initializedOK;
  }

}
