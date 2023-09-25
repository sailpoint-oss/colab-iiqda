package sailpoint.iiqda.editors;

import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.actions.ActionDefinitionIds;
import org.eclipse.wst.sse.ui.internal.actions.StructuredTextEditorActionConstants;

import sailpoint.iiqda.editors.actions.OpenReferenceAction;
import sailpoint.iiqda.resources.IIQDAUIMessages;

@SuppressWarnings("restriction")
public class ArtifactEditor extends StructuredTextEditor {  

  private static final String UNDERSCORE = "_";

	public ArtifactEditor() {
    super();
    //    getSite().registerContextMenu("ArtifactEditorContext", menuManager, selectionProvider);
  }

  protected void initializeEditor() {
    super.initializeEditor();
    setEditorContextMenuId("#ArtifactEditorContext"); //$NON-NLS-1$
    setRulerContextMenuId("#ArtifactRulerContext"); //$NON-NLS-1$
    //    setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
    //    configureInsertMode(SMART_INSERT, false);
    //    setInsertMode(INSERT);
    setSourceViewerConfiguration(new ArtifactSourceViewerConfiguration(this));
  }

  @Override
  protected void initSourceViewer(StructuredTextViewer sourceViewer) {
    // TODO Auto-generated method stub
    super.initSourceViewer(sourceViewer);
    TextViewer tv = (TextViewer)sourceViewer;
    if(tv!=null) {
      tv.setDocumentPartitioning("abc");
    }
  }

  @Override
  protected void createActions() {
	  super.createActions();
	  ResourceBundle resourceBundle = IIQDAUIMessages.getResourceBundle();
	  
		// StructuredTextViewer Action - open file on selection
		Action action = new OpenReferenceAction(resourceBundle, StructuredTextEditorActionConstants.ACTION_NAME_OPEN_FILE + UNDERSCORE, this, getSourceViewer());
		action.setActionDefinitionId(ActionDefinitionIds.OPEN_FILE);
		setAction(StructuredTextEditorActionConstants.ACTION_NAME_OPEN_FILE, action);

  }  

}