package sailpoint.iiqda.assist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.Name;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.BSImportDeclaration;
import sailpoint.iiqda.validation.RuleNotFoundException;
import sailpoint.iiqda.validation.RuleVisitor;

public class PreParsedSourceElement {

  private List<BSImportDeclaration> importDeclarations;
  private int endOfImports;
  private String source;
  private Map<String,ExpectComponent> expectedVars;
  
  public List<BSImportDeclaration> getImportDeclarations() {
    return importDeclarations;
  }
  
  public int getEndOfImports() {
    return endOfImports;
  }
  
  public void parseRule(String name, IProject project) throws RuleNotFoundException {
    String ruleContents=getReferencedRule(name, project);
    parseSource(ruleContents);
  }

  @SuppressWarnings("unchecked")
  public void parseSource(String contents) {

    importDeclarations=new ArrayList<BSImportDeclaration>();
    endOfImports=0;
    expectedVars=new HashMap<String,ExpectComponent>();
    

    CompilationUnit blk=compileAST(contents);
    // Tried to compile as a compilation unit (java file)
    // This is so we can get the import declarations
    // The rest of the CompilationUnit is useless because it's
    // not in a java class/method framework
    //
    // Just pull the name start and length out of the ImportDeclarations
    List<ImportDeclaration> imps=(List<ImportDeclaration>)blk.imports();
    for (Object imp: imps){
      ImportDeclaration declaration=(ImportDeclaration)imp;
      Name name = declaration.getName();
      int start=declaration.getStartPosition();
      int length=declaration.getLength();
      if(start+length>endOfImports) endOfImports=start+length;

      BSImportDeclaration decl=new BSImportDeclaration(name.toString(), declaration.isOnDemand(), start, length, blk.getLineNumber(start));
      importDeclarations.add(decl);
    }

    // Go through the comments list
    // If it's a line comment, get its contents (startposition + 2 ->startposition+length)
    // If it's an Expect:, get the class and var name
    for (Object comm: blk.getCommentList()) {
      if(comm instanceof LineComment) {
        LineComment lc=(LineComment)comm;
        String comment=contents.substring(lc.getStartPosition()+2, lc.getStartPosition()+lc.getLength());
          
        if( comment.trim().startsWith("Expect: ") ) {
          String[] expectComponents=comment.trim().split(" ");
          if(expectComponents.length>2) {
             expectedVars.put(expectComponents[2], new ExpectComponent(expectComponents[1], lc.getStartPosition()+comment.indexOf("E"), lc.getLength()-comment.indexOf("E")));
          }
        }
      }
    }
    
    source=contents.substring(endOfImports);
    
  }
  
  public Map<String, ExpectComponent> getExpectedVars() {
    return expectedVars;
  }

  protected String getReferencedRule(String ruleName, IProject container) throws RuleNotFoundException {
    // Try and find a rule resource in the project with the name of the rule reference here.
    try {
      RuleVisitor rrv=new RuleVisitor(ruleName);
      container.accept(rrv);
      return rrv.getContents();
    } catch (CoreException e) {
      IIQPlugin.log(Status.ERROR, "getReferencedRule: Couldn't get Rule Contents", e);
    }
    throw new RuleNotFoundException(ruleName, container);
  }
  
  private CompilationUnit compileAST(String str) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    //    Map options;
    //    parser.setCompilerOptions(options);

    parser.setSource(str.toCharArray());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    final Object cu = parser.createAST(null);
    CompilationUnit blk=(CompilationUnit)cu;
    return blk;
  }

  public String getSource() {
    return source;
  }

}
