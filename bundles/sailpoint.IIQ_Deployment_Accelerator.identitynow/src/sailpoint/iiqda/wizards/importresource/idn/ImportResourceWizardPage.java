package sailpoint.iiqda.wizards.importresource.idn;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.wizards.IImportResourceWizardTab;
import sailpoint.iiqda.wizards.ObjectDefinition;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rule).
 */

public class ImportResourceWizardPage extends WizardPage implements SelectionListener {

  TabFolder fldr;

  private IDNRestHandler restClient;

  private boolean isReady=false;

  /**
   * Constructor for SampleNewWizardPage.
   * @param pageName
   */
  public ImportResourceWizardPage(ISelection selection) {

    super("wizardPage");
    setTitle("Import a Resource");
    setDescription("This wizard imports a resource from an IdentitNow instance");

  }

  /**
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {

    Composite comp=new Composite(parent, SWT.NULL);
    GridLayout grid=new GridLayout(1, true);
    comp.setLayout(grid);

    fldr=new TabFolder(comp, SWT.BORDER);

    boolean ok=addListOfObjectsTab(fldr);
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

  }

  public void setRESTHandler(IDNRestHandler client) {
    this.restClient=client;
  }

  @Override
  public boolean isPageComplete() {
    int selectedTab=fldr.getSelectionIndex();
    IImportResourceWizardTab tab=(IImportResourceWizardTab)(fldr.getItem(selectedTab).getControl());
    return tab.canFinish();
  }

  public boolean isReady() {
    // don't show the dialog if we have an issue getting anything from IIQ
    return isReady;
  }

  private boolean addListOfObjectsTab(TabFolder fldr) {
    TabItem itm=new TabItem(fldr, SWT.NONE);
    itm.setText("Import by Category");   
    ListOfObjectsTab listObjTab=new ListOfObjectsTab(this, restClient, fldr);	
    itm.setControl(listObjTab);
    listObjTab.initialize();
    return listObjTab.initializedOK();
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
