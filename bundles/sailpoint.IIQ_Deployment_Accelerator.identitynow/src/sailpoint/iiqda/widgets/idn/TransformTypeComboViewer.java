package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.objects.idn.Transform.Type;

public class TransformTypeComboViewer extends ComboViewer implements SelectionListener {

  public Transform.Type selection;
  
  public TransformTypeComboViewer(Composite parent) {
    super(parent, SWT.DROP_DOWN|SWT.READ_ONLY);
    setContentProvider(ArrayContentProvider.getInstance());
    setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
          if (element instanceof Transform.Type) {
              Transform.Type xformType = (Transform.Type) element;
              return xformType.getDescription();
          }
          return super.getText(element);
      }
    });
    setInput(Transform.Type.valuesNoUnknown());
    getCombo().addSelectionListener(this);
  }

  public Type getType() {
    System.out.println("TransformTypeComboViewer.getType: "+selection);

    return selection;
  }

  public void setLayoutData(Object gd) {
    getCombo().setLayoutData(gd);
  }

  public void select(int i) {
    getCombo().select(i);
    widgetDefaultSelected(null); // keep the selection logic in one place
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent arg0) {
  
    System.out.println("TransformTypeComboViewer.selectionChanged: ");
    selection = (Transform.Type)((IStructuredSelection)this.getSelection()).getFirstElement();
    System.out.println("TransformTypeComboViewer.selectionChanged: "+selection);
    
    
  }

  @Override
  public void widgetSelected(SelectionEvent arg0) {
    widgetDefaultSelected(arg0);
  }

}
