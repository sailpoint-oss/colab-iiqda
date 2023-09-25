package sailpoint.iiqda.wizards;

import java.util.List;

public interface IImportResourceWizardTab {

	public boolean canFinish();
	public void initialize();
	public List<ObjectDefinition> getSelectedObjects();
	
}
