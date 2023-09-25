package sailpoint.iiqda.widgets.idn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.editors.idn.TransformEditorPart;
import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.objects.idn.Transform.Attribute;
import sailpoint.iiqda.objects.idn.Transform.Type;

public class TransformControl extends Composite implements Rebuildable, TransformChangeListener, Listener, ISelectionChangedListener {

  private Transform transform;
  private Text tId;
  //private TransformTypeComboViewer combo;
  private Transform.Type transformType;
  private Map<String, Control> attributeControls;

  List<TransformChangeListener> listeners;
  private Group grpAttributes;
  private Group grpXform;
  private TableViewer tv;
  private TableValuesList tableContent;
  private TransformEditorPart editor;
  private Table table;
  private TransformListControl parentList;
  private InputGroup grpInput; 

  public TransformControl(Composite parent, int style, Transform theTransform) {
    this(parent, style, theTransform, null);
  }

  public TransformControl(Composite parent, int style, Transform theTransform, TransformEditorPart transformEditorPart) {
    super(parent, SWT.BORDER);

    this.editor=transformEditorPart;
    this.transform=theTransform;
    this.transformType=theTransform.getTransformType(); // so we know if it changes; the popup menu will update the Transform then call updateModel()
    
    this.listeners=new ArrayList<TransformChangeListener>();

    attributeControls=new HashMap<String, Control>();

    setLayout(new FillLayout());

    grpXform = new Group(this, SWT.NONE);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns=1;
    grpXform.setLayout(gridLayout);
    
    if (transform.getId()!=null || transformEditorPart!=null) {
      Group grpId = new Group(grpXform, SWT.NONE);
      grpId.setLayout(gridLayout);
      grpId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      grpId.setText("ID");
      
      tId = new Text(grpId, SWT.BORDER);
      tId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      if (transform.getId()!=null) {
        tId.setText(transform.getId());
      }
      tId.addListener(SWT.Modify, this);
    }

    createContextMenu(grpXform);

    grpAttributes = new Group(grpXform, SWT.NONE);
    grpAttributes.setText("Attributes");
    grpAttributes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    grpAttributes.setLayout(new GridLayout(2, false));

    grpInput = new InputGroup(grpXform, SWT.NONE, transform.getInput());
    GridData gd=new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd.horizontalIndent=40;
    grpInput.setLayoutData(gd);
    grpInput.setLayout(gridLayout);
    grpInput.addTransformChangeListener(this);
    if (!hasInput()) {
      grpInput.setVisible(false);
      gd.exclude=true;
    }
    
    rebuildView();
  }

  private void rebuildView() {

    updateTitle();

    if (transform.getTransformType()!=Transform.Type.UNKNOWN) {

      //      grpType.addDragDetectListener(new DragDetectListener() {
      //        
      //        @Override
      //        public void dragDetected(DragDetectEvent arg0) {
      //          System.out.println("Type1499724481137.dragDetected:");          
      //        }
      //      });

      //      combo = new TransformTypeComboViewer(grpType);
      //      combo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      //      
      //      combo.addSelectionChangedListener(this);

      createAttributeUI();
    } else {
      new Label(grpXform, SWT.NONE).setText("Unable to create Transform for unhandled transform type "+transform.getUnknownType());
      tId.setEditable(false);
    }
//    setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
//    this.layout();
    layout(true, true);
  }
  
