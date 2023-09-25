package sailpoint.iiqda.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.dialogs.StepNameDialog;

@SuppressWarnings("restriction")
public class BaseStepCommandHandler extends AbstractHandler {

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


  protected Element addNewStep(Node element, String name,
      String newStepName) {
    // NOTE: When we insert this step, if there is a text element before 'element',
    // we need to insert before that (actually, we need to split the text node at the last '\n',
    // then insert after that but before the last bit of whitespace)
    // to keep the formatting of the existing step.
    
    Document document=element.getOwnerDocument();
    System.out.println("InsertStepCommandHandler.addNewStep:");
    Element to=document.createElement("Transition");
    to.setAttribute("to", name);
    Element step=document.createElement("Step");
    step.setAttribute("icon", "Default");
    step.setAttribute("name", newStepName);
    step.appendChild(document.createTextNode("\n"));
    step.appendChild(to);
    step.appendChild(document.createTextNode("\n"));
    
    Node before=element;
    
    Node whitespace=element.getPreviousSibling();
    String originalIndent="";
    if(whitespace!=null && whitespace instanceof Text) {
      Text tWhitespace = (Text)whitespace;
      String sWhitespace=tWhitespace.getTextContent();
      int lastCR=sWhitespace.lastIndexOf('\n');
      if(lastCR!=-1) {
        int startOfIndent=lastCR;
        // adjust if end of whitespace isn't CR (i.e. there is an actual indent
        if( lastCR!=(sWhitespace.length()-1) ) startOfIndent++;
        //tWhitespace.setData(sWhitespace.substring(0, startOfIndent));
        originalIndent=sWhitespace.substring(startOfIndent);
      }
    }
    
    before.getParentNode().insertBefore(step, before);
    before.getParentNode().insertBefore(document.createTextNode("\r\n"+originalIndent), before);
    
    return step;
  }


  protected void changeTransitionSteps(Document document, String name,
      String newStepName) {
    // TODO Auto-generated method stub
    System.out.println("InsertStepCommandHandler.changeTransitionSteps:");
    
    
    NodeList nl=document.getElementsByTagName("Transition");
    
    if (nl!=null) {
      for (int i=0;i<nl.getLength();i++) {
        Node n=nl.item(i);
        NamedNodeMap attributes = n.getAttributes();
        Node namedItem = attributes.getNamedItem("to");
        if (namedItem!=null) {
          String transTo=namedItem.getNodeValue();
          if (DEBUG_INSERTSTEP) {
            IIQPlugin.logDebug("Transition to='"+transTo+"'");
          }
          if(transTo.equals(name)) {
            AttrImpl ai=(AttrImpl)namedItem;
            ai.setValue(newStepName);
          }
        }
      }
    }
  }

  protected void renameStep(Document document, String name,
      String newStepName) {
    // TODO Auto-generated method stub
    System.out.println("BaseStepCommandHandler.renameStep:");
    
    
    NodeList nl=document.getElementsByTagName("Step");
    
    if (nl!=null) {
      for (int i=0;i<nl.getLength();i++) {
        Node n=nl.item(i);
        NamedNodeMap attributes = n.getAttributes();
        Node namedItem = attributes.getNamedItem("name");
        if (namedItem!=null) {
          String stepName=namedItem.getNodeValue();
          if (DEBUG_INSERTSTEP) {
            IIQPlugin.logDebug("Step name='"+stepName+"'");
          }
          if(stepName.equals(name)) {
            AttrImpl ai=(AttrImpl)namedItem;
            ai.setValue(newStepName);
            return; // There will only be one step with the right name
          }
        }
      }
    }
  }
  
  protected List<String> getStepNames(Document document) {
    
    List<String> names=new ArrayList<String>();
    NodeList nl=document.getElementsByTagName("Step");
    
    if (nl!=null) {
      for (int i=0;i<nl.getLength();i++) {
        Node n=nl.item(i);
        NamedNodeMap attributes = n.getAttributes();
        Node namedItem = attributes.getNamedItem("name");
        if (namedItem!=null) {
          String stepName=namedItem.getNodeValue();
          if (DEBUG_INSERTSTEP) {
            IIQPlugin.logDebug("Found Step: "+stepName);
          }
          names.add(stepName);
        }
      }
    }
    return names;
  }

}
