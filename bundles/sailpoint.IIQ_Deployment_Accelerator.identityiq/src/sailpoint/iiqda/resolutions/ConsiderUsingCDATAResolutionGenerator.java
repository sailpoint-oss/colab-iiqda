package sailpoint.iiqda.resolutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.markers.internal.Util;

import sailpoint.iiqda.IIQPlugin;

@SuppressWarnings("restriction")
public class ConsiderUsingCDATAResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    IMarkerResolution[] resolutions=new IMarkerResolution[1];

    resolutions[0]=new SurroundWithCDATAResolution(marker);
    return resolutions;
  }

  public class SurroundWithCDATAResolution extends ResourceModifyingMarkerResolution implements IMarkerResolution {

  	private IMarker originalMarker;
  	
    public SurroundWithCDATAResolution(IMarker originalMarker){
    	this.originalMarker=originalMarker;
    }

    @Override
    public String getLabel() {
      return "Surround Beanshell with <![CDATA[ ]]>";
    }

    @Override
    public void run(IMarker marker) {

      try {
        IFile file = (IFile)marker.getResource();
        ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().
            getActiveWorkbenchWindow().getActivePage(), file, true);
        IDocument doc = editor.getDocumentProvider().
            getDocument(new FileEditorInput(file));

        int end=(Integer)marker.getAttribute("charEnd");
        int start=(Integer)marker.getAttribute("charStart");
        
        // TODO: Reuse this for 'add ]]>' resolution
        String lineDelim=TextUtilities.getDefaultLineDelimiter(doc);
        if("\r\n".equals(lineDelim)) {
          int endOffset=countLF(doc,start, end);
          end+=endOffset;
        }
        doc.replace(end, 0, "]]>");
        String escapedXml=doc.get(start, end-start);
        doc.replace(start, end-start, StringEscapeUtils.unescapeXml(escapedXml));
        doc.replace(start, 0, "<![CDATA[");
      } catch (BadLocationException be) {
        IIQPlugin.logException("Bad Location:",be);
      } catch (CoreException e) {
        IIQPlugin.logException("AddOpenCDATAResolution.run: CoreException", e);
      }
      try {
        marker.delete();
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

		private int countLF(IDocument doc, int start, int end) {
      // TODO Auto-generated method stub
      String region=null;
      try {
        region = doc.get(start, end-start);
      } catch (BadLocationException e) {
        region="";
      }
      return IIQPlugin.countLF(region);
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
			
			List<IMarker> otherMarkers=new ArrayList<IMarker>();
			
			for (IMarker marker: markers) {
				if(!marker.equals(originalMarker)) {
					try {
		        if( marker.getType().equals(IIQPlugin.CONSIDER_USING_CDATA_MARKER_TYPE) ) {
		        	otherMarkers.add(marker);
		        }
	        } catch (CoreException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
	        }
				}
			}
	    return otherMarkers.toArray(new IMarker[otherMarkers.size()]);
    }

		@Override
    public void run(IMarker[] markers, IProgressMonitor monitor) {
			// Order markers: Map of Lists. Key in map is the filename
			// List is an ordered list, ordered by start character, highest first
			// This is so that when we fix multiple markers, we don't mess up the
			// character locations while we're fixing
			Map<String, TreeSet<IMarker>> orderedMarkers=new HashMap<String, TreeSet<IMarker>>();
			
			for(IMarker marker: markers) {
				String resource=marker.getResource().getFullPath().toString();
				TreeSet<IMarker> resMarkers=orderedMarkers.get(resource);
				if(resMarkers==null) resMarkers=new TreeSet<IMarker>(new MarkerLocationComparator());
				resMarkers.add(marker);
				orderedMarkers.put(resource, resMarkers);
			}
			
			// Now they're ordered, run them
			
			for(String key: orderedMarkers.keySet()) {
				TreeSet<IMarker> tmarkers=orderedMarkers.get(key);
				for (IMarker marker: tmarkers) {
					monitor.subTask(Util.getProperty(IMarker.MESSAGE, marker));
					run(marker);
				}
			}
    }
		
  }
  
}
