package sailpoint.iiqda.wizards.rulelibrary;

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.IIQPlugin;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rule).
 */

public class IIQNewRuleLibraryWizardPage extends WizardPage {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));

  private Text fileText;

	private ISelection selection;

	private IContainer container;
	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	@SuppressWarnings("rawtypes")
  public IIQNewRuleLibraryWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("IdentityIQ Rule Library");
		setDescription("This wizard creates a new Rule Library skeleton, ready for adding utility methods");
		this.selection = selection;

		Iterator iter=((IStructuredSelection)this.selection).iterator();
		IProject p=null;
		
		while(iter.hasNext()&&p==null) {

			Object o=iter.next();
			Class[] interfaces=o.getClass().getInterfaces();
			if(interfaces!=null) {
				for(int i=0;i<interfaces.length;i++) {
					if(interfaces[i].equals(IJavaProject.class)) {
						p=((IJavaProject)o).getProject();
						break;
					}
					if(interfaces[i].equals(IProject.class)) {
						p=(IProject)o;
						break;
					}
				}
			}

		}
		if(p==null) {
//			IStatus status =
//			new Status(IStatus.ERROR, "IIQ_Deployment_Accelerator", IStatus.OK, "No project selected or selection is not project", null);
//			throw new CoreException(status);
		}

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
		
		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
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
		fileText.setText("Library");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		String fileName = getFileName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		
		if( container.exists(new Path(fileName+".roletransform")) ) {
			updateStatus("File already exists");
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getFileName() {
		return fileText.getText();
	}

	public String getContainerName() {
		return container.getFullPath().toString();
	}

}