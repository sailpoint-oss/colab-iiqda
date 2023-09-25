package sailpoint.iiqda.resolutions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import sailpoint.iiqda.IIQPlugin;

public class MissingCloseElementResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {


    try {
      IMarkerResolution[] resolutions=new IMarkerResolution[1];

      resolutions[0]=new AddCloseElementResolution((String)marker.getAttribute("expectedElement"), (String)marker.getAttribute("foundElement"));
      return resolutions;
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException generating Resolutions", e);
      return null;

    }


  }

  public class AddCloseElementResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

    private String expected;
    private String found;

    public AddCloseElementResolution(String expected, String found){
      this.expected=expected;
      this.found=found;
    }

    @Override
    public String getLabel() {
      return "Insert </"+expected+"> before </"+found+">";
    }

    @Override
    public void run(IMarker marker) {

      try {
        int pos=(Integer)marker.getAttribute("location");
        String expectedElement=(String)(marker.getAttribute("expectedElement"));
        String foundElement=(String)(marker.getAttribute("foundElement"));
        IFile res=(IFile)(marker.getResource());

        modifyResource(res, ModificationType.INSERTBEFORE, pos, "</"+expectedElement+">", "</"+foundElement+">");
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
