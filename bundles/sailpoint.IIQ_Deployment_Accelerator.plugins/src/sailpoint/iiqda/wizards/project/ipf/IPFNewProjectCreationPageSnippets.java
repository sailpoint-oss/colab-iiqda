package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.SnippetDefinition;
import sailpoint.iiqda.wizards.project.SnippetDefinition.CSSInclude;
import sailpoint.iiqda.wizards.project.SnippetDefinition.JSInclude;
import sailpoint.iiqda.wizards.project.Snippets;
import sailpoint.iiqda.wizards.project.TextEntryDialog;

public class IPFNewProjectCreationPageSnippets extends WizardPage {

  public static final String pageName="New IPF Project";

  private Text tRegexPattern;
  private Text tRightRequired;
  private Button btnDefinedSnippetRemove;
  private List listScriptIncludes;
  private Button btnScriptAdd;
  private Button btnScriptRemove;
  private List listCSSIncludes;
  private Button btnCSSAdd;
  private Button btnCSSRemove;
  private Button btnAddSnippet;

  private Snippets snippets;
  private ListViewer lvDefinedSnippets;
  private NewIPFProjectWizard wiz;


  protected IPFNewProjectCreationPageSnippets(NewIPFProjectWizard wiz) {
    super("Full Pages");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.snippets"));

    snippets=new Snippets();
    this.wiz=wiz;
  }

  public Snippets getSnippets() {
    return snippets;
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // page is being moved into
      tRightRequired.setText(wiz.getPageOne().getRightName());
    } else {
      // page is being moved away from
      for (String right: snippets.getSPRights()) {
        if (!wiz.getSPRights().contains(right)) wiz.getSPRights().add(right);
      }
    }
    super.setVisible(visible);
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    setControl(container);
    container.setLayout(new GridLayout(1, false));

    Group grpSnippet = new Group(container, SWT.NONE);
    GridData gd_grpSnippet = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_grpSnippet.widthHint = 221;
    grpSnippet.setLayoutData(gd_grpSnippet);
    grpSnippet.setText("Snippet");
    grpSnippet.setLayout(new GridLayout(1, false));

    Composite compositeRegex = new Composite(grpSnippet, SWT.NONE);
    compositeRegex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    compositeRegex.setLayout(new GridLayout(2, false));

    Label lblRegexPattern = new Label(compositeRegex, SWT.NONE);
    lblRegexPattern.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblRegexPattern.setText("Regex pattern");

    tRegexPattern = new Text(compositeRegex, SWT.BORDER);
    tRegexPattern.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

    Composite compositeRight = new Composite(grpSnippet, SWT.NONE);
    compositeRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    compositeRight.setLayout(new GridLayout(2, false));

    Label lblRightRequired = new Label(compositeRight, SWT.NONE);
    lblRightRequired.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblRightRequired.setText("Right required");

    tRightRequired = new Text(compositeRight, SWT.BORDER);
    tRightRequired.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Composite compositeScript = new Composite(grpSnippet, SWT.NONE);
    compositeScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    compositeScript.setLayout(new GridLayout(3, false));

    Label lblScriptIncludes = new Label(compositeScript, SWT.NONE);
    lblScriptIncludes.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
    lblScriptIncludes.setText("Script Includes");

    listScriptIncludes = new List(compositeScript, SWT.BORDER);
    listScriptIncludes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Composite compositeScriptButtons = new Composite(compositeScript, SWT.NONE);
    RowLayout rl_compositeScriptButtons = new RowLayout(SWT.VERTICAL);
    rl_compositeScriptButtons.fill = true;
    compositeScriptButtons.setLayout(rl_compositeScriptButtons);

