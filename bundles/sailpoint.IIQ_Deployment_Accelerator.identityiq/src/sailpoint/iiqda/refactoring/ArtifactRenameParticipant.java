package sailpoint.iiqda.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class ArtifactRenameParticipant extends RenameParticipant {

	private IFile element;
	
	public ArtifactRenameParticipant() {
  }
	
	@Override
	protected boolean initialize(Object element) {
		// The entry in plugin.xml guarantees the element is an IFIle
		this.element=(IFile)element;
		return true;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		//TODO: IConditionChecker checker=context.getChecker(element.getClass());
		return new RefactoringStatus();
	}

	@Override
	public Change createPreChange(IProgressMonitor pm) throws CoreException,
	OperationCanceledException {
		ArtifactNameChange changer=new ArtifactNameChange(element, getArguments());
		return changer;
	}

	@Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
	  return null;
  }

}
