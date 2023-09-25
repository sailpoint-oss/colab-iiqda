package sailpoint.iiqda.comparison;

import java.util.List;

import org.eclipse.compare.ICompareFilter;
import org.eclipse.jface.text.IRegion;

public abstract class AbstractArtifactComparator implements ICompareFilter {

  @Override
  public void setInput(Object input, Object ancestor, Object left, Object right) {
  }

  @Override
  public boolean isEnabledInitially() {
    return true;
  }

  @Override
  public boolean canCacheFilteredRegions() {
    return true;
  }

  protected void getStringRegions(List<IRegion> regions, String string, String thisLine) {
    if(thisLine!=null) {
      int idLoc=thisLine.indexOf(string);
      while(idLoc!=-1) {
        IRegion region=new TextRegion(idLoc, string.length());
        regions.add(region);
        idLoc=thisLine.indexOf(string, idLoc+string.length()+1);
      }
    }
  }

  protected class TextRegion implements IRegion {
    
    private int offset;
    private int length;

    public TextRegion(int offset, int length) {
      this.offset=offset;
      this.length=length;
    }
    
    @Override
    public int getOffset() {
      return offset;
    }
    
    @Override
    public int getLength() {
      return length;
    }
  }
}
