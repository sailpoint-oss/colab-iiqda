package sailpoint.iiqda.wizards.rule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.internal.Argument;
import sailpoint.iiqda.internal.CommentsBlock;
import sailpoint.iiqda.internal.Rule;
import sailpoint.iiqda.internal.RuleModel;
import sailpoint.iiqda.internal.RuleRegistry;

public class IIQNewRuleWizard extends Wizard implements INewWizard {
  private IIQNewRuleWizardPage page;
  private ISelection selection;

  public IIQNewRuleWizard() {
    super();
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */

  public void addPages() {
    page = new IIQNewRuleWizardPage(selection);		
    addPage(page);
  } 
  
  /**
   * This method is called when 'Finish' button is pressed in
   * the wizard. We will create an operation and run it
   * using wizard as execution context.
   */
  public boolean performFinish() {
    final String fileName = page.getRuleName();
    final Rule rule=page.getRule();
    final String container=page.getContainerName();
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(rule, container, fileName, monitor);
        } catch (CoreException e) {
          IIQPlugin.logException("NewRuleWizard.performFinish CoreException", e);
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
      Rule ruleTemplate,
      String containerName,
      String ruleName,
      IProgressMonitor monitor)
          throws CoreException {
    // create a sample file
    monitor.beginTask("Creating " + ruleName, 2);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = root.findMember(new Path(containerName));
    if (!resource.exists() || !(resource instanceof IContainer)) {
      throwCoreException("Container \"" + containerName + "\" does not exist.");
    }
    IContainer container = (IContainer) resource;
    final IFile file = container.getFile(new Path("Rule-"+CoreUtils.toCamelCase(ruleName)+".xml"));

    try {
      InputStream stream = generateSource(ruleName, ruleTemplate);			 
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

  private String getContent(Rule rule, String fileNamePart) {

  	// fileNamePart is the camelcased
  	
    CommentsBlock cb=new CommentsBlock();

    // Generate the initial file contents
    cb.addComment("");
    cb.addComment("Generated: "+new Date());
    cb.addComment("Rule: "+rule.getName());
    cb.addComment("Description: "+rule.getName().trim());
    cb.addComment("Inputs:");
    List<Argument> in=rule.getSignature().getInputs();
    Iterator<Argument> iter=in.iterator();
    while(iter.hasNext()) {
      Argument arg=iter.next();
      cb.addComment("    "+arg.getName()+" - "+arg.getDescription().trim());
    }
    cb.addComment("Returns:");
    List<Argument> ret=rule.getSignature().getReturns();
    iter=ret.iterator();
    while(iter.hasNext()) {
      Argument arg=iter.next();
      cb.addComment("    "+arg.getName()+" - "+arg.getDescription().trim());
    }
    
    StringBuilder contents=new StringBuilder(cb.toString());
    addSERILog(contents, CoreUtils.toCamelCase(rule.getName()) );

    return contents.toString();
  }

  private void addSERILog(StringBuilder contents, String name) {
	  
  	contents.append("serilog=org.apache.commons.logging.LogFactory.getLog(\"SERI.Rule.");
  	contents.append(name);
  	contents.append("\");\n");
  	contents.append("serilog.debug(\"----Rule Start----\");\n");

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

  private InputStream generateSource(String ruleName, Rule rule) throws CoreException {
    StringBuffer buf=new StringBuffer();
    buf.append("<?xml version='1.0' encoding='UTF-8'?>\n");
    buf.append("<!DOCTYPE sailpoint PUBLIC \"sailpoint.dtd\" \"sailpoint.dtd\">\n");

    String language=rule.getLanguage();
    String type=rule.getType();
    String description=rule.getDescription().trim();

    RuleRegistry reg=IIQPlugin.getRuleRegistry();
    RuleModel model=reg.getModel();
    buf.append("<sailpoint>");
    buf.append("<Rule language=\"");
    buf.append(language==null?"":language);
    buf.append("\" name=\"");
    buf.append(ruleName==null?"":ruleName);
    buf.append("\" type=\"");
    buf.append(type==null?"":type);
    buf.append("\">\n");

    buf.append("  <Description>");
    buf.append(description==null?"":description.trim());
    buf.append("</Description>\n");

    Rule rul=model.getRuleByType(type);
    if(rul!=null) buf.append(rule.getSignature().toXML());

    buf.append("<!--ReferencedRules>\n");
    buf.append("  <Reference class=\"sailpoint.object.Rule\" name=\"\"/>\n");
    buf.append("</ReferencedRules-->\n");

    buf.append("  <Source><![CDATA[\n");
    buf.append(getContent(rule, CoreUtils.toCamelCase(ruleName)));
    buf.append("  ]]></Source>\n");
    buf.append("</Rule>\n");
    buf.append("</sailpoint>");

    return new ByteArrayInputStream(buf.toString().getBytes());

  }


}