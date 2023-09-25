package sailpoint.iiqda.wizards.project.ipf;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.Setting;
import sailpoint.iiqda.wizards.project.SettingHelpEditingSupport;
import sailpoint.iiqda.wizards.project.SettingLabelEditingSupport;
import sailpoint.iiqda.wizards.project.SettingNameEditingSupport;
import sailpoint.iiqda.wizards.project.SettingTypeEditingSupport;
import sailpoint.iiqda.wizards.project.SettingValueEditingSupport;

public class IPFNewProjectCreationPageFullPage extends WizardPage {
  private Text txtFullPageName;
  private Button btnEnableSettings;
  private Button btnEnableFull;
  private Composite container;

  private PageDataFullPage data;
  private Composite tblSettings;
  private Table table;
  private TableViewer tableViewer;
  private TableColumn tableColumn;
  private TableViewerColumn tableViewerColumn;
  private TableColumn tableColumn_1;
  private TableViewerColumn tableViewerColumn_1;
  private TableViewerColumn nameViewCol;
  private TableViewerColumn labelViewCol;
  private TableViewerColumn valueViewCol;
  private TableViewerColumn datatypeViewCol;
  private TableViewerColumn helptextViewCol;
  private Button btnUseAngular;

  protected IPFNewProjectCreationPageFullPage() {
    super("Full Pages");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.fullPage"));
    this.data=new PageDataFullPage();
    Setting a=new Setting("", "", "", "", "");
    this.data.getSettings().add(a);
  }

  public PageDataFullPage getData() {
    return data;
  }

