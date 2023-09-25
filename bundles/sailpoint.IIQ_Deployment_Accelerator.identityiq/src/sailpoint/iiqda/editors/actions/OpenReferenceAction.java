package sailpoint.iiqda.editors.actions;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.IIQPlugin.SimpleElement;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.declaration.DeclarationElement;
import sailpoint.iiqda.declaration.ReferenceDeclaration;
import sailpoint.iiqda.declaration.StepDeclaration;
import sailpoint.iiqda.validation.ArtifactFinderVisitor;

public class OpenReferenceAction extends TextEditorAction {

	private static final boolean DEBUG_ACTION = "true".equalsIgnoreCase(Platform
			.getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/OpenReferenceAction"));

	public OpenReferenceAction(ResourceBundle bundle, String string, ITextEditor artifactEditor,
			ISourceViewer sourceViewer) {

		super(bundle, string, artifactEditor);
		// this.fSourceViewer=sourceViewer;
	}

	@Override
	public void run() {
		//super.run();
		// Find what element the pointer is on
		StyledText s = (StyledText) getTextEditor().getAdapter(Control.class);
		int caretOffset = s.getCaretOffset();
		if(DEBUG_ACTION) {
			IIQPlugin.logDebug("caret="+caretOffset);		  
		}
		String content=getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput()).get();
		DeclarationElement de=IIQPlugin.getElementAt(content, caretOffset);
		if(de instanceof ReferenceDeclaration) {
			// If it's a <Reference>, find what object it's referring to
			String artifactName=((ReferenceDeclaration)de).getReference();
			if(artifactName!=null) {
				// Look through the project. If we find it, open an editor on it
				try {
					ArtifactFinderVisitor afv=new ArtifactFinderVisitor(artifactName);
					getProject(getTextEditor()).accept(afv);
					if (afv.getFile()!=null) {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						page.openEditor(new FileEditorInput(afv.getFile()), "sailpoint.IIQ_Deployment_Accelerator.content.Artifact.ArtifactEditor");
					}
				} catch (CoreException e) {
					IIQPlugin.logException("CoreException ", e);
				}
			}
		} else if (de instanceof StepDeclaration) {
			String stepName=((StepDeclaration)de).getStep();
			List<SimpleElement> elements=IIQPlugin.simpleParse(content);
			for (SimpleElement element: elements) {
				if( "Step".equals(element.getName())
				 && ((StepDeclaration)de).getStep().equals(element.getAttribute("name")))
				{
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					getTextEditor().selectAndReveal(element.getOffset(), 0);
					break;
				}
			}
		}
	}

	private IProject getProject(ITextEditor textEditor) throws CoreException {
		IEditorInput input= textEditor.getEditorInput(); 
		IFile file = ((FileEditorInput) input).getFile(); 
		if(file==null) {
			CoreUtils.throwCE("No file found for text editor content");
		}
		return file.getProject();
	}


}