  private void createContextMenu(final Group grp) {
    grp.addListener(SWT.MenuDetect, new Listener() {
      @Override
      public void handleEvent(Event event) {
        System.out.println(
            "TransformControl menu");
        if (aboveBounds(grp.getDisplay().map(null, grp, event.x, event.y), grp.getClientArea())) {
          System.out.println("Above client Area");
          Menu popupMenu = new Menu(grp);

          MenuItem changeTypeItm = new MenuItem(popupMenu, SWT.NONE);
          changeTypeItm.setText("Change Transform Type");
          changeTypeItm.addSelectionListener(new ChangeTypeSelectionListener((TransformControl) grp.getParent()));

          MenuItem makeInputItm = new MenuItem(popupMenu, SWT.NONE);
          makeInputItm.setText("Make this the input to a new Transform");
          makeInputItm.addSelectionListener(new MakeInputSelectionListener((TransformControl) grp.getParent()));
          
          MenuItem makeParentItm = new MenuItem(popupMenu, SWT.NONE);
          makeParentItm.setText("Add a new Transform as input to this");
          makeParentItm.addSelectionListener(new MakeParentSelectionListener((TransformControl) grp.getParent()));
          if(hasInput()) {
            makeParentItm.setEnabled(false);  
          }

          if (parentList!=null) {
            MenuItem insertBeforeItem = new MenuItem(popupMenu, SWT.NONE);
            insertBeforeItem.setText("Insert new Transform before this one");
            insertBeforeItem.addSelectionListener(new InsertBeforeSelectionListener(parentList, (TransformControl) grp.getParent()));
  
            MenuItem insertAfterItem = new MenuItem(popupMenu, SWT.NONE);
            insertAfterItem.setText("Insert new Transform after this one");
            insertAfterItem.addSelectionListener(new InsertAfterSelectionListener(parentList, (TransformControl) grp.getParent()));
          }
          
          MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
          deleteItem.setText("Delete Transform");
          deleteItem.addSelectionListener(new DeleteTransformSelectionListener((TransformControl) grp.getParent()));

          grp.setMenu(popupMenu);

        }

      }

      private boolean aboveBounds(Point p, Rectangle clientArea) {        
        System.out.println("y="+p.y+" bounds: "+grp.getClientArea());
        return (p.y<clientArea.y);
      }
    });
  }

