package sailpoint.iiqda.wizards.importresource;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.IImportResourceWizardTab;
import sailpoint.iiqda.wizards.ObjectDefinition;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rule).
 */

public class ImportResourceWizardPage extends WizardPage implements SelectionListener {

  TabFolder fldr;

  private IIQRESTClient restClient;

  private boolean isReady=false;

  private Button btn;

  /**
   * Constructor for SampleNewWizardPage.
   * @param pageName
   */
  public ImportResourceWizardPage(ISelection selection) {

    super("wizardPage");
    setTitle("Import a Resource");
    setDescription("This wizard imports a resource from a running IdentityIQ instance");

  }

  /**
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {

    Composite comp=new Composite(parent, SWT.NULL);
    GridLayout grid=new GridLayout(1, true);
    comp.setLayout(grid);

    fldr=new TabFolder(comp, SWT.BORDER);

    boolean ok=addCategorizedTab(fldr);
    if (ok) {
      ok=addDateOrderedTab(fldr);
    }
    setControl(fldr);
    if(!ok) {
      isReady=false;
      return;
    }
    GridData gd=new GridData(GridData.FILL_BOTH);
    gd.grabExcessHorizontalSpace=true;
    gd.grabExcessVerticalSpace=true;
    fldr.setLayoutData(gd);
    fldr.addSelectionListener(this);
    isReady=true;

    btn = new Button(comp, SWT.CHECK);
    btn.setText("Automatically surround beanshell with CDATA tags");
    btn.setSelection(IIQPlugin.getDefault().getBooleanPreference(IIQPreferenceConstants.P_IMPORT_AUTO_CDATA));
  }

  public void setRESTClient(IIQRESTClient client) {
    this.restClient=client;
  }

  @Override
  public boolean isPageComplete() {
    int selectedTab=fldr.getSelectionIndex();
    IImportResourceWizardTab tab=(IImportResourceWizardTab)(fldr.getItem(selectedTab).getControl());
    return tab.canFinish();
  }

  public boolean shouldInsertCDATA() {
    return btn.getSelection();
  }

  public boolean isReady() {
    // don't show the dialog if we have an issue getting anything from IIQ
    return isReady;
  }

  private boolean addCategorizedTab(TabFolder fldr) {
    TabItem itm=new TabItem(fldr, SWT.NONE);
    itm.setText("Import by Category");   
    CategorizedTab categorizedTab=new CategorizedTab(this, restClient, fldr);	
    itm.setControl(categorizedTab);
    categorizedTab.initialize();
    return categorizedTab.initializedOK();
  }

  private boolean addDateOrderedTab(TabFolder fldr) {
    TabItem itm=new TabItem(fldr, SWT.NONE);
    itm.setText("Import by Recently Changed");   
    DateOrderedTab dateTab=new DateOrderedTab(this, restClient, fldr);
    itm.setControl(dateTab);
    dateTab.initialize();
    return dateTab.initializedOK();
  }

  public List<ObjectDefinition> getSelectedObjects() {
    int selectedTab=fldr.getSelectionIndex();
    IImportResourceWizardTab tab=(IImportResourceWizardTab)(fldr.getItem(selectedTab).getControl());
    return tab.getSelectedObjects();
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e) {
    this.widgetSelected(e);
  }

  @Override
  public void widgetSelected(SelectionEvent e) {
    this.setPageComplete(this.isPageComplete());
  }

}
