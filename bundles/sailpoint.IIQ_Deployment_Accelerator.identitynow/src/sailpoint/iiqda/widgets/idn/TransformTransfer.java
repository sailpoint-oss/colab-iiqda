package sailpoint.iiqda.widgets.idn;

import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class TransformTransfer extends Transfer {

  @Override
  public TransferData[] getSupportedTypes() {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.getSupportedTypes:");
    return null;
  }

  @Override
  protected int[] getTypeIds() {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.getTypeIds:");
    return null;
  }

  @Override
  protected String[] getTypeNames() {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.getTypeNames:");
    return null;
  }

  @Override
  public boolean isSupportedType(TransferData arg0) {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.isSupportedType:");
    return false;
  }

  @Override
  protected void javaToNative(Object arg0, TransferData arg1) {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.javaToNative:");

  }

  @Override
  protected Object nativeToJava(TransferData arg0) {
    // TODO Auto-generated method stub
    System.out.println("TransformTransfer.nativeToJava:");
    return null;
  }

}
