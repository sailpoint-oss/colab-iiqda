package sailpoint.iiqda.editors.idn;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import sailpoint.iiqda.idn.IDNHelper;

public class TransformEditor  extends MultiPageEditorPart implements IEditorPart, IResourceChangeListener, IElementStateListener {

  private FileEditorInput fileInput;
  private TransformEditorPart xformEd;
  private TextEditor textEd;

  private boolean dirty=false;

  public TransformEditor() {
    super();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
  }

  @Override
  public void init(IEditorSite site, IEditorInput edInput)
      throws PartInitException {

    super.init(site, edInput);
    System.out.println("SourceEditor.init:");
    if (!(edInput instanceof FileEditorInput)) {
      throw new RuntimeException("Wrong input");
    }

    this.fileInput = (FileEditorInput) edInput;
    setSite(site);
    setInput(edInput);


  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    getEditor(0).doSave(monitor);

    //    String out = IDNHelper.transformToJSON(transform);
    //
    //    System.out.println(out);
    //    
    //    IFile theFile=fileInput.getFile();
    //    try {
    //      theFile.setContents(new ByteArrayInputStream(out.getBytes("UTF-8")), false, true, arg0);
    //      originalTransform=new Transform(transform);
    //      dirtyFlag=false;
    //      firePropertyChange(IEditorPart.PROP_DIRTY);
    //    } catch (CoreException ce) {
    //      System.out.println("TransformEditor.doSave: CoreException "+ce);
    //
    //    } catch (UnsupportedEncodingException e) {
    //      // TODO Auto-generated catch block
    //      e.printStackTrace();
    //    }
    dirty=false;
    firePropertyChange(IEditorPart.PROP_DIRTY);
  }


  @Override
  public void doSaveAs() {
    System.out.println("TransformEditor.doSaveAs:");

  }

  @Override
  public boolean isDirty() {
    System.out.println("TransformEditor.isDirty: "+dirty);
    return dirty;
  }

  @Override
  public boolean isSaveAsAllowed() {
    //    System.out.println("TransformEditor.isSaveAsAllowed:");
    return false;
  }

  @Override
  public void setFocus() {
    //    System.out.println("TransformEditor.setFocus:");

  }

  @Override
  protected void createPages() {

    try {
      xformEd=new TransformEditorPart();
      int index=addPage(xformEd, getEditorInput());
      setPageText(index, "Model");

      textEd=new TextEditor();
      index=addPage(textEd, getEditorInput());
      setPageText(index, "JSON");

      IDocumentProvider p=textEd.getDocumentProvider();
      p.addElementStateListener(this);

      if (xformEd.getTransform()!=null) {
        setPartName("Transform: " + xformEd.getTransform().getId());
      }

    } catch (PartInitException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void resourceChanged(IResourceChangeEvent arg0) {
    // TODO Auto-generated method stub
    System.out.println("IResourceChangeListener.resourceChanged:");

  }

  @Override
  protected void pageChange(int newPageIndex) {
    // TODO Auto-generated method stub
    System.out.println("TransformEditor.pageChange: "+newPageIndex);
    if (newPageIndex==0) {
      // Changing to 'Model' page. update the model
      IDocumentProvider dp=textEd.getDocumentProvider();
      IDocument doc=dp.getDocument(textEd.getEditorInput());
      String out=doc.get();
      xformEd.parseInput(new ByteArrayInputStream(out.getBytes()));
      xformEd.buildTree();
    } else if (newPageIndex==1) {
      // Changing to 'JSON' page. serialize the model
      String out = IDNHelper.transformToJSON(xformEd.getTransform());
      IDocumentProvider dp=textEd.getDocumentProvider();
      IDocument doc=dp.getDocument(textEd.getEditorInput());
      doc.set(out);      
    }
    super.pageChange(newPageIndex);
  }


  @Override
  public void elementContentAboutToBeReplaced(Object arg0) {
    // TODO Auto-generated method stub
    System.out.println("IElementStateListener.elementContentAboutToBeReplaced:");

  }

  @Override
  public void elementContentReplaced(Object arg0) {
    // TODO Auto-generated method stub
    System.out.println("IElementStateListener.elementContentReplaced:");

  }

  @Override
  public void elementDeleted(Object arg0) {
    // TODO Auto-generated method stub
    System.out.println("IElementStateListener.elementDeleted:");

  }

  @Override
  public void elementDirtyStateChanged(Object arg0, boolean arg1) {
    System.out.println("IElementStateListener.elementDirtyStateChanged:");
    firePropertyChange(PROP_DIRTY);
  }

  @Override
  public void elementMoved(Object arg0, Object arg1) {
    // TODO Auto-generated method stub
    System.out.println("IElementStateListener.elementMoved:");

  }

}
