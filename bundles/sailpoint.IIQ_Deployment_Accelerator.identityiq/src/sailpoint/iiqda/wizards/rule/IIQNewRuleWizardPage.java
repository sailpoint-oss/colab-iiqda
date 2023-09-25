package sailpoint.iiqda.wizards.rule;

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.internal.Rule;
import sailpoint.iiqda.internal.RuleModel;
import sailpoint.iiqda.internal.RuleRegistry;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rule).
 */

public class IIQNewRuleWizardPage extends WizardPage {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));

  private Text nameText;

	private ISelection selection;

	private RuleModel model;

	private Combo ruleSelector;
	private Text ruleDescription;
	private IContainer container;
	
	private int previousSelected=-1;


	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	@SuppressWarnings("rawtypes")
  public IIQNewRuleWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("IdentityIQ Rule");
		setDescription("This wizard creates a new file with an IdentityIQ BeanShell snippet and associated configuration");
		this.selection = selection;

		Iterator iter=((IStructuredSelection)this.selection).iterator();
		IProject p=null;
		
		while(iter.hasNext()&&p==null) {

			Object o=iter.next();
			Class<?>[] interfaces=o.getClass().getInterfaces();
			if(interfaces!=null) {
				for(int i=0;i<interfaces.length;i++) {
//					if(interfaces[i].equals(IJavaProject.class)) {
//						p=((IJavaProject)o).getProject();
//						break;
//					}
					if(interfaces[i].equals(IProject.class)) {
						p=(IProject)o;
						break;
					}
					if(interfaces[i].equals(IFolder.class)) {
						p=((IFolder)o).getProject();
						break;
					}
					if(interfaces[i].equals(IJavaProject.class)) {
						p=((IJavaProject)o).getProject();
						break;
					}
				}
			}

		}
		if(p!=null) {
			RuleRegistry reg=IIQPlugin.getRuleRegistry();
			model=reg.getModel();
		}
		if(p==null) {
			new Status(IStatus.ERROR, "sailpoint.IIQ_Deployment_Accelerator", IStatus.OK, "No project selected or selection is not project", null);
//			throw new CoreException(status);
		}
		if(model==null) {
		  if (DEBUG_WIZARDS) IIQPlugin.logDebug("No Rule Registry: No IProject in selection");
		}

	}

	
	
	@Override
  public boolean isPageComplete() {
    if (!(selection instanceof TreeSelection)) {
      setErrorMessage("unknown selection type "+selection.getClass().getName());
      return false;
    }
    TreeSelection treeSel=(TreeSelection)selection;
    Object first=treeSel.getFirstElement();
    if(first instanceof IJavaProject) {
      first=((IJavaProject)first).getProject();
    }
    try {
      IProjectNature nature=((IResource)first).getProject().getNature(IIQPlugin.PLUGIN_ID+".iiqNature");
      if(nature==null) {
        setErrorMessage("Selection is not an IIQ Project");
        return false;
      }
    } catch (CoreException e) {
      setErrorMessage("CoreException determining project nature "+e);
      return false;
    }
    if(nameText.getText().length()==0) {
      setErrorMessage("Rule name must be specified");
      return false;
    }
    setErrorMessage(null);
    return true;
  }



  /**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		ruleSelector = new Combo (container, SWT.DROP_DOWN|SWT.READ_ONLY);
		Iterator<Rule> iter=model.getRules().iterator();
		while(iter.hasNext()) {
			Rule r=iter.next();
			ruleSelector.add(r.getName());
		}
		GridData gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan=3;
		ruleSelector.setLayoutData(gd);
		ruleSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		ruleDescription = new Text (container, SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan=3;
		gd.grabExcessVerticalSpace=true;
		ruleDescription.setLayoutData(gd);


		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		ruleSelector.select(0);
//		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (DEBUG_WIZARDS) IIQPlugin.logDebug("IIQNewRuleWizardPage.initialize: obj="+obj.getClass().getName());
			if (obj instanceof IJavaProject) obj=((IJavaProject)obj).getProject();
			if (obj instanceof IResource ) {
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				//				containerText.setText(container.getFullPath().toString());
			}
		}
		nameText.setText("new_file");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		int selected=ruleSelector.getSelectionIndex();
		if(selected!=-1 && previousSelected!=selected) {
			previousSelected=selected;
			Rule r=(Rule)model.getRules().get(selected);
			String description = r.getDescription();
			if (description==null) description="";
			ruleDescription.setText(description);
			if (DEBUG_WIZARDS) IIQPlugin.logDebug("IIQNewRuleWizardPage.dialogChanged: setting");
			String cc="New "+r.getName();
			if(cc.endsWith(" Template")) {
			  cc=cc.substring(0,cc.length()-9); // String ' Template' off
			}
			nameText.setText(cc);
			
			if (DEBUG_WIZARDS) IIQPlugin.logDebug("IIQNewRuleWizardPage.dialogChanged: set");
		}

		String fileName = getRuleName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getRuleName() {
		return nameText.getText();
	}

	public String getContainerName() {
		return container.getFullPath().toString();
	}

	public Rule getRule() {
		int selected=ruleSelector.getSelectionIndex();
		if(selected==-1) return null;
		Rule r=(Rule)model.getRules().get(selected);
		return r;
	}

}