  private boolean hasInput() {
    return grpInput!=null && grpInput.getInput()!=null;
  }
  
  
  private void updateModel() {

    if (tId!=null) {
      String text = tId.getText();
      if (!text.equals(transform.getId())) {
        transform.setId(text);
      }
    }
    if (transformType!=transform.getTransformType()) {
      transformType=transform.getTransformType();
      updateTitle();
      createAttributeUI();
    }
    for (Attribute attr: transformType.getAttributes()) {
      Control ctrl=attributeControls.get(attr.getName());
      if (ctrl!=null) {
        Object currentValue=transform.getAttribute(attr.getName());
        if (ctrl instanceof Button) {
          boolean value=((Button)ctrl).getSelection();
          if (currentValue==null) currentValue=Boolean.FALSE;
          if (value!=(Boolean)currentValue) {
            transform.setAttribute(attr.getName(), value);
          }
        } else if (ctrl instanceof Text) {
          String value=((Text)ctrl).getText();
          if (currentValue==null) currentValue="";
          if (!((String)currentValue).equals(value)) {
            if (value.equals("")) {
              transform.removeAttribute(attr.getName());
            } else {
              transform.setAttribute(attr.getName(), "".equals(value)?null:value);
            }
          }
        } else if (ctrl instanceof Table) {
          TableItem[] value=((Table)ctrl).getItems();

          Map<String,String> mValue=new HashMap<String,String>();
          for (TableItem itm: value) {
            mValue.put(itm.getText(0), itm.getText(1));
          }

          if (currentValue==null && value.length!=0 ||
              currentValue!=null && !((Map)currentValue).equals(mValue)) {
            transform.setAttribute(attr.getName(), mValue);
          }
        }
      }
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void createAttributeUI() {

    Control[] currentControls = grpAttributes.getChildren();
    if (currentControls!=null) {
      for (Control ctrl: currentControls) {
        ctrl.dispose();
      }
    }

    if (transform.getTransformType().hasAttributes()) {
      if (transform.getType()==null) {
        addDefaultComponent(grpAttributes, transform.getUnknownType());
      } else {
        Map<String,Object> values=transform.getAttributes();
        if (values==null) values=new HashMap<String,Object>();
        for (Attribute attr: transform.getTransformType().getAttributes()) {
          String attrName=attr.getName();
          Object value=values.get(attrName);
          Label lbl=new Label(grpAttributes, SWT.NONE);
          lbl.setText(attrName);
          lbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
          Control ctrl=null;
          switch (attr.getType()) {
            case LIST: {
              // Assume that List is strings, unless it's a FirstValid transform, in which case it's a list of Transforms
              Class clazz=String.class;
              if (transform.getTransformType()==Type.FIRSTVALID) {
                clazz=Transform.class;
              }
              if (value==null) { // TODO does this fix our dirty flag when adding first list element problem?
                value=new ArrayList();
                values.put(attrName, value);
              }
              ctrl=addListComponent(grpAttributes, (List)value, clazz);
              break;
            }
            case BOOLEAN: ctrl=addBooleanComponent(grpAttributes, (Boolean)value); break;
            case TABLE: ctrl=addTableComponent(grpAttributes, (Map<String,String>)value); break;
            case INTEGER:
            case STRING: ctrl=addStringComponent(grpAttributes, (value!=null)?value.toString():""); break;
            default: addDefaultComponent(grpAttributes, attr.getType().toString());       
          }
          if (ctrl!=null) {
            attributeControls.put(attr.getName(), ctrl);
          }
        }
      }
      grpAttributes.setVisible(true);
      GridData gd=(GridData)grpAttributes.getLayoutData();
      gd.exclude=false;
      grpAttributes.setLayoutData(gd);
      grpAttributes.getParent().layout(true, true);
    } else { // Remove the 'attributes' controls
      grpAttributes.setVisible(false);
      GridData gd=(GridData)grpAttributes.getLayoutData();
      gd.exclude=true;
      grpAttributes.setLayoutData(gd);
      grpAttributes.getParent().getParent().layout(true, true);
    }
  }

  private Control addStringComponent(Composite parent, String string) {
    Text textCmp=new Text(parent, SWT.BORDER);
    textCmp.setText(string);
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd.widthHint=200;
    textCmp.setLayoutData(gd);    
    textCmp.addListener(SWT.Modify, this);
    return textCmp;
  }

  private Control addBooleanComponent(Composite parent, Boolean bool) {
    Button btnCmp=new Button(parent, SWT.CHECK);
    btnCmp.setSelection(bool!=null?bool:false);
    btnCmp.addListener(SWT.Modify, this);
    return btnCmp;
  }

  private void addDefaultComponent(Composite parent, String string) {
    Label textCmp=new Label(parent, SWT.NONE);
    textCmp.setText("Unhandled Attribute type: "+string);
  }

  private Control addTableComponent(Composite parent, Map<String, String> value) {

    final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setLayout(new GridLayout(1, false));
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd.heightHint=500;
    scrolledComposite.setLayoutData(gd);


    tv = new TableViewer(scrolledComposite, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    tv.setColumnProperties( new String[] {"key","value"});
    tv.setContentProvider(new ArrayContentProvider());
    
    table = tv.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setLayoutData(gd);

    TableViewerColumn tvCol = new TableViewerColumn(tv, SWT.NONE);
    TableColumn tCol=tvCol.getColumn();
    tCol.setWidth(100);
    tCol.setText("Key");
    KeyEditingSupport keyEditingSupport = new KeyEditingSupport(tv);
    keyEditingSupport.addModifyListener(this);
    
    tvCol.setEditingSupport(keyEditingSupport);
    
    tvCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return ((KeyValuePair)element).key;
      }
    });
    
    TableViewerColumn tvCol_1 = new TableViewerColumn(tv, SWT.NONE);
    TableColumn tCol_1=tvCol_1.getColumn();    
    tCol_1.setWidth(100);
    tCol_1.setText("Value");
    ValueEditingSupport valueEditingSupport = new ValueEditingSupport(tv);
    valueEditingSupport.addModifyListener(this);
    tvCol_1.setEditingSupport(valueEditingSupport);
    tvCol_1.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return ((KeyValuePair)element).value;
      }
    });


    ArrayList<KeyValuePair> tableSource=new ArrayList<KeyValuePair>();

    if (value!=null) {
      for (String key: value.keySet()) {
        KeyValuePair kvp=new KeyValuePair(key, value.get(key));
        tableSource.add(kvp);
      }
    } 
    KeyValuePair kvp=new KeyValuePair("", "");
    tableSource.add(kvp);

    tv.setInput(tableSource);

    scrolledComposite.setContent(table);
    //    scrolledComposite.layout(true, true);
    //    scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    return table;
  }

  private Control addListComponent(Composite parent, List lValues, Class clazz) {
    if (clazz==String.class) {
      org.eclipse.swt.widgets.List listCmp=new org.eclipse.swt.widgets.List(parent, SWT.NONE);
      if (lValues!=null) {
        String[] values=new String[lValues.size()];
        for (int i=0;i<lValues.size();i++) {
          values[i]=lValues.get(i).toString();
        }
        listCmp.setItems(values);
      }
      return listCmp;
    } else if (clazz==Transform.class) {
      if (lValues!=null) {
        for (Object obj: lValues) {
          if (!(obj instanceof Transform)) {
            throw new IllegalArgumentException("Expected Transform in list: got "+obj.getClass().getName());
          }
        }
      }
      TransformListControl cmp=new TransformListControl(parent, SWT.NONE, lValues);
      cmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      cmp.addTransformChangeListener(this);
      return cmp;

    } else {
      throw new IllegalArgumentException("Can't create a List for class "+clazz.getName());
    }
  }


  public void removeListener(int eventType, ModifyListener ml) {
    System.out.println("TransformControl.removeModifyListener: ");

    listeners.remove(ml);
  }

  public String toString() {
    StringBuilder sb=new StringBuilder();
    sb.append("Transform: ");
    sb.append(transform.getId());
    sb.append("(");
    sb.append(transform.getType());
    sb.append(")");
    return sb.toString();
  }

  @Override
  public void handleEvent(Event evt) {

    System.out.println(evt);
    if (evt.type==SWT.Modify) {
      updateModel();
    }
    if (evt.widget!=null) {
      fireChange(new TransformChangeEvent(TransformChangeEvent.Type.WIDGET_CHANGE, evt.widget));
    } else {
      fireChange(new TransformChangeEvent(TransformChangeEvent.Type.DATA_CHANGE, evt.data));
    }
  }
  
  private void fireChange(TransformChangeEvent evt) {
    Iterator<TransformChangeListener> iter = listeners.iterator();

    while (iter.hasNext()) {
      TransformChangeListener lsnr = iter.next(); 
      lsnr.handleTransformChangeEvent(evt);
    }
//    if (grpAttributes!=null) {
//      for (Control ctrl: grpAttributes.getChildren()) {
//        ctrl.setSize(ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//      }
////      grpAttributes.setSize(grpAttributes.computeSize(SWT.DEFAULT, SWT.DEFAULT));
////      grpAttributes.requestLayout();
//      grpAttributes.ayout(true, true);
//    }
//    if (inputControl!=null) {
//      inputControl.setSize(inputControl.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//      inputControl.requestLayout();
//    }
//    layout(true, true);
  }

  public void addTransformChangeListener(TransformChangeListener lsnr) {

    listeners.add(lsnr);

  }

  public void removeTransformChangeListener(TransformChangeListener lsnr) {

    listeners.remove(lsnr);

  }

  @Override
  public void selectionChanged(SelectionChangedEvent arg0) {
    updateModel();

  }

  public Group getGroup() {
    return grpXform;
  }

  public Transform getTransform() {
    return transform;
  }

  private class ChangeTypeSelectionListener implements SelectionListener {

    private TransformControl tc;

    public ChangeTypeSelectionListener(TransformControl theControl) {
      this.tc=theControl;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      TransformTypeDialog dlg=new TransformTypeDialog(tc.getShell());
      int res=dlg.open(tc.getTransform().getTransformType());
      if (res==0) {
        if (TransformControl.this.transformType!=dlg.getType()){
          TransformControl.this.transformType=dlg.getType();
          TransformControl.this.transform.setTransformType(dlg.getType());          
          rebuildView();
          TransformChangeEvent evt=new TransformChangeEvent(TransformChangeEvent.Type.TYPE_CHANGE, tc);
          fireChange(evt);
        }

      }
    }
    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }

  }

  private class MakeInputSelectionListener implements SelectionListener {

    private TransformControl tc;

    public MakeInputSelectionListener(TransformControl theControl) {
      this.tc=theControl;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      TransformTypeDialog dlg=new TransformTypeDialog(tc.getShell());
      int res=dlg.open();
      if (res==0) {
        Control parent=tc.getParent();

        Transform newXform = new Transform(dlg.getType());
        if (parent instanceof TransformListControl) {
          // This is one of a list of Transforms
          ((TransformListControl)parent).makeInput(newXform, tc.getTransform());         
        } else if (parent instanceof TransformControl) {
          newXform.setInput(tc.getTransform());
          ((TransformControl)parent).getTransform().setInput(newXform);
        } else if (parent instanceof InputGroup) { // TODO: make this an input. maybe subclass group as InputGroup?
          ((InputGroup)parent).wrap(newXform);
          
          ((InputGroup) parent).layout(true,true);
//          TransformControl.this.transform.setInput(newXform);
        } else if (parent instanceof Composite) {
          // This is the top of the food chain
          newXform.setInput(tc.getTransform());
          // Move the ID to the new parent
          newXform.setId(tc.getTransform().getId());
          tc.getTransform().setId(null);

          editor.setNewRoot(newXform);
        }

        //            rebuildView();
        fireChange(new TransformChangeEvent(TransformChangeEvent.Type.NEW_PARENT, newXform));

        //            isDirty=true;
      }
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }

  }

  // Make this (the selection) the parent of the new item
  private class MakeParentSelectionListener implements SelectionListener {

    private TransformControl tc;

    public MakeParentSelectionListener(TransformControl theControl) {
      this.tc=theControl;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {      
      TransformTypeDialog dlg=new TransformTypeDialog(tc.getShell());
      int res=dlg.open();
      if (res==0) {
        Transform newXform = new Transform(dlg.getType());
        tc.setInput(newXform);
        fireChange(new TransformChangeEvent(TransformChangeEvent.Type.NEW_CHILD, tc));
        layout(true, true);
      }
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }

  }

  private class InsertBeforeSelectionListener implements SelectionListener {
    
    private TransformListControl tcList;
    private TransformControl tCtrl;
    
    public InsertBeforeSelectionListener(TransformListControl theControl, TransformControl ctrl) {
      this.tcList=theControl;
      this.tCtrl=ctrl;
    }
    
    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      tcList.insertBefore(tCtrl);
    }
    
    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }
    
  }

  private class InsertAfterSelectionListener implements SelectionListener {
    
    private TransformListControl tcList;
    private TransformControl tCtrl;
    
    public InsertAfterSelectionListener(TransformListControl theControl, TransformControl ctrl) {
      this.tcList=theControl;
      this.tCtrl=ctrl;
    }
    
    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      tcList.insertAfter(tCtrl);
    }
    
    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }
    
  }
  
  private class DeleteTransformSelectionListener implements SelectionListener {

    private TransformControl tc;

    public DeleteTransformSelectionListener(TransformControl theControl) {
      this.tc=theControl;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      System.out.println(
          "DeleteTransform");
      TransformChangeEvent tce=new TransformChangeEvent(TransformChangeEvent.Type.DELETE_TRANSFORM, tc);
      fireChange(tce);

    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }

  }

  public void setInput(Transform xform) {
    transform.setInput(xform);
    if (!grpInput.getVisible()) {
      grpInput.setVisible(true);
      GridData gd=(GridData)grpInput.getLayoutData();
      gd.exclude=false;
      grpInput.setInput(xform);
    }
  }

  public void updateTask(KeyValuePair kvp) {
    tableContent.update(kvp);
    ((Map)transform.getAttribute("table")).remove(kvp.oldKey);
    ((Map)transform.getAttribute("table")).put(kvp.key, kvp.value);
    Event evt=new Event();
    evt.type=SWT.Modify;
    evt.widget=table;
    handleEvent(evt);
  }

