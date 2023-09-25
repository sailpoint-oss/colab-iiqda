package sailpoint.iiqda.widgets.idn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.wizards.importresource.idn.IDNObject;

public class NameValueListViewer extends ListViewer implements SelectionListener {

	private String methodName = "getId";
	
  public List<IDNObject> selection;
  
  public NameValueListViewer(Composite parent, String nameKey) {
    super(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    setLabelType(nameKey);
    setContentProvider(ArrayContentProvider.getInstance());
    setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
      	try {
					Method method=element.getClass().getMethod(methodName);
					return (String) method.invoke(element);
				} catch (NoSuchMethodException e) {
					return "Class "+element.getClass().getName()+" has no method "+methodName;
				} catch (SecurityException e) {
					return "Security Exception on "+element.getClass().getName()+" "+methodName;
				} catch (IllegalAccessException e) {
					return "Illegal Access on "+element.getClass().getName()+" "+methodName;
				} catch (IllegalArgumentException e) {
					return "Illegal Argument on "+element.getClass().getName()+" "+methodName;
				} catch (InvocationTargetException e) {
					return "Invocation Target on "+element.getClass().getName()+" "+methodName;
				}
      	
      }
    });
    getList().addSelectionListener(this);
  }
  
  public void removeAll() {
  	getList().removeAll();
  }

  public void setLabelType(String nameKey) {
    if (nameKey==null) {
    	nameKey="id";
    }
    methodName="get"+CoreUtils.capitalize(nameKey);

	}



	public List<IDNObject> getSelectedObjects() {
    System.out.println("TransformTypeComboViewer.getType: "+selection);

    return selection;
  }

  public void setLayoutData(Object gd) {
    getList().setLayoutData(gd);
  }

  public void select(int i) {
    getList().select(i);
    widgetDefaultSelected(null); // keep the selection logic in one place
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent arg0) {
  
    System.out.println("TransformTypeComboViewer.selectionChanged: ");
    IStructuredSelection selection2 = (IStructuredSelection)this.getSelection();
    selection=selection2.toList();
    System.out.println("TransformTypeComboViewer.selectionChanged: "+selection);
    
    
  }

  @Override
  public void widgetSelected(SelectionEvent arg0) {
    widgetDefaultSelected(arg0);
  }

  
}
