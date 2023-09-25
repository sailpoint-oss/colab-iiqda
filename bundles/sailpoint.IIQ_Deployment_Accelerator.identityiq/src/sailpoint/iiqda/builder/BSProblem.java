package sailpoint.iiqda.builder;

import org.eclipse.core.resources.IMarker;

public class BSProblem {

	private IMarker problemMarker;
	private String fragmentSource;
	
	public BSProblem(IMarker problemMarker, String fragmentSource) {
	  this.problemMarker=problemMarker;
	  this.fragmentSource=fragmentSource;
  }

	public IMarker getProblemMarker() {
		return problemMarker;
	}

	public String getFragmentSource() {
		return fragmentSource;
	}
	
}