//  class TableValuesContentProvider implements IStructuredContentProvider {
//
//    // Return the tasks as an array of Objects
//    public Object[] getElements(Object parent) {
//      return tableContent.getLevelSettings().toArray();
//    }
//
//    /* (non-Javadoc)
//     * @see ITaskListViewer#addTask(ExampleTask)
//     */
//    public void addTask(LogLevelSetting task) {
//      tv.add(task);
//    }
//
//    /* (non-Javadoc)
//     * @see ITaskListViewer#removeTask(ExampleTask)
//     */
//    public void removeTask(LogLevelSetting task) {
//      tv.remove(task);     
//    }
//
//    /* (non-Javadoc)
//     * @see ITaskListViewer#updateTask(ExampleTask)
//     */
//    public void updateTask(TableValuesList task) {
//      tv.update(task, null); 
//    }
//
////    @Override
////    public void dispose() {
////    }
//
//    @Override
//    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//    }
//  }

  class TableValuesList {

    private ArrayList<KeyValuePair> settings;

    public TableValuesList(ArrayList<KeyValuePair> tableEntry) {
      this.settings=tableEntry;
    }

    public void add(KeyValuePair setting) {
      settings.add(setting);
      tv.add(setting);
    }

    public void remove(KeyValuePair setting) {
      settings.remove(setting);
      tv.remove(setting);
    }

    public void update(KeyValuePair setting) {
      tv.update(setting, null);
    }

    public List<KeyValuePair> getLevelSettings() {
      return settings;
    }

  }
  
  private void updateTitle() {
    grpXform.setText(transform.getTransformType().getDescription());
  }

  @Override
  public void handleTransformChangeEvent(TransformChangeEvent source) {  
    System.out.println("TransformControl.handleTransformChangeEvent: passing through "+transform.getType());
    if (grpInput!=null) {
      if ( grpInput.getInput()!=null && !(grpInput.getInput().equals(transform.getInput())) ) {
        transform.setInput(grpInput.getInput());
      }
      grpInput.layout(true, true);
    }
    layout(true, true);
    fireChange(source);
  }

  public void addInsertionMenuItems(TransformListControl transformListControl) {
    this.parentList=transformListControl;
  }
  
}
