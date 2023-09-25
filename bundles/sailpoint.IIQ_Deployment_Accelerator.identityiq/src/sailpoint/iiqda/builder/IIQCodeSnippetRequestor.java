package sailpoint.iiqda.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;

import sailpoint.iiqda.IIQPlugin;

public class IIQCodeSnippetRequestor /*extends LocalEvaluationEngine */implements ICodeSnippetRequestor {


//  private class MyClassLoader extends ClassLoader {
//
//    public Class<?> getClass(String name, byte[] b) {
//      return defineClass(name, b, 0, b.length);
//    }
//
//  }

  private List<BSProblem> problems;

  public IIQCodeSnippetRequestor() {
    problems=new ArrayList<BSProblem>();
  }

  public boolean hasProblems() {
    return (problems.size()>0);
  }

  /*
   * The problems we have will be offset since we take the snippet, and add
   * class A {
   * }
   * Around the whole thing.
   */
  public List<BSProblem> adjustedProblems(String classPreamble, int methodPreambleLoc, String methodPreamble) {
    List<BSProblem> probs=new ArrayList<BSProblem>();
    for(BSProblem prob: problems) {
      int startChar = prob.getProblemMarker().getAttribute("charStart", -1);
      int line = prob.getProblemMarker().getAttribute("lineNumber", -1);
      int endChar = prob.getProblemMarker().getAttribute("charEnd", -1);
      if(startChar>methodPreambleLoc) {
        startChar-=methodPreamble.length();
        line--;
        endChar-=methodPreamble.length();
      }
      // Now take the Class preamble off the attributes. This is always there no matter whether the marker
      // is after the new method preamble or not
      startChar-=classPreamble.length();
      endChar-=classPreamble.length();
      line--;
      
      try {
        prob.getProblemMarker().setAttribute("charStart", startChar);
        prob.getProblemMarker().setAttribute("charEnd", endChar);
        prob.getProblemMarker().setAttribute("lineNumber", line);
      } catch (CoreException e) {
        IIQPlugin.logException("CoreException updating problemMarker: ", e);
      }
      probs.add(prob);
    }
    return probs;
  }
  
  public List<BSProblem> getProblems() {
    return problems;
  }

  @Override
  public boolean acceptClassFiles(byte[][] classFileBytes,
      String[][] classFileCompoundNames, String codeSnippetClassName) {
    //    CoreActivator.logDebug("acceptClassFiles: "+codeSnippetClassName);
    //    MyClassLoader cl=new MyClassLoader();
    //    Class cla=cl.getClass("org.eclipse.jdt.internal.eval.target.CodeSnippet", classFileBytes[0]);
    //    Field[] fld=cla.getDeclaredFields();
    //    Method[] met=cla.getDeclaredMethods();
    //    try {
    //      Field privateField=cla.getDeclaredField("x");
    //    } catch (Throwable t) {
    //      CoreActivator.logException("can't get declared field x", t);
    //    }
    return true;
  }

  @Override
  public void acceptProblem(IMarker problemMarker, String fragmentSource,
      int fragmentKind) {
    problems.add(new BSProblem(problemMarker, fragmentSource));
//logDebug("id="+problemMarker.getAttribute("id", -1));
//logDebug("message="+problemMarker.getAttribute("message", "***"));
//logDebug("---");

  }


  /* This is something I was working on for code completion
   * I may come back to this later, but put to one side for getting "method in rule" functionality working
   * Should have branched! 

	private List<BSProblem> problems;
	public IIQCodeSnippetRequestor2(IJavaProject project, IJavaDebugTarget vm, File directory) {
	  super(project, vm, directory);
		problems=new ArrayList<BSProblem>();
	}

	public boolean hasProblems() {
		return (problems.size()>0);
	}

	public List<BSProblem> getProblems() {
		return problems;
	}

	@Override
	public void acceptProblem(IMarker problemMarker, String fragmentSource,
			int fragmentKind) {
		logdebug("acceptProblem: "+fragmentSource);
		problems.add(new BSProblem(problemMarker, fragmentSource));
		try {
			for(String key: problemMarker.getAttributes().keySet()) {
				logdebug(key+"="+problemMarker.getAttribute(key, "***"));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   */

}

