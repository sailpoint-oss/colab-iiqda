package sailpoint.iiqda.wizards;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.widgets.idn.TransformTypeComboViewer;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rule).
 */

public class IDNNewTransformWizardPage extends WizardPage {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/Wizards"));

  private String transformName;
  private Transform.Type type;

  
  private Text nameText;
  
	private ISelection selection;

	private TransformTypeComboViewer comboType;
	private IContainer container;
	
	private int previousSelected=-1;


	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	@SuppressWarnings("rawtypes")
  public IDNNewTransformWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("IdentityNow Transform");
		setDescription("This wizard creates a new IdentityNow Transform file");
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
				}
			}

		}
		if(p==null) {
			new Status(IStatus.ERROR, "sailpoint.iiqda", IStatus.OK, "No project selected or selection is not project", null);
//			throw new CoreException(status);
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
    try {
      IProjectNature nature=((IResource)first).getProject().getNature(IDNPlugin.PLUGIN_ID+".idnNature");
      if(nature==null) {
        setErrorMessage("Selection is not an IdentityNow Project");
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

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
		  public void modifyText(ModifyEvent e) {
		    dialogChanged();
		  }
		});

		comboType = new TransformTypeComboViewer(container);
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan=3;
		comboType.setLayoutData(gd);
		
		initialize();
		comboType.select(0);
		type=comboType.getType();		
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
			if (DEBUG_WIZARDS) IDNPlugin.logDebug("IIQNewTransformWizardPage.initialize: obj="+obj.getClass().getName());
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

		transformName = nameText.getText();


		if (transformName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (transformName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
	
		updateStatus(null);
	}
	
	public String getTransformName() {
	  return transformName;
	}

	public Transform.Type getType() {
	  return comboType.getType();
	}
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return container.getFullPath().toString();
	}

	
	
}