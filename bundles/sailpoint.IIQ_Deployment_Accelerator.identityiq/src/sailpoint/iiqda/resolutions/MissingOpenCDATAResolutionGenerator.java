package sailpoint.iiqda.resolutions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import sailpoint.iiqda.IIQPlugin;

public class MissingOpenCDATAResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {


    try {
      IMarkerResolution[] resolutions=new IMarkerResolution[2];

      resolutions[0]=new AddOpenCDATAResolution((String)marker.getAttribute("previousOpeningElementName"));
      resolutions[1]=new RemoveClosingCDATAResolution();
      return resolutions;
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException generating Resolutions", e);
      return null;

    }


  }

  public class AddOpenCDATAResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

    private String elementName;

    public AddOpenCDATAResolution(String elementName){
      this.elementName=elementName;
    }

    @Override
    public String getLabel() {
      return "Insert <![CDATA[ after <"+elementName+">";
    }

    @Override
    public void run(IMarker marker) {


      try {
        int pos=(Integer)marker.getAttribute("previousOpeningElementlocation");
        String element=(String)(marker.getAttribute("previousOpeningElementName"));
        int elementLength=element.length()+2; // 2 for < and >
        pos+=elementLength; // position it after the element
        IFile res=(IFile)(marker.getResource());

        modifyResource(res, ModificationType.INSERT, pos, "<![CDATA[");
      } catch (CoreException e) {
        IIQPlugin.logException("AddOpenCDATAResolution.run: CoreException", e);
      }
      try {
        marker.delete();
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

		@Override
    public String getDescription() {
	    return null;
    }

		@Override
    public Image getImage() {
	    return null;
    }

		@Override
    public IMarker[] findOtherMarkers(IMarker[] markers) {
	    return new IMarker[0];
    }

  }

  public class RemoveClosingCDATAResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

    public RemoveClosingCDATAResolution(){
    }

    @Override
    public String getLabel() {
      return "Remove ']]>'";
    }

    @Override
    public void run(IMarker marker) {
      try {
        int pos=(Integer)marker.getAttribute("location");
        IFile res=(IFile)(marker.getResource());

        modifyResource(res, ModificationType.DELETE, pos, "]]>");
      } catch (CoreException e) {
        IIQPlugin.logException("AddOpenCDATAResolution.run: CoreException", e);
      }
      try {
        marker.delete();
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

		@Override
    public String getDescription() {
	    return null;
    }

		@Override
    public Image getImage() {
	    return null;
    }

		@Override
    public IMarker[] findOtherMarkers(IMarker[] markers) {
	     return new IMarker[0];
    }

  }

}
