package sailpoint.iiqda.resolutions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import sailpoint.iiqda.IIQPlugin;

public class MissingCloseCDATAResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {


    try {
      IMarkerResolution[] resolutions=new IMarkerResolution[2];

      resolutions[0]=new AddCloseCDATAResolution((String)marker.getAttribute("previousOpeningElementName"));
      resolutions[1]=new RemoveOpeningCDATAResolution();
      return resolutions;
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException generating Resolutions", e);
      return null;

    }


  }

  public class AddCloseCDATAResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

    private String elementName;

    public AddCloseCDATAResolution(String elementName){
      this.elementName=elementName;
    }

    @Override
    public String getLabel() {
      return "Insert ]]> before </"+elementName+">";
    }

    @Override
    public void run(IMarker marker) {

      try {
        int pos=(Integer)marker.getAttribute("previousOpeningElementlocation");
        String element=(String)(marker.getAttribute("previousOpeningElementName"));
        int elementLength=element.length()+2; // 2 for < and >
        pos+=elementLength; // position it after the element
        IFile res=(IFile)(marker.getResource());

        modifyResource(res, ModificationType.INSERTBEFORE, pos, "]]>", "</"+element+">");
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

  public class RemoveOpeningCDATAResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

    public RemoveOpeningCDATAResolution(){
    }

    @Override
    public String getLabel() {
      return "Remove '<![CDATA['";
    }

    @Override
    public void run(IMarker marker) {
      try {
        int pos=(Integer)marker.getAttribute("previousOpeningElementlocation");
        String element=(String)marker.getAttribute("previousOpeningElementName");
        pos+=element.length()+2; // +2 for <> 

        IFile res=(IFile)(marker.getResource());

        modifyResource(res, ModificationType.DELETE, pos, "<![CDATA[");
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
