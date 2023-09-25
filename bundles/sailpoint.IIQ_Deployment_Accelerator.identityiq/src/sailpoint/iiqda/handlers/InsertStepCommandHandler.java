package sailpoint.iiqda.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.dialogs.StepNameDialog;

@SuppressWarnings("restriction")
public class InsertStepCommandHandler extends BaseStepCommandHandler {

  private static final boolean DEBUG_INSERTSTEP = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/InsertStepHandler"));

  
  @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

	  
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		IWorkbenchPart iwPart=HandlerUtil.getActivePart(event);
		StructuredTextEditor ste=null;
		if(iwPart instanceof StructuredTextEditor) {
		  ste=(StructuredTextEditor)iwPart;
		}
//		StructuredModelManager.getModelManager().getExistingModelForEdit(doc);
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win.getShell();

		IProject project;
		IContainer container;

		//MessageDialog.openInformation(shell, "IIQ Plugin", "Insert Step");

		// get the project that is selected    
		if (selection == null) {
			MessageDialog.openError(
					null,
					"IIQ Plugin",
					"Null Selection!");
			return null;      
		}
		
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			if(strucSelection.size()>1) {
				System.out
				.println("ResourceImportTaskHandler.execute: can't work on multiple objects");
				MessageDialog.openError(
						shell,
						"IIQ Plugin",
						"Can't work on multiple objects!");
				return null;      
			}
			Object firstEl = strucSelection.getFirstElement();
			if (firstEl instanceof Node) {

			  Node element=(Node)firstEl;
			// This is in case we clicked on one of the attributes in the element declaration
	      if(element instanceof AttrImpl) {
	        element=((AttrImpl)element).getOwnerElement();
	      }
	      // now we have the element, we can get the document, and then get the step names to pass to the dialog
	      
	      Document ownerDocument = element.getOwnerDocument();
	      StepNameDialog dlg=new StepNameDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), getStepNames(ownerDocument));
	      int open = dlg.open();
        if(open!=Dialog.OK) return null;

        String newStepName=dlg.getNewStepName();
	      
			  // get the 'name' of the currentStep
			  NamedNodeMap attributes = element.getAttributes();
        Node namedItem = attributes.getNamedItem("name");
        String name=namedItem.getNodeValue();
			  if (DEBUG_INSERTSTEP) {
          IIQPlugin.logDebug("inserting before Step '"+name+"'");
        }
			  			  
			  // find all the <Transition to='currentStep'>
			  // change those to newStep
        changeTransitionSteps(ownerDocument, name, newStepName);
			  // Add <Step name="newStep">
			  //       <Transition to="currentStep"/>
			  //     </Step>
			  Element newStep=addNewStep(element, name, newStepName);
			  
			  if(ste!=null) {
			    ElementImpl iNewStep=(ElementImpl)newStep;
			    // get the location and length of the step from the model
			    IDOMModel model=((DocumentImpl)ownerDocument).getModel();
			    
			    // Add the whitespace before the new step, so that it gets
			    // indented properly			    
			    int start=iNewStep.getStartOffset();
			    if(iNewStep.getPreviousSibling()!=null) {
			      start=((IDOMNode)iNewStep.getPreviousSibling()).getStartOffset();
			    }
			    int end=iNewStep.getEndOffset();
			    ste.getTextViewer().setSelectedRange(start, end-start);
			    ste.getTextViewer().doOperation(StructuredTextViewer.FORMAT_ACTIVE_ELEMENTS);
			  }
			}
		} else {
			MessageDialog.openError(
					shell,
					"IIQ Plugin",
					"Unknown selection type "+selection.getClass().getName());
		}
		return null;
		
	}
}
