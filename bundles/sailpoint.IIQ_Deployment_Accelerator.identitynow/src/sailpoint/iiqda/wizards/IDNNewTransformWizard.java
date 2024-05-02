package sailpoint.iiqda.wizards;

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

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.objects.idn.Transform;

public class IDNNewTransformWizard extends Wizard implements INewWizard {
  private IDNNewTransformWizardPage page;
  private ISelection selection;

  public IDNNewTransformWizard() {
    super();
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */

  public void addPages() {
    page = new IDNNewTransformWizardPage(selection);		
    addPage(page);
  } 
  
  /**
   * This method is called when 'Finish' button is pressed in
   * the wizard. We will create an operation and run it
   * using wizard as execution context.
   */
  public boolean performFinish() {
    final String transformName=page.getTransformName();
    final Transform.Type type=page.getType();
    final String container=page.getContainerName();
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(type, container, transformName, monitor);
        } catch (CoreException e) {
          IDNPlugin.logException("NewRuleWizard.performFinish CoreException", e);
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
      Transform.Type type,
      String containerName,
      String transformName,
      IProgressMonitor monitor)
          throws CoreException {
    // create a sample file
    monitor.beginTask("Creating " + transformName, 2);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = root.findMember(new Path(containerName));
    if (!resource.exists() || !(resource instanceof IContainer)) {
      throwCoreException("Container \"" + containerName + "\" does not exist.");
    }
    IContainer container = (IContainer) resource;
    final IFile file = container.getFile(new Path(CoreUtils.toCamelCase(transformName)+".transform"));

    try {
      InputStream stream = generateSource(transformName, type);			 
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

	private void throwCoreException(String message) throws CoreException {
    IStatus status = CoreUtils.toErrorStatus(message);
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

  private InputStream generateSource(String transformName, Transform.Type type) throws CoreException {
    
    Transform xform=new Transform(type);
    
    xform.setId(transformName);

    return new ByteArrayInputStream(IDNHelper.transformToJSON(xform).getBytes());

  }


}