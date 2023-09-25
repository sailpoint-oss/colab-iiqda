package sailpoint.iiqda.editors.idn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.widgets.idn.Rebuildable;
import sailpoint.iiqda.widgets.idn.TransformChangeEvent;
import sailpoint.iiqda.widgets.idn.TransformChangeListener;
import sailpoint.iiqda.widgets.idn.TransformControl;

public class TransformEditorPart extends EditorPart implements TransformChangeListener {

  private Composite cmp;
  private ScrolledComposite sc;
  private TransformControl tc;
  private Transform transform;
  private Transform originalTransform;
  private boolean dirtyFlag=false;
  private IProject theProject;


  @Override
  public void doSave(IProgressMonitor arg0) {
    // TODO Auto-generated method stub
    System.out.println("TransformEditorPart.doSave:");

  }

  @Override
  public void doSaveAs() {
    // TODO Auto-generated method stub
    System.out.println("TransformEditorPart.doSaveAs:");

  }

  @Override
  public void init(IEditorSite site, IEditorInput input)
      throws PartInitException {
    System.out.println("TransformEditorPart.init:");
    
    setSite(site);
    setInput(input);
    
    IFile theFile=((FileEditorInput) input).getFile();
    theProject=theFile.getProject();
    try {
      parseInput(theFile.getContents());
    } catch (CoreException ce) {
      throw new PartInitException("CoreException parsing file contents: "+ce);
    }
    
    
  }

  @Override
  public boolean isDirty() {
    // TODO Auto-generated method stub
    System.out.println("TransformEditorPart.isDirty:");
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    // TODO Auto-generated method stub
    System.out.println("TransformEditorPart.isSaveAsAllowed:");
    return false;
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
    System.out.println("TransformEditorPart.setFocus:");

  }