    btnScriptAdd = new Button(compositeScriptButtons, SWT.NONE);
    btnScriptAdd.setText("Add");
    btnScriptAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        TextEntryDialog ted=new TextEntryDialog(getShell(), "Enter Script path");
        ted.setInfoLabel("Specify script filename relative to {plugin_root}/web/ui/js");
        int open = ted.open();
        if (open==Dialog.OK) {
          listScriptIncludes.add(ted.getTextValue());
        };
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });

    btnScriptRemove = new Button(compositeScriptButtons, SWT.NONE);
    btnScriptRemove.setText("Remove");
    btnScriptRemove.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        int[] selected=listScriptIncludes.getSelectionIndices();
        listScriptIncludes.remove(selected);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });

    Composite compositeCSSIncludes = new Composite(grpSnippet, SWT.NONE);
    compositeCSSIncludes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    compositeCSSIncludes.setLayout(new GridLayout(3, false));

    Label lblCSSIncludes = new Label(compositeCSSIncludes, SWT.NONE);
    lblCSSIncludes.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
    lblCSSIncludes.setText("CSS Includes");

    listCSSIncludes = new List(compositeCSSIncludes, SWT.BORDER);
    listCSSIncludes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    listScriptIncludes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Composite compositeCSSButtons = new Composite(compositeCSSIncludes, SWT.NONE);
    RowLayout rl_compositeCSSButtons = new RowLayout(SWT.VERTICAL);
    rl_compositeCSSButtons.fill = true;
    compositeCSSButtons.setLayout(rl_compositeCSSButtons);

    btnCSSAdd = new Button(compositeCSSButtons, SWT.NONE);
    btnCSSAdd.setText("Add");
    btnCSSAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        TextEntryDialog ted=new TextEntryDialog(getShell(), "Enter CSS path");
        ted.setInfoLabel("Specify css filename relative to {plugin_root}/web/ui/css");
        int open = ted.open();
        if (open==Dialog.OK) {
          listCSSIncludes.add(ted.getTextValue());
        };
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });
    btnCSSRemove = new Button(compositeCSSButtons, SWT.NONE);
    btnCSSRemove.setText("Remove");
    btnCSSRemove.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        int[] selected=listCSSIncludes.getSelectionIndices();
        listCSSIncludes.remove(selected);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

    });

    btnAddSnippet = new Button(grpSnippet, SWT.NONE);
    btnAddSnippet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    btnAddSnippet.setText("Add Snippet");
    btnAddSnippet.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        SnippetDefinition sd=new SnippetDefinition();
        sd.setRegex(tRegexPattern.getText());
        sd.setRightRequired(tRightRequired.getText());
        sd.setScriptIncludes(toJSIncludeList(listScriptIncludes.getItems()));
        sd.setCssIncludes(toCSSIncludeList(listCSSIncludes.getItems()));

        snippets.addSnippet(sd);

        tRegexPattern.setText("");
        //        btnDesktop.setSelection(false);
        //        btnMobile.setSelection(false);
        tRightRequired.setText(wiz.getPageOne().getRightName());
        listScriptIncludes.removeAll();
        listCSSIncludes.removeAll();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });

    Composite composite = new Composite(container, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    Group grpDefinedSnippets = new Group(composite, SWT.NONE);
    grpDefinedSnippets.setLayout(new GridLayout(1, false));
    grpDefinedSnippets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    grpDefinedSnippets.setText("Defined Snippets");

    lvDefinedSnippets = new ListViewer(grpDefinedSnippets, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
    List listDefinedSnippets = lvDefinedSnippets.getList();
    listDefinedSnippets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    btnDefinedSnippetRemove = new Button(composite, SWT.NONE);
    btnDefinedSnippetRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=lvDefinedSnippets.getList().getSelection();
        for (String selection: selected) {
          snippets.removeSnippetDefinitionByPattern(selection);
        }
      }
    });
    btnDefinedSnippetRemove.setText("Remove");

    ModifyListener modLsnr=new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        validateFields();
      }
    };
    tRegexPattern.addModifyListener(modLsnr);
    initDataBindings();

  }

  private void validateFields() {

    String sRegex = tRegexPattern.getText();
    boolean canAdd=sRegex.length()>0;
    if (!canAdd) {
      setErrorMessage("Regex pattern must be specified");
      btnAddSnippet.setEnabled(false);
      return;
    }

    for (SnippetDefinition snippet: snippets) {
      boolean exists=snippet.getRegex().equals(sRegex);
      if (exists) {
        setErrorMessage("Regex pattern must be unique");
        btnAddSnippet.setEnabled(false);
        return;
      }
    }

    btnAddSnippet.setEnabled(true);
    setErrorMessage(null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = BeansObservables.observeMap(listContentProvider.getKnownElements(), SnippetDefinition.class, "regex");
    lvDefinedSnippets.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    lvDefinedSnippets.setContentProvider(listContentProvider);
    //
    IObservableList snippetsSnippetsObserveList = BeanProperties.list("snippets").observe(snippets);
    lvDefinedSnippets.setInput(snippetsSnippetsObserveList);
    //
    return bindingContext;
  }
  
  private ArrayList<JSInclude> toJSIncludeList(String[] items) {
    
    ArrayList<JSInclude> inc=new ArrayList<JSInclude>();
    for (String item: items) {
      inc.add(new JSInclude("jsTemplate.js", item));
    }
    return inc;
  }

  private ArrayList<CSSInclude> toCSSIncludeList(String[] items) {
    
    ArrayList<CSSInclude> inc=new ArrayList<CSSInclude>();
    for (String item: items) {
      inc.add(new CSSInclude("cssTemplate.css", item));
    }
    return inc;
  }
  
}
