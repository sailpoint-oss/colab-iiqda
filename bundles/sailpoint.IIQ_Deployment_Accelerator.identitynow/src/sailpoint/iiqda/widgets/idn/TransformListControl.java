package sailpoint.iiqda.widgets.idn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import sailpoint.iiqda.objects.idn.Transform;

public class TransformListControl extends Composite implements Rebuildable, TransformChangeListener {

  List<TransformChangeListener> listeners;
  private List<TransformControl> controls;
  private List<Transform> transforms;
  private final Label placeholder;
  
  public TransformListControl(Composite parent, int style, List<Transform> transforms) {
    super(parent, style);

    this.transforms=transforms;
    if (this.transforms==null) {
      this.transforms=new ArrayList<Transform>();
    }
    listeners=new ArrayList<TransformChangeListener>();
    controls=new ArrayList<TransformControl>();

    
    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns=1;
    gridLayout.verticalSpacing=20;
    gridLayout.marginBottom=10;
    setLayout(gridLayout);
    
    DropTarget target=new DropTarget(this, DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    target.addDropListener(new TransformDropTargetListener(this, target));
//    addDropListener(this);
    
    placeholder = new Label(this, SWT.NONE);
    placeholder.setText("Click here to add a Transform..");
    placeholder.setLayoutData(new GridData());
    placeholder.addListener(SWT.MouseDown, new Listener() {

      @Override
      public void handleEvent(Event arg0) {
        Transform xform=getNewTransform();
        if (xform!=null) {
          hidePlaceholder();
          addTransform(xform);
          TransformListControl.this.transforms.add(xform);
        }
        
      }
      
    });

    if (transforms!=null) {
      for (Transform transform: transforms) {
        addTransform(transform);
      }
    } else {
      showPlaceholder();
    }
  }

  private TransformControl addTransform(Transform t) {
    final TransformControl tc=new TransformControl(this, SWT.NONE, t);
    tc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    tc.addTransformChangeListener(this);

    tc.addInsertionMenuItems(this);
    
    DragSource source=new DragSource(tc.getGroup(), DND.DROP_MOVE);
    source.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    source.addDragListener(new TransformDragSourceListener(this, source));
    
    hidePlaceholder();
//  System.out.println("making tc a drop target");
//  DropTarget target=new DropTarget(tc, DND.DROP_MOVE);
//  target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
//  target.addDropListener(new TransformDropTargetListener(this, target));

//      // Step 1: Get JFace's LocalSelectionTransfer instance
//      final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
//
//      final DragSourceAdapter dragAdapter = new DragSourceAdapter()
//      {
//        @Override public void dragSetData(final DragSourceEvent event)
//        {
//          System.out.println(
//              "TransformListControl.addTransform(...).new DragSourceAdapter() {...}.dragSetData: ");
//// Step 2: On drag events, create a new JFace StructuredSelection
//          // with the dragged control.
//          transfer.setSelection(new StructuredSelection(tc));
//        }
//      };
//
//      final DragSource dragSource = new DragSource(tc.getGroup(), DND.DROP_MOVE);
//      dragSource.setTransfer(new Transfer[] { transfer });
//      dragSource.addDragListener(dragAdapter);
      
    
    controls.add(tc);
    fireChange(new TransformChangeEvent(TransformChangeEvent.Type.ADD, tc));
    return tc;
  }

  private void updateModel() {
    transforms.removeAll(transforms);
    for (TransformControl tc: controls) {
      transforms.add(tc.getTransform());
    }
  }

  private static class TransformDragSourceListener extends DragSourceAdapter {

    private Composite parentComposite;
    private DragSource source;

    final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
    
    @Override
    public void dragFinished(DragSourceEvent event) {
      // TODO Auto-generated method stub
      System.out.println("TransformListControl.TransformDragSourceListener.dragFinished:");
      super.dragFinished(event);
    }

    @Override
    public void dragStart(DragSourceEvent event) {
      // TODO Auto-generated method stub
      System.out.println("TransformListControl.TransformDragSourceListener.dragStart:");
      super.dragStart(event);
    }

    /**
     * @param parentComposite - the composite that holds all pictures
     * @param source - the drag source
     *
     */
    public TransformDragSourceListener(Composite parentComposite, DragSource source) {
      this.parentComposite = parentComposite;
      this.source = source;
    }

    /**
     * The method computes the position / index of the source control
     * (label) in the children array of the parent composite. This index is
     * passed to the drop target using the data field of the drag source
     * event.
     */
    public void dragSetData(DragSourceEvent event) {
      System.out.println(
          "TransformListControl.TransformDragSourceListener.dragSetData: ");
      transfer.setSelection(new StructuredSelection(source));
      for (int i = 0; i < parentComposite.getChildren().length; i++) {
        // Here we need to do getControl().getParent() because we are dragging the XForm group
        // And moving the parent TransformControl
        if (parentComposite.getChildren()[i].equals(source.getControl().getParent())) {
          event.data = new Integer(i-1).toString(); // -1 because we're going to ignore the 'click to add' control
          System.out.println("="+(i-1));
          break;
        }
      }
    }

  }
 
  public static class TransformDropTargetListener extends DropTargetAdapter {

    private Composite parentComposite;
    private DropTarget target;

    /**
     * @param parentComposite - the composite that holds all pictures
     * @param target - the drop target
     */
    public TransformDropTargetListener(Composite parentComposite, DropTarget target) {
      this.parentComposite = parentComposite;
      this.target = target;
    }

    /**
     * This method moves the dragged picture to the new position and shifts the
     * old picture to the right or left.
     */
    public void drop(DropTargetEvent event) {
      
      System.out.println(
          "TransformListControl.TransformDropTargetListener.drop: ");


      // retrieve the stored index
      int sourceIndex = Integer.valueOf(event.data.toString());

      
      // compute the index of where we should insert
      TransformListControl targetControl = (TransformListControl)target.getControl();
      Point p=targetControl.getDisplay().map(null, targetControl, event.x, event.y);
      int eventY=p.y;
       
      for (int i = 0; i < targetControl.getChildren().length; i++) {
        int insertAfter=getMaxY(parentComposite.getChildren()[i]);
        
        int insertBefore=Integer.MAX_VALUE;        
        if (i<(parentComposite.getChildren().length-1)) {
          insertBefore=getMinY(parentComposite.getChildren()[i+1]);
        }
        
        System.out.println(
            "TransformListControl.TransformDropTargetListener.drop: after="+insertAfter+" y="+eventY+" before="+insertBefore);
        if (insertAfter<=eventY && insertBefore>=eventY) {
          System.out.println(
              "TransformListControl.TransformDropTargetListener.drop: moving..");
          targetControl.moveItemAfter(sourceIndex, (i-1)); // -1 because we're ignoring the 'click to add' control
          break;
        }
      }
    }
    private int getMaxY(Control ctrl) {
      org.eclipse.swt.graphics.Rectangle rect=ctrl.getBounds();
      return rect.y+rect.height;
    }
    private int getMinY(Control ctrl) {
      org.eclipse.swt.graphics.Rectangle rect=ctrl.getBounds();
      return rect.y;
    }
    
  }

  public Control getTransform(Object data) {
    if (data instanceof Integer) {
      return controls.get((Integer)data);      
    } else if (data instanceof String) {
      return controls.get(Integer.parseInt((String)data));
    } else {
      throw new IllegalArgumentException("Can't look up number of control with "+data.getClass().getName());
    }
  }

  public void moveItemBefore(int sourceIndex, int targetIndex) {
    System.out.println("TransformListControl.moveItemBefore:");
    System.out.println("TransformListControl.moveItemBefore: rotating items between "+sourceIndex+" and "+targetIndex);
    int direction=1;
    if (sourceIndex<targetIndex) {
      // rotate backwards; this will put the first item at the end
      direction=-1;
    } else {
      // we need to swap source and target around, so that subList works correctly
      int tmp=sourceIndex;
      sourceIndex=targetIndex;
      targetIndex=tmp;
    }
    Collections.rotate(controls.subList(sourceIndex, targetIndex+1), direction);
    reOrderTransforms();
  }

  public void moveItemAfter(int sourceIndex, int targetIndex) {
    System.out.println("TransformListControl.moveItemAfter:");
    System.out.println("TransformListControl.moveItemAfter: rotating items between "+sourceIndex+" and "+targetIndex);
    int direction=1;
    if (sourceIndex<targetIndex) {
      // rotate backwards; this will put the first item at the end
      direction=-1;
    } else {
      // we need to swap source and target around, so that subList works correctly
      int tmp=sourceIndex;
      sourceIndex=targetIndex;
      targetIndex=tmp;
      sourceIndex++; // If we're moving backwards, don't include the one before the gap in the rotation
    }
    Collections.rotate(controls.subList(sourceIndex, targetIndex+1), direction);
    reOrderTransforms();
  }
  
  void reOrderTransforms() {
    for (int i=controls.size()-1;i>0; i--) {
      controls.get(i-1).moveAbove(controls.get(i));
    }
    updateModel();
    fireChange(new TransformChangeEvent(TransformChangeEvent.Type.REORDER_TRANSFORMS));
  }

  public int indexOf(TransformControl tc) {
    return controls.indexOf(tc);
  }

  public void insertTransformAt(int idx, TransformControl tc) {    
    System.out.println("TransformListControl.insertTransformAt:");
    controls.add(idx, tc);
    updateModel();
  }

  public void remove(TransformControl tc) {
    controls.remove(tc);
    if (getChildren().length==1) {
      placeholder.setVisible(true);
    }
    updateModel();
    fireChange(new TransformChangeEvent(TransformChangeEvent.Type.REMOVED));
  }

  public void rebuild() {
    
  }

  public void makeInput(Transform newXform, Transform transform) {
    newXform.setInput(transform);
    
    int idx=transforms.indexOf(transform);
    transforms.add(idx, newXform);
    
    transforms.remove(transform);
    rebuildView();
  }

  private void rebuildView() {
    for (TransformControl tc: controls) {
      tc.dispose();
    }
    controls.removeAll(controls);
    for (Transform t: transforms) {
      addTransform(t);
    }

    fireChange(new TransformChangeEvent(TransformChangeEvent.Type.REBUILD));
    
  }
  
  private void fireChange(TransformChangeEvent source) {
//    setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
//    requestLayout();
    layout(false);
    for (TransformChangeListener lsnr: listeners) {
      lsnr.handleTransformChangeEvent(source);        
    }
  }
  
  public void addTransformChangeListener(TransformChangeListener lsnr) {

    listeners.add(lsnr);

  }

  public void removeTransformChangeListener(TransformChangeListener lsnr) {

    listeners.remove(lsnr);

  }

  @Override
  public void handleTransformChangeEvent(TransformChangeEvent evt) {
    // just pass straight through
  	if (evt.getType()==TransformChangeEvent.Type.DELETE_TRANSFORM) {
  		if (evt.getSource()!=null) {
  			deleteTransform((TransformControl)evt.getSource());
  		}
      fireChange(new TransformChangeEvent(TransformChangeEvent.Type.EVENT_HANDLED));
  	} else {
      fireChange(evt);
  	}
  }

  public void insertBefore(Widget widget) {
    Transform xform=getNewTransform();
    if (xform!=null) {
      TransformControl tc=addTransform(xform);
      moveItemBefore(controls.indexOf(tc), controls.indexOf(widget));
      this.changed(controls.toArray(new Control[0]));
      updateModel();
      fireChange(new TransformChangeEvent(TransformChangeEvent.Type.INSERT));
    }
    
  }

  public void insertAfter(Widget widget) {

    Transform xform=getNewTransform();
    if (xform!=null) {
      TransformControl tc=addTransform(xform);
      moveItemAfter(controls.indexOf(tc), controls.indexOf(widget));
      this.changed(controls.toArray(new Control[0]));
      updateModel();
      fireChange(new TransformChangeEvent(TransformChangeEvent.Type.INSERT));
    }
    
  }
  
  private Transform getNewTransform() {
    
    Transform xform=null;
    
    TransformTypeDialog dlg=new TransformTypeDialog(getShell());
    
    int res=dlg.open();
    if (res==0) {
      xform=new Transform(dlg.getType());
    }
  
    return xform;
  }
  
  private void showPlaceholder() {
    placeholder.setVisible(true);
    GridData gd=(GridData)placeholder.getLayoutData();
    gd.exclude=false;
    requestLayout();
  }
  private void hidePlaceholder() {
    placeholder.setVisible(false);
    GridData gd=(GridData)placeholder.getLayoutData();
    gd.exclude=true;
    requestLayout();
  }
  
  public void deleteTransform(TransformControl tc) {
    transforms.remove(tc.getTransform());
    controls.remove(tc);
    tc.dispose();
    if (transforms.size()==0) {
    	showPlaceholder();
    }
    fireChange(new TransformChangeEvent(TransformChangeEvent.Type.DELETE_TRANSFORM));
  }
}