  @Override
  public void createPartControl(Composite arg0) {
    System.out.println("TransformEditorPart.createPartControl: ");

    arg0.setLayout(new FillLayout());

    sc = new ScrolledComposite(arg0, SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
    sc.setLayout(new FillLayout());
    cmp = new Composite(sc, SWT.BORDER);
    cmp.setLayout(new FillLayout());
    sc.setContent(cmp);

    //    GridLayout gridLayout = new GridLayout();
    //    gridLayout.numColumns=1;
    buildTree();
  }

  /*
   * Rebuild the Transform tree, and replace it in the UI
   */
  public void buildTree() {
    System.out.println("TransformEditorPart.buildTree: ");

    if (tc!=null) {
      tc.removeTransformChangeListener(this);
      tc.dispose();
    }
    tc = new TransformControl(cmp, SWT.NONE, transform, this);
    //    tc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    setPartName("Transform: " + transform.getId());

    tc.addTransformChangeListener(this);
    cmp.setSize(cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
    cmp.layout();
  }

  @Override
  public void handleTransformChangeEvent(TransformChangeEvent source) {
    System.out.println("TransformChangeListener.handleTransformChangeEvent: source="+source.getClass().getName());

    boolean dirty=!(originalTransform.equals(transform));
    if (dirtyFlag!=dirty) {
      dirtyFlag=dirty;
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }
    //    System.out.println("cmp size: "+cmp.getSize());
    //    System.out.println("cmp pref size "+cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    //    System.out.println("cmp pref size "+cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
    cmp.setSize(cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));

    if (source instanceof Rebuildable) {
      cmp.layout(true, true);
      //      System.out.println("--after layout");
      //      System.out.println("cmp size: "+cmp.getSize());
      //      System.out.println("cmp pref size "+cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      //      System.out.println("cmp pref size "+cmp.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
      //      System.out.println("sc size: "+sc.getSize());
      //      System.out.println("sc pref size "+sc.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      //      System.out.println("sc pref size "+sc.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
    }
    System.out.println("TransforEditor.handleEvent: flag="+dirtyFlag);

  }

  public void setNewRoot(Transform newXform) {

    // Root transform has changed, so rebuild the UI tree

    System.out.println("TransformEditor.setNewRoot:");
    transform=newXform;
    tc.dispose();
    buildTree();

  }

  public Transform getTransform() {
    return transform;
  }

  public void parseInput(InputStream contents) {
    GsonBuilder gsonBldr=new GsonBuilder();
    //  gsonBldr.registerTypeAdapter(Transform.Type.class, new com.google.gson.TypeAdapter() {
    //
    //    @Override
    //    public Object read(JsonReader arg0) throws IOException {
    //      String typeCode=arg0.nextString();
    //      System.out.println("TypeAdapter.read "+typeCode);
    //      return Transform.Type.getTypeOf(typeCode);
    //    }
    //
    //    @Override
    //    public void write(JsonWriter arg0, Object arg1) throws IOException {
    //      System.out.println("TypeAdapter.write");
    //      
    //    }
    //    
    //  });

    gsonBldr.registerTypeAdapter(Transform.class, new com.google.gson.TypeAdapter() {

      @Override
      public Object read(final JsonReader in) throws IOException {

        System.out.println("TypeAdapter.read");

        Transform xform=new Transform();

        in.beginObject();
        while (in.hasNext()) {
          switch (in.nextName()) {
            case "id":
              xform.setId(in.nextString());
              break;
            case "type":
              xform.setType(in.nextString());
              break;
            case "attributes":
              System.out.println("case: attributes");
              JsonToken peekToken=in.peek();
              if (peekToken==JsonToken.NULL) {
                // do nothing for null except grab it from the stream
                in.nextNull();
              } else if (peekToken==JsonToken.BEGIN_OBJECT) {
                in.beginObject();
                while (in.hasNext()) {
                  String nxtName = in.nextName();
                  System.out.println("nxtName="+nxtName);
                  switch (nxtName) {
                    case "input":
                      xform.setInput((Transform)read(in));
                      break;
                    case "values": 
                      in.beginArray();
                      List<Object> values=new ArrayList<Object>();
                      while (in.hasNext()) {
                        JsonToken type=in.peek();
                        if (type==JsonToken.STRING) {
                          String str=in.nextString();
                          values.add(str);
                        } else if (type==JsonToken.BEGIN_OBJECT) {
                          // We'll need to fix this at some point, but right now the only 'object' (complex object) in a values list is Transform. Otherwise it's a string 
                          values.add((Transform)read(in));
                        }
                      }
                      in.endArray();
                      xform.setAttribute("values", values);
                      break;
                    case "table":
                      in.beginObject();
                      Map<String,String> tableData=new HashMap<String,String>();
                      while (in.hasNext()) {
                        String name=in.nextName();
                        String value=in.nextString();
                        System.out.println("table: "+name+"="+value);
                        tableData.put(name, value);                      
                      }
                      in.endObject();
                      xform.setAttribute("table",  tableData);
                      break;
                    default:
                      String nextString = in.nextString();
                      System.out.println("attribute "+nxtName+"="+nextString);
                      xform.setAttribute(nxtName, nextString);
                  }
                }
                in.endObject();
              }
              break;
          }
        }
        in.endObject();


        return xform;
      }

      @Override
      public void write(JsonWriter arg0, Object arg1) throws IOException {
        System.out.println("TypeAdapter.write");

      }

    });

    Gson gson=gsonBldr.create();

    //  JSONDeserializer json=new JSONDeserializer()
    //      .use(null, Transform.class)
    //      .use("attributes.input",  Transform.class)
    //      .use("attributes.values", new AttributeValuesTransformer());
    try {
      this.transform=(Transform)gson.fromJson(new InputStreamReader(contents), Transform.class);
      originalTransform=new Transform(transform);
    } catch (Exception e) {
      System.out.println("..");
      e.printStackTrace();
    }

  }
  
}
