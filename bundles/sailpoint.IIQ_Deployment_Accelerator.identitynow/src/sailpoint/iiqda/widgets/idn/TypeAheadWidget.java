package sailpoint.iiqda.widgets.idn;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.idn.IDNRestHandler;

public class TypeAheadWidget extends Composite implements ModifyListener {

  private IDNRestHandler handler;
  private Text theText;
  int wait=500;
  private Thread waiter;
  
  private class GetOptionsWaiter extends Thread {
    
    private String stub;
    
    public GetOptionsWaiter(String stub) {
      this.stub=stub;
      
    }
    
    public void run() {
      boolean go=true;
      try {
        Thread.sleep(wait);
      } catch (InterruptedException ie) {
        go=false;
      }
      System.out.println(stub+" : go="+go);
      if (go) {
        //List<Identity> identities=handler.getObjects("identity");
      }
    }
    
  }
  
  public TypeAheadWidget(Composite parent, int style, IDNRestHandler handler) {
    super(parent, style);
    this.handler=handler;
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    setLayout(layout);
    theText=new Text(this, SWT.NONE);
    theText.addModifyListener(this);
  }

  @Override
  public void modifyText(ModifyEvent arg0) {
    if (waiter!=null) waiter.interrupt();
    waiter=new GetOptionsWaiter(theText.getText());
    waiter.start();
  }
  
  public String getText() {
    return theText.getText();
  }
  
  public void setText(String text) {
    theText.setText(text);
  }
  

  
}
