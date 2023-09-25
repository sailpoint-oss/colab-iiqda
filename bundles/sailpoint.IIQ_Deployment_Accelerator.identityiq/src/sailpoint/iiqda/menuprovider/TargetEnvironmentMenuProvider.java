package sailpoint.iiqda.menuprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;

public abstract class TargetEnvironmentMenuProvider extends CompoundContributionItem implements IWorkbenchContribution{

  protected String commandId;

  private IServiceLocator serviceLocator;

  @Override
  protected IContributionItem[] getContributionItems() {
    ISelectionService selectionService = (ISelectionService) serviceLocator
        .getService(ISelectionService.class);
    List<IContributionItem> menuItems=new ArrayList<IContributionItem>();
    if (selectionService != null) {
      ISelection selection = selectionService.getSelection();
      if (selection != null && selection instanceof IStructuredSelection) {
        // get the first item in the selection. Menu contribution logic should have precluded
        // getting here if it's not a bunch of files 
        if (selection instanceof IStructuredSelection) {

          Object element=null;
          IStructuredSelection strucSel=(IStructuredSelection)selection;
          if(strucSel instanceof TextSelection) {
            // Selection was from active editor; get the related file
            element=CoreUtils.getActiveWorkbenchFile();					  
          } else {
            // it's some kind of file selection
            element=strucSel.getFirstElement();
          }
          IProject prj=null;
          if(element instanceof IFile) {
            prj=((IFile)element).getProject();
          } else if (element instanceof IFolder) {
            prj=((IFolder)element).getProject();
          } else if (element instanceof IJavaProject) {
            prj=((IJavaProject)element).getProject();
          } else if (element instanceof IProject) {
          	prj=(IProject)element;
          } else if (element instanceof ICompilationUnit) {
            prj=((ICompilationUnit)element).getJavaProject().getProject();
          }
          if(prj!=null) {
            // Get all the files with the TARGET_SUFFIX suffix
            List<String>environments=IIQPlugin.getTargetEnvironments(prj);
            for (String target: environments) {
              final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator,
                  null, commandId,  
                  CommandContributionItem.STYLE_PUSH);  

              contributionParameter.label = "target: "+target;  
              //           contributionParameter.visibleEnabled = true;
              contributionParameter.parameters=new HashMap<String,String>();
              contributionParameter.parameters.put(IIQPlugin.PLUGIN_ID+".commands.targetEnvironment", target);

              CommandContributionItem e = new CommandContributionItem(contributionParameter);
              e.setVisible(true);
              menuItems.add(e);
            }
          }
        }
      }
    }
    if(menuItems.size()==0) {
      IAction a = new Action("No targets available") {}; 
      a.setEnabled(false); 
      IContributionItem item = new ActionContributionItem(a); 

      menuItems.add(item);      
    }
    return menuItems.toArray(new IContributionItem[menuItems.size()]);
  }

  @Override
  public void initialize(IServiceLocator serviceLocator) {
    this.serviceLocator=serviceLocator;
  }
}
