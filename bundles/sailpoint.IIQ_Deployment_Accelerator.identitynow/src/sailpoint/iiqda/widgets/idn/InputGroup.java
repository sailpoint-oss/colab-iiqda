package sailpoint.iiqda.widgets.idn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sailpoint.iiqda.objects.idn.Transform;

public class InputGroup extends Composite implements TransformChangeListener {

  private TransformControl inputControl;
  private Transform xform;
  private List<TransformChangeListener> listeners;
  
  public InputGroup(Composite parent, int style, Transform xform) {
    super(parent, style);
    
    listeners=new ArrayList<TransformChangeListener>();
    
    Label lbl=new Label(this, SWT.NONE);
    lbl.setText("Input");
    
    if (xform!=null) {
      setInput(xform);
    }
    
  }
  
  // wrap this with a new transform
  public void wrap(Transform newXform) {

    inputControl.dispose();
    
    inputControl = new TransformControl(this, SWT.NONE, newXform);
    inputControl.setInput(xform);
    
    this.xform=newXform;
    
    handleTransformChangeEvent(new TransformChangeEvent(TransformChangeEvent.Type.WRAP_INPUT, this));
    
//    TransformControl newControl=new TransformControl(this, SWT.NONE, xform);
//    newXform.setInput(xform);
//    
//    this.xform=newXform;
//    inputControl.setParent(newControl);
//    // was grid?
//    newControl.setLayoutData(inputControl.getLayoutData());
//    inputControl.setLayoutData(null);
//    this.inputControl=newControl;
    
  }

  @Override
  public void handleTransformChangeEvent(TransformChangeEvent source) {
    for (TransformChangeListener lsnr: listeners) {
      lsnr.handleTransformChangeEvent(source);
    }
  }
  
  public void addTransformChangeListener(TransformChangeListener tc) {
    listeners.add(tc);
  }

  public void removeTransformChangeListener(TransformChangeListener tc) {
    listeners.remove(tc);
  }

  public void setInput(Transform newXform) {
    TransformControl tc=new TransformControl(this, SWT.NONE, newXform);
    this.xform=newXform;
    this.inputControl=tc;
    inputControl.addTransformChangeListener(this);
  }
  
  public Transform getInput() {
    return xform;
  }
}
