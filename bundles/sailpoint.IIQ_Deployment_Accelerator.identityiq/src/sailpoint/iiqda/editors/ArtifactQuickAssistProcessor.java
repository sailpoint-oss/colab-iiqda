package sailpoint.iiqda.editors;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

public class ArtifactQuickAssistProcessor implements  IQuickAssistProcessor
{

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public boolean canFix(Annotation annotation) {
    return false;
  }

  @Override
  public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
    return false;
  }

  @Override
  public ICompletionProposal[] computeQuickAssistProposals(
      IQuickAssistInvocationContext invocationContext) {
    return null;
  }

}