  @Override
  public void createControl(Composite parent) {
    container = new Composite(parent, SWT.NULL);
    container.setLayout(new GridLayout(1, false));
    setControl(container);

    Group grpFullPage = new Group(container, SWT.NONE);
    grpFullPage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    grpFullPage.setText("Full Page");
    grpFullPage.setLayout(new GridLayout(2, false));

    btnEnableFull = new Button(grpFullPage, SWT.CHECK);
    btnEnableFull.setText("Enable");
    data.setEnableFull(false);
//    btnEnableFull.addSelectionListener(new SelectionListener() {
//      
//      @Override
//      public void widgetDefaultSelected(SelectionEvent e) {
//        widgetSelected(e);
//      }
//      
//      @Override
//      public void widgetSelected(SelectionEvent e) {
//        boolean selected=btnEnableFull.getSelection();
//        txtFullPageName.setEnabled(selected);
//      }
//      
//    });
    new Label(grpFullPage, SWT.NONE);

    Label lblFullPageName = new Label(grpFullPage, SWT.NONE);
    lblFullPageName.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
    lblFullPageName.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.lblDesktopUrl.text")); //$NON-NLS-1$

    txtFullPageName = new Text(grpFullPage, SWT.BORDER);
    txtFullPageName.setEnabled(false);
    txtFullPageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    btnUseAngular = new Button(grpFullPage, SWT.CHECK);
    btnUseAngular.setEnabled(false);
    btnUseAngular.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.btnUseAngular.text")); //$NON-NLS-1$
    new Label(grpFullPage, SWT.NONE);

    Group gSettings = new Group(container, SWT.NONE);
    gSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    gSettings.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.gSettings.text")); //$NON-NLS-1$
    gSettings.setLayout(new GridLayout(1, false));

    btnEnableSettings = new Button(gSettings, SWT.CHECK);
    btnEnableSettings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    btnEnableSettings.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.btnEnableSettings.text"));

    ArrayContentProvider instance = ArrayContentProvider.getInstance();    

    tblSettings = new Composite(gSettings, SWT.NONE);
    tblSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    tblSettings.setLayout(new GridLayout(1, false));

    tableViewer = new TableViewer(tblSettings, SWT.BORDER | SWT.FULL_SELECTION);
    table = tableViewer.getTable();
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    tableViewer.setContentProvider(instance);
    tableViewer.setInput(data.getSettings());

    nameViewCol = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn colName = nameViewCol.getColumn();
    colName.setWidth(217);
    colName.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.tblclmnName.text")); //$NON-NLS-1$
    nameViewCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Setting p = (Setting) element;
        return p.getName();
      }
    });
    nameViewCol.setEditingSupport(new SettingNameEditingSupport(tableViewer)); 

    labelViewCol = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn colLabel = labelViewCol.getColumn();
    colLabel.setWidth(217);
    colLabel.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.tblclmnLabel.text")); //$NON-NLS-1$
    labelViewCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Setting p = (Setting) element;
        return p.getLabel();
      }
    });
    labelViewCol.setEditingSupport(new SettingLabelEditingSupport(tableViewer)); 

    helptextViewCol = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn colHelpText = helptextViewCol.getColumn();
    colHelpText.setWidth(217);
    colHelpText.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.tblclmnHelp.text")); //$NON-NLS-1$
    helptextViewCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Setting p = (Setting) element;
        return p.getHelpText();
      }
    });
    helptextViewCol.setEditingSupport(new SettingHelpEditingSupport(tableViewer)); 


    valueViewCol = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn colValue = valueViewCol.getColumn();
    colValue.setWidth(471);
    colValue.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.tblclmnDefaultValue.text")); //$NON-NLS-1$
    valueViewCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Setting p = (Setting) element;
        return p.getValue();
      }
    });  
    valueViewCol.setEditingSupport(new SettingValueEditingSupport(tableViewer)); 

    datatypeViewCol = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn colDataType = datatypeViewCol.getColumn();
    colDataType.setWidth(106);
    colDataType.setText(IPFStrings.getString("IPFNewProjectCreationPageFullPage.tblclmnType.text")); //$NON-NLS-1$
    datatypeViewCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Setting p = (Setting) element;
        return p.getDataType();
      }
    });  
    datatypeViewCol.setEditingSupport(new SettingTypeEditingSupport(tableViewer)); 

    // Set up so that <tab> goes to next field in the table, *not* the next component in the window

    TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));

    ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
      protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
        if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
          EventObject source = event.sourceEvent;
          if (source instanceof MouseEvent && ((MouseEvent)source).button == 3)
            return false;
        }
        return super.isEditorActivationEvent(event) || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR);
      }
    };

    TableViewerEditor.create(tableViewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL | 
        ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | 
        ColumnViewerEditor.TABBING_VERTICAL |
        ColumnViewerEditor.KEYBOARD_ACTIVATION);


    tableViewer.refresh();
    btnEnableSettings.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        boolean selected=btnEnableSettings.getSelection();
        tableViewer.getTable().setEnabled(selected);
      }

    });
    data.setFullPageName("Full Page");
    initDataBindings();

  }

  @Override
  public boolean isPageComplete() {
    // All setting names must be unique
    Set<String> set = new HashSet<String>();
    // Set#add returns false if the set does not change, which
    // indicates that a duplicate element has been added.
    for (int i=0; i<data.getSettings().size();i++) { // using loop counter to allow flagging in UI -need to know which row is duplicate
      String name=data.getSettings().get(i).getName();
      if (!set.add(name)) {
        setErrorMessage("Setting names must be unique");
        return false;        
      }
    }

    setErrorMessage(null);
    return true;
  }

  public String getTxtFullDesktopURL() {
    return txtFullPageName.getText();
  }

  public String getTxtFullPageName() {
    return txtFullPageName.getText();
  }

  public boolean isEnableSettings() {
    return btnEnableSettings.getSelection();
  }

  public boolean isEnableFull() {
    return btnEnableFull.getSelection();
  }

  @Override
  public IWizardPage getNextPage() {
    return ((NewIPFProjectWizard)getWizard()).getPageThree();
  }
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeSelectionBtnEnableSettingsObserveWidget = WidgetProperties.selection().observe(btnEnableSettings);
    IObservableValue enableSettingsDataObserveValue = BeanProperties.value("enableSettings").observe(data);
    bindingContext.bindValue(observeSelectionBtnEnableSettingsObserveWidget, enableSettingsDataObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnEnableFullObserveWidget = WidgetProperties.selection().observe(btnEnableFull);
    IObservableValue enableFullDataObserveValue = BeanProperties.value("enableFull").observe(data);
    bindingContext.bindValue(observeSelectionBtnEnableFullObserveWidget, enableFullDataObserveValue, null, null);
    //
    IObservableValue observeTextTxtFullPageNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtFullPageName);
    IObservableValue fullPageNameDataObserveValue = BeanProperties.value("fullPageName").observe(data);
    bindingContext.bindValue(observeTextTxtFullPageNameObserveWidget, fullPageNameDataObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnUseAngularObserveWidget = WidgetProperties.selection().observe(btnUseAngular);
    IObservableValue useAngularDataObserveValue = BeanProperties.value("useAngular").observe(data);
    bindingContext.bindValue(observeSelectionBtnUseAngularObserveWidget, useAngularDataObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnEnableFullObserveWidget_1 = WidgetProperties.selection().observe(btnEnableFull);
    IObservableValue enabledBtnUseAngularObserveValue = PojoProperties.value("enabled").observe(btnUseAngular);
    bindingContext.bindValue(observeSelectionBtnEnableFullObserveWidget_1, enabledBtnUseAngularObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnEnableFullObserveWidget_2 = WidgetProperties.selection().observe(btnEnableFull);
    IObservableValue enabledTxtFullPageNameObserveValue = PojoProperties.value("enabled").observe(txtFullPageName);
    bindingContext.bindValue(observeSelectionBtnEnableFullObserveWidget_2, enabledTxtFullPageNameObserveValue, null, null);
    //
    return bindingContext;
  }
}
