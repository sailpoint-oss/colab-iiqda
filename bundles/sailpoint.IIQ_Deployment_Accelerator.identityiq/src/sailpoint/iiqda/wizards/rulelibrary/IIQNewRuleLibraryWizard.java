package sailpoint.iiqda.wizards.rulelibrary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.internal.CommentsBlock;

public class IIQNewRuleLibraryWizard extends Wizard implements INewWizard {
  private IIQNewRuleLibraryWizardPage page;
  private ISelection selection;

  public IIQNewRuleLibraryWizard() {
    super();
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */

  public void addPages() {
    page = new IIQNewRuleLibraryWizardPage(selection);		
    addPage(page);
  }

  /**
   * This method is called when 'Finish' button is pressed in
   * the wizard. We will create an operation and run it
   * using wizard as execution context.
   */
  public boolean performFinish() {
    final String fileName = page.getFileName();
    //		final Rule rule=page.getRule();
    final String container=page.getContainerName();
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(container, fileName, monitor);
        } catch (CoreException e) {
          IIQPlugin.logException("NewRoleImportTransformWizard.performFinish CoreException", e);
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  /**
   * The worker method. It will find the container, create the
   * file if missing or just replace its contents, and open
   * the editor on the newly created file.
   */

  private void doFinish(
      String containerName,
      String fileName,
      IProgressMonitor monitor)
          throws CoreException {
    // create a sample file
    monitor.beginTask("Creating " + fileName, 2);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = root.findMember(new Path(containerName));
    if (!resource.exists() || !(resource instanceof IContainer)) {
      throwCoreException("Container \"" + containerName + "\" does not exist.");
    }
    IContainer container = (IContainer) resource;
    final IFile file = container.getFile(new Path("RuleLibrary-"+CoreUtils.toCamelCase(fileName)+".xml"));

    try {
      InputStream stream = openContentStream(fileName);
      if (file.exists()) {
        file.setContents(stream, true, true, monitor);
      } else {
        file.create(stream, true, monitor);
      }
      stream.close();
    } catch (IOException e) {
    }
    monitor.worked(1);
    monitor.setTaskName("Opening file for editing...");
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage page =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          IDE.openEditor(page, file, true);
        } catch (PartInitException e) {
        }
      }
    });
    monitor.worked(1);
  }

  private InputStream openContentStream(String fileName) {

    CommentsBlock cb=new CommentsBlock();

    // Generate the initial file contents
    cb.addComment("  Rule Library");
    cb.addComment("");	
    cb.addComment("  A rule library is a collection of utility methods that can be called from other");
    cb.addComment("  Rules or workflows.");
    cb.addComment("");	
    cb.addComment("  To include a library in another Rule, use this format:");
    cb.addComment("    <Rule ......>");
    cb.addComment("      <ReferencedRules>");
    cb.addComment("        <Reference class=\"sailpoint.object.Rule\" name=\"I18n Library\"/>");
    cb.addComment("      </ReferencedRules>");
    cb.addComment("      ..");
    cb.addComment("      ..");
    cb.addComment("    </Rule>");
    cb.addComment("");    
    cb.addComment("  To include a library in a Workflow, use this format:");
    cb.addComment("");    
    cb.addComment("    <Workflow ......>");
    cb.addComment("      <RuleLibraries>");
    cb.addComment("        <Reference class=\"sailpoint.object.Rule\" name=\"Approval Library\" />");
    cb.addComment("      </RuleLibraries>");
    cb.addComment("      ..");
    cb.addComment("      ..");
    cb.addComment("    </Workflow>");

    StringBuilder sb=new StringBuilder();
    sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<!DOCTYPE Rule PUBLIC 'sailpoint.dtd' 'sailpoint.dtd'>\n");
    sb.append("<Rule language='beanshell' name='"+fileName+"'>\n");
    sb.append("  <Source><![CDATA[");
    sb.append(cb.toString());
    sb.append("  ]]></Source>");
    sb.append("</Rule>");

    return new ByteArrayInputStream(sb.toString().getBytes());
  }

  private void throwCoreException(String message) throws CoreException {
    IStatus status =
        new Status(IStatus.ERROR, "sailpoint.IIQ_Deployment_Accelerator", IStatus.OK, message, null);
    throw new CoreException(status);
  }

  /**
   * We will accept the selection in the workbench to see if
   * we can initialize from it.
   * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.selection = selection;
  }
}
