package sailpoint.iiqda.wizards;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

public abstract class ImportResourceWizardTab extends Composite implements IImportResourceWizardTab {

	public ImportResourceWizardTab(TabFolder parent, int style) {
	  super(parent, style);
  }
	
	protected class CatSelectionListener implements SelectionListener {

		private final IWizardPage page;

		public CatSelectionListener(IWizardPage page) {
			this.page=page;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			page.getWizard().getContainer().updateButtons();        
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			page.getWizard().getContainer().updateButtons();
		}

	}
	
	@Override
	public abstract boolean canFinish();
	@Override
	public abstract void initialize();
	@Override
  public abstract List<ObjectDefinition> getSelectedObjects();
	
	public abstract boolean initializedOK();
}
