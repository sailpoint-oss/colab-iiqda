package sailpoint.iiqda.validation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.eval.IEvaluationContext;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.assist.ExpectComponent;
import sailpoint.iiqda.assist.PreParsedSourceElement;
import sailpoint.iiqda.builder.BSImportDeclaration;
import sailpoint.iiqda.builder.BSProblem;
import sailpoint.iiqda.builder.IIQCodeSnippetRequestor;
import sailpoint.iiqda.builder.SourceElement;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;

public abstract class BaseXMLArtifactParser {

  private static final boolean DEBUG_PARSER = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Parser"));

  public static final String  CLASS_POSTAMBLE            = "\n}";
  public static final String  CLASS_PREAMBLE             = "\nclass ScriptWrapper {\npublic void println(String s){}\n";
  // TODO: do we need to calculate this? Will the CLASS_PREAMBLE change much, if
  // at all?
  public static final int     CLASS_PREAMBLE_LINE_ADJUST = 3;
  public static final String  METHOD_POSTAMBLE           = "\n}";

  private static final String systemFolder               = "system";

  public enum ArtifactType {
    AFTERSCRIPT("AfterScript"),
    ALLOWEDVALUESDEFINITION("AllowedValuesDefinition"),
    APPLICATION("Application"),
    APPROVAL("Approval"),
    ARG("Arg"),
    BUNDLE("Bundle"),
    CAPABILITY("Capability"),
    CHART("Chart"),
    CONDITIONSCRIPT("ConditionScript"),
    CONFIGURATION("Configuration"),
    CORRELATIONCONFIG("CorrelationConfig"),
    DYNAMICSCOPE("DynamicScope"),
    DATASOURCESCRIPT("DataSourceScript"),
    EXTENDEDCOLUMNSCRIPT("ExtendedColumnScript"),
    ENTRY("entry"),
    FIELD("Field"),
    FILTERSCRIPT("FilterScript"),
    FORM("Form"),
    GROUPDEFINITION("GroupDefinition"),
    IDENTITYDASHBOARD("IdentityDashboard"),
    IDENTITYSELECTOR("IdentitySelector"),
    IDENTITYTRIGGER("IdentityTrigger"),
    INTEGRATIONCONFIG("IntegrationConfig"), REFERENCE("Reference"),
    RULE("Rule"),
    TASKDEFINITION("TaskDefinition"),
    TRANSITION("Transition"),
    OBJECTCONFIG("ObjectConfig"),
    OPTIONSSCRIPT("OptionsScript"),
    POLICY("Policy"),
    QUERYSCRIPT("QueryScript"),
    QUICKLINK("QuickLink"),
    RENDERSCRIPT("RenderScript"),
    // SCRIPT ("Script"),
    SECTION("Section"),
    STEP("Step"),
    TARGETSOURCE("TargetSource"),
    TEMPLATE("Template"),
    VALIDATIONSCRIPT("ValidationScript"),
    VALIDATORSCRIPT("ValidatorScript"),
    VARIABLE("Variable"),
    VALUE("value"),
    VALUESCRIPT("ValueScript"),
    WORKFLOW("Workflow"),
    NONE("none");

    private String type;

    private ArtifactType(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }

    public static ArtifactType fromString(String name) {
      for (ArtifactType item : ArtifactType.values()) {
        if (item.getType().equals(name)) {
          return item;
        }
      }
      return NONE;
    }
  }

  private List<IArtifactRootElement>  artifacts;

  private InputStream                 inStream;
  private ArtifactType                type;
  private String                      ruleType;
  private int                         rootElementStartChar;
  private int                         rootElementLine;
  private int                         rootElementEndChar;
  private String                      unparsedType        = null;

  protected boolean                   isWindowsLineEnding = false;
  protected Map<String, List<String>> allTheArtifacts;
  protected Map<String, String>       allTheRules;

  protected void init(InputStream inStream, boolean isCRLF) {
    this.inStream = inStream;
    isWindowsLineEnding = isCRLF;

  }

  public List<IArtifactRootElement> getArtifacts() {
    return artifacts;
  }

  public void dumpArtifacts() {
    for(IArtifactRootElement root: artifacts) {
      System.out.println("---------------");
      int indent=0;
      printElement((IArtifactElement)root, indent);
    }
  }
  
  private void printElement(IArtifactElement el, int indent) {
    for(int i=0;i<indent;i++) {
      System.out.print(" ");
    }
    System.out.println(el.getClass().getName());
    if(el.getChildren()!=null) {
      for(IArtifactElement child: el.getChildren()) {
        printElement(child, indent+2);
      }
    }
  }
  
  public String getStringType() {
    // String type is
    // a) The string representation of the enum ArtifactType (if it is a type
    // that we care about), or
    // b) The name of the root element (for things we don't care about, but want
    // to check the reference for,
    // e.g. CorrelationConfig
    if (type != null) {
      return type.getType();
    }
    return unparsedType;
  }

  public String getRuleType() {
    return ruleType;
  }

  public void parse() throws XMLArtifactParserException {
    parse(false);
  }

  public void parse(boolean endAtRoot) throws XMLArtifactParserException {

    artifacts = new ArrayList<IArtifactRootElement>();

    XMLInputFactory2 fac = (XMLInputFactory2)XMLInputFactory2.newInstance();
    XMLStreamReader2 stream = null;
    fac.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    fac.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
        Boolean.FALSE);
    fac.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    // Doesn't work:
    // fac.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

    // entityReferenceAdjustments=new ArrayList<Point>();

    boolean inArtifact = false;

    try {
      stream = (XMLStreamReader2)fac.createXMLStreamReader(new InputStreamReader(inStream));
      boolean inSource = false;
      int startLine = -1;
      StringBuilder sourceCode = null;
      int startChar = -1;
      boolean isCDATA = false;
      boolean needrootElementEndChar = false;
      boolean needReferenceElementEndChar = false;

      int refStart = -1;
      int referenceElementEndChar = -1;
      int startOfSourceCode = -1;

      ReferenceElement lastReference = null;

      Deque<IArtifactElement> elementStack = new ArrayDeque<IArtifactElement>();

      ArtifactElementFactory elFactory = new ArtifactElementFactory();
      boolean ignoring = false;

      while (stream.hasNext() && ((!endAtRoot) || (!inArtifact && endAtRoot))) {
        int evtCode = stream.next();
        if (needrootElementEndChar) {
          // Unless I can find a way to get the length of the START_ELEMENT..
          int elementEnd = stream.getLocation().getCharacterOffset() - 1;
          ((IArtifactRootElement) elementStack.peek())
          .setElementEndChar(elementEnd);
          needrootElementEndChar = false;
        }
        if (needReferenceElementEndChar) {
          // If the element is self ending, then we get an END_ELEMENT with
          // exactly the same start char.
          // This is not what we want..
          if (stream.getLocation().getCharacterOffset() != (refStart)) {
            // if( !(elementStack.peek() instanceof ReferenceElement) ) {
            // throw new
            // XMLArtifactParserException("Expected Reference on stack: found "+elementStack.peek().getClass().getSimpleName());
            // }
            // Unless I can find a way to get the length of the START_ELEMENT..
            referenceElementEndChar = stream.getLocation().getCharacterOffset();
            lastReference.setEndChar(referenceElementEndChar);
            needReferenceElementEndChar = false;
          }
        }
        if (evtCode == XMLStreamConstants.START_ELEMENT) {

          String elName = stream.getLocalName();
          if (DEBUG_PARSER) {
            IIQPlugin.logDebug("start: " + elName);
          }
          // <Script> only ever contains <Source>. Ignore it.
          if (!("Script".equals(elName)) && !ignoring) {
            // If it's an <Identity>, ignore stuff until we have the </Identity>
            if ("Identity".equals(elName)) {
              ignoring = true;
            } else {
              IArtifactElement el = null;
              if ("Source".equals(elName)) {
                inSource = true;
                sourceCode = new StringBuilder();
                startOfSourceCode = stream.getLocation().getCharacterOffset() + 8; // 8
                // is
                // for
                // <Source>
              } else {
                el = elFactory.getElement(stream);
                if (el instanceof ReferenceElement) {
                  // not sure if I can get the length of the START_ELEMENT so
                  // wait for the next el start
                  refStart = stream.getLocation().getCharacterOffset();
                  needReferenceElementEndChar = true;
                  lastReference = (ReferenceElement) el;
                }
                // Don't do the whole start element tracking, parent structure
                // thing if this element
                // is uninteresting
                if (!(el instanceof UninterestingElement)) {
                  // elementStack.peek() throws EmptyStackException if the stack
                  // is empty
                  // so do try.. catch instead of if..then
                  IArtifactElement parent = getParent(el, elementStack);
                  if (parent == null) {
                    if (DEBUG_PARSER) {
                      IIQPlugin.logDebug("Root name="
                          + stream.getAttributeValue(null, "name"));
                    }
                    if (el instanceof IArtifactRootElement) {
                      ((IArtifactRootElement) el).setStartChar(stream
                          .getLocation().getCharacterOffset());
                      ((IArtifactRootElement) el).setStartLine(stream
                          .getLocation().getLineNumber());
                      needrootElementEndChar = true;
                    }
                  } else {
                    el.setParent(parent);
                    // not needed - setParent adds the child.. parent.addChild(el);
                  }
                }
                elementStack.push(el);

                // TODO: Move to validation of fields
                // fieldIsDefined=isFieldDefined(formFields, fieldName);
                // if(fieldIsDefined) {
                // addMarker(IMarker.SEVERITY_ERROR,
                // "Form Field '"+fieldName+"' already defined",
                // stream.getLocation().getCharacterOffset(),
                // stream.getLocation().getCharacterOffset()+10,
                // stream.getLocation().getLineNumber());
                // }
              }
            }
          }
        } else if ((evtCode == XMLStreamConstants.CDATA || evtCode == XMLStreamConstants.CHARACTERS)
            && inSource && !ignoring) {
          // Don't take CDATA-ness from last CDATA section.. Take it from any
          // text section
          // within the Source section - this covers the case where there is
          // white space before
          // or after the CDATA section.
          isCDATA = (evtCode == XMLStreamConstants.CDATA) || isCDATA;

          String text = stream.getText();
          if (startChar == -1 && startLine==-1) {
            startLine = stream.getLocation().getLineNumber();
          }

          sourceCode.append(text);
        } else if (evtCode == XMLStreamConstants.END_ELEMENT) {
          String elName = stream.getLocalName();
          if (DEBUG_PARSER) {
            IIQPlugin.logDebug("End: " + elName);
          }
          // <Script> only ever contains <Source>. Ignore it.
          // Identities can have references, but we know they are never going to
          // have scripts, so hey - let's ignore them too
          if (!"Script".equals(elName)) {
            if ("Identity".equals(elName)) {
              ignoring = false;
            } else {
              if (!ignoring) {
                if (inSource) {
                  inSource = false;
                  SourceElement se = new SourceElement(startOfSourceCode,
                      startLine, sourceCode.toString(), isCDATA,
                      elementStack.peek());
                  //se.setParent(elementStack.peek());
                  isCDATA = false; // reset for another potential CDATA
                  startChar=-1; 
                  startLine=-1;
                  try {
                    ((IScriptContainerElement) elementStack.peek()).addSource(se);
                  } catch (ClassCastException cce) {
                    throw new XMLArtifactParserException("ClassCastException at or around line "+startLine);
                  }
                } else {
                  // We never pushed Source onto the stack, so we only pop if
                  // it's not source
                  IArtifactElement ae = elementStack.pop();
                  if (ae.getParent() == null
                      && !(ae instanceof UninterestingElement)) {
                    // We need to check if this is an Entry Element
                    // and if so, does it contain a script?
                    // there are a lot of objects that contain maps, objects
                    // that we don't care about
                    // because they never contain scripts.
                    boolean add = true;
                    if (ae instanceof EntryElement) {
                      add = ((EntryElement) ae).hasSource();
                    }
                    if (add) {
                      // We've exhausted the stack, and our 'add' flag is still
                      // true; this must be an actual artifact
                      artifacts.add((IArtifactRootElement) ae);
                      if (DEBUG_PARSER) {
                        IIQPlugin.logDebug("--------------------");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      stream.close();
    } catch (XMLStreamException xmle) {
      IIQPlugin.logException("BaseXMLArtifactParser.parse: XMLException ", xmle);
      throw new XMLArtifactParserException(xmle);
    }

  }

  // private int adjusted(int characterOffset) {
  // int adjust=0;
  // for(Point p: entityReferenceAdjustments) {
  // if ( p.x<characterOffset ) {
  // adjust+=p.y;
  // }
  // }
  // return characterOffset+adjust;
  // }

  private IArtifactElement getParent(IArtifactElement el,
      Deque<IArtifactElement> elementStack) {

    Iterator<IArtifactElement> iter = elementStack.iterator();
    while (iter.hasNext()) {
      IArtifactElement next = iter.next();
      if (!(next instanceof UninterestingElement)) {
        return next;
      }
    }
    return null;
  }

  public int getRootElementStart() {
    return rootElementStartChar;
  }

  public int getRootElementEnd() {
    return rootElementEndChar;
  }

  public int getRootElementLine() {
    return rootElementLine;
  }

  public void validateReferences(IJavaProject jProj) {

    for (IArtifactRootElement root : artifacts) {
      validateReferences((IArtifactElement) root, jProj);
    }

  }

  public void validateReferences(IArtifactElement aEl, IJavaProject jProj) {
    if (aEl instanceof ReferenceElement) {
      ReferenceElement ref = (ReferenceElement) aEl;
      // Try to find a rule resource in the project with the relevant name
      // if not, add a marker
      // TODO: Add a quickfix of "retrieve rule from target"
      String refType = ref.getClassName();
      if (ref.getClassName() == null) {
        createMarker(IMarker.SEVERITY_ERROR, "No reference class specified",
            ref.getStartChar(), ref.getEndChar(), ref.getLine(),
            IIQPlugin.RULE_REFERENCE_INVALID_CLASS);
        return;
      }
      if (!refType.startsWith("sailpoint.object")) {
        refType = "sailpoint.object." + refType;
      }
      try {
        jProj.findType(refType);
      } catch (JavaModelException e) {
        // Couldn't find sailpoint.object.<class>
        createMarker(IMarker.SEVERITY_ERROR,
            "Invalid reference class '" + ref.getClassName(),
            ref.getStartChar(), ref.getEndChar(), ref.getLine(),
            IIQPlugin.RULE_REFERENCE_INVALID_CLASS);
        return;
      }
      IProject project = jProj.getProject();
      String objType = ref.getClassName();
      if (ref.getClassName().startsWith("sailpoint.object")) {
        objType = ref.getClassName().substring(17); // 17=len(sailpoint.object.);
      }

      if (!objType.equals("Identity") && !objType.equals("Bundle")
          && !findReferencedObject(objType, ref.getName(), project)) {
        // Didn't find a rule
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("refName", ref.getName());
        attributes.put("refType", objType);
        attributes.put("project", getProject());
        createMarker(IMarker.SEVERITY_ERROR, "Referenced " + objType + " '"
            + ref.getName() + "' was not found", ref.getStartChar(),
            ref.getEndChar(), ref.getLine(),
            IIQPlugin.RULE_REFERENCE_MISSING_MARKER_TYPE,
            attributes);
      }
    } else {
      for (IArtifactElement child : ((IArtifactElement) aEl).getChildren()) {
        validateReferences(child, jProj);
      }
    }
  }

  public List<IMarker> parseSourceElement(SourceElement srcEl,
      IJavaProject jProj) {

    IProject proj = jProj.getProject();

    if (!srcEl.isCDATA()) {
      // Check if the source goes over an arbitrary minimum number of lines
      // If it does, then we flag a warning
      // If not, then don't bother. Short scripts shouldn't have that overhead.
      // For example
      //
      // return identity.getFirstname();
      //
      // probably doesn't need cdata tags

      int minNumLines = 3;
      try {
        minNumLines = Integer.parseInt(jProj.getResource()
            .getPersistentProperty(
                new QualifiedName("",
                    IIQPreferenceConstants.P_NUM_LINES_BEFORE_CDATA)));
      } catch (NumberFormatException nfe) {
        // don't care. Use Default of 3
      } catch (CoreException e) {
        // don't care. Use Default of 3
      }

      boolean shouldBeCDATA = false;
      int srcLines = 0;
      for (int i = 0; i < srcEl.getSource().length(); i++) {
        if (srcEl.getSource().charAt(i) == '\n') {
          srcLines++;
          if (srcLines > minNumLines) {
            shouldBeCDATA = true;
            break;
          }
        }
      }
      if (shouldBeCDATA) {
        createMarker(IMarker.SEVERITY_WARNING,
            "Consider using <![CDATA[ and ]]> tags around Beanshell",
            srcEl.getStartChar(), srcEl.getStartChar() + srcEl.getLength(),
            srcEl.getStartLine(),
            IIQPlugin.CONSIDER_USING_CDATA_MARKER_TYPE);
      }
    }

    if(DEBUG_PARSER) {
      IIQPlugin.logDebug("SourceElement.."+srcEl.getSource());
    }
    String str = srcEl.getSource();
    String testStr = str.trim();
    boolean needsClosingSemicolon = !(testStr.endsWith(";") || testStr
        .endsWith("}"));

    // Pass #1
    // Identify the imports. Make a note of them. Replace them in the code with
    // spaces to preserve char/line numbering
    PreParsedSourceElement ppse = new PreParsedSourceElement();
    ppse.parseSource(str);
    List<BSImportDeclaration> declarations = ppse.getImportDeclarations();

    // In the snippet, replace the import statements with spaces
    // this takes them out of the snippet compile (since they are really
    // part of K_COMPILATION_UNIT, not the snippet to be evaluated).
    // Replacing with spaces allows us to keep the line/character numbering
    // the same.
    StringBuilder modStr = new StringBuilder(str);
    for (BSImportDeclaration decl : declarations) {
      modStr.replace(decl.getStart(), decl.getStart() + decl.getLength(),
          charString(decl.getLength(), ' '));
      // Put a warning in if this is an on demand (xxx.*) import
      if (decl.isOnDemand()) {
        int startChar = srcEl.getStartChar() + decl.getStart();
        int endChar = srcEl.getStartChar() + decl.getStart() + decl.getLength();
        addMarker(
            getSeverityPreference(jProj.getResource(),
                IIQPreferenceConstants.P_ON_DEMAND_IMPORTS), "On-Demand imports ("
                    + decl.getImport() + ") are discouraged in BeanShell",
                    startChar, endChar, srcEl.getStartLine() + decl.getLineNumber());
      }
    }

    // Pass #2. Figure out if there are any methods. Using the ASTVisitor, get
    // the start character of the first method, and the
    // end character of the *last* method.
    // we will put a class wrapper around all the methods
    // we will put a method wrapper around any remaining code (this will be the
    // body of the code)
    // we'll need to remember these offsets when we are marking up problems
    //
    str = CLASS_PREAMBLE + modStr.toString() + CLASS_POSTAMBLE;

    CompilationUnit blk = compileAST(str);
    MethodCalculatingVisitor visitor = new MethodCalculatingVisitor(str);
    blk.accept(visitor);

    int methodPreAmblePos = visitor.getEndOfLastMethod();
    if(DEBUG_PARSER) {
      IIQPlugin.logDebug("methods end at " + methodPreAmblePos);
    }

    if (methodPreAmblePos == -1)
      methodPreAmblePos = CLASS_PREAMBLE.length();

    // Pass #3. Add an appropriate method signature around everything after the
    // method definitions.
    // Do a compile and get the problems

    boolean needsReturn = srcEl.needsReturn();

    StringBuilder sb = new StringBuilder();
    sb.append("private ");
    sb.append(needsReturn ? getQualifiedReturnType(jProj, srcEl.getReturnType()) : "void");
    sb.append(" scriptWrapper() {\n");
    String method_preamble = sb.toString();

    StringBuilder finalSrc = new StringBuilder(str);
    // Let's insert the rule libraries at the end
    // That way it doesn't mess up our numbering any more than we have to
    // (we don't need to remember how long the rule libraries are)
    // And, if we get any error markers that start *after*
    int libraryPos = str.length() - CLASS_POSTAMBLE.length();
    if (srcEl.scriptReferencesRules()) {
      for (ReferenceElement rule : srcEl.getReferencedRules()) {
        // Try to find a rule resource in the project with the relevant name
        // if not, add a marker
        // TODO: Add a quickfix of "retrieve rule from target"
        String ruleContents = getReferencedRule(rule.getName(), proj);
        if (ruleContents != null) {
          // Parse the rule: we need to get the import declarations and the
          // actual library code separately,
          // then add the imports to the declarations list and stick the source
          // in
          // the final source *after* the main code (to make offset maths easy)
          PreParsedSourceElement ppseRule = new PreParsedSourceElement();
          ppseRule.parseSource(ruleContents);
          List<BSImportDeclaration> ruleDeclarations = ppseRule
              .getImportDeclarations();
          declarations.addAll(ruleDeclarations);
          finalSrc.insert(libraryPos, ppseRule.getSource());
        }
      }
    }
    finalSrc.insert(libraryPos, METHOD_POSTAMBLE); // do last first to avoid
    // computing offset+preamble
    // length
    if (needsClosingSemicolon)
      finalSrc.insert(libraryPos, ";");
    finalSrc.insert(methodPreAmblePos, method_preamble);

    if (DEBUG_PARSER) {
      IIQPlugin.logDebug("After everything, finalSrc=" + finalSrc.toString());
    }

    IEvaluationContext context = jProj.newEvaluationContext();
    // Set the imports
    String[] imports = getImportList(declarations);
    context.setImports(imports);

    // When evaluateCodeSnippet is called, it builds several source files to
    // compile. One of them is obviously
    // the code we send it, but one of the others is called Global_Variables_x
    // (where x is a number) and is evaluated
    // even if there are no global variables defined. All the imports are passed
    // into this compilation unit as well,
    // so if there isn't a global variable that uses the import we get a 'Import
    // not used' problem/marker. Now, the
    // CategorizedProblem that the Eclipse code uses to generate the marker has
    // the class name, but there's no way
    // we can get at it. So, let's just create a global variable for each
    // import. This doesn't impact generating
    // an 'import not used' for unused imports in the code snippet.
    //
    // TODO: Unrecognized imports cause issues with compilation of static
    // variables. Need to find some way to
    // trigger a retry of the compile cycle with the offending import(s) removed
    for (String imp : imports) {
      if (!imp.endsWith("*")) {
        context.newVariable(imp, gvName(imp), null);
      }
    }

    List<Variable> vars = getVariables(jProj.getResource(), srcEl);
    List<String> alreadyDefinedVariables = new ArrayList<String>();
    for (Variable var : vars) {
      if (alreadyDefinedVariables.contains(var.getName())) {
        if(DEBUG_PARSER) {
          IIQPlugin.logDebug("Variable " + var.getName() + " already defined");
        }
      } else {
        boolean isPrimitive = ("boolean".equals(var.getType())
            || "byte".equals(var.getType()) || "char".equals(var.getType())
            || "double".equals(var.getType()) || "float".equals(var.getType())
            || "int".equals(var.getType()) || "long".equals(var.getType()) || "short"
            .equals(var.getType()));
        IType type = null;
        if (!isPrimitive) {
          try {
            type = jProj.findType(var.getType());
          } catch (JavaModelException e) {
            IIQPlugin.logException("JavaModelException ", e);
          }
        }
        if (type == null && !isPrimitive) {
          if (DEBUG_PARSER) {
            IIQPlugin.logDebug("class " + var.getType()
                + " not found for variable " + var.getName());
          }
        } else {
          if (var.getName()!=null) {
            context.newVariable(var.getType(), var.getName(), null);
            alreadyDefinedVariables.add(var.getName());
          }
        }
      }
    }
    Map<String, ExpectComponent> expectedVars = ppse.getExpectedVars();
    if (expectedVars != null) {
      for (String varName : expectedVars.keySet()) {
        // TODO: If there is an expected variable, that we already have defined
        // (e.g. from rule contract),
        // Then make a warning label that this variable is already known, and
        // don't add the expected to the
        // variable list. Double variable definitions break the compile
        
        // Also, if the class doesn't exist, we need to not add the variable, and flag it as an error.
        // Otherwise all the variables and imports break (or at least a lot of unrelated stuff breaks)
        ExpectComponent expectation = expectedVars.get(varName);
        String fqClass=expectation.getFQClass();
        IType iType=null;
        try {
          if (!fqClass.contains(".")) {
            // It's a simple name.. we have to do some testing
            // First, check if it's a java type
            iType=jProj.findType("java.lang."+fqClass);
            if (iType==null) {
              // Next, look through the imports, check all those
              for (String sImport: imports) {
                if (sImport.endsWith("*")) {
                  iType=jProj.findType(sImport.substring(0, sImport.length()-1)+fqClass);
                } else if (sImport.endsWith("."+fqClass)) {
                  iType=jProj.findType(sImport);
                }
                if (iType!=null) {
                  break;
                }
              }
            }
          } else {
            iType=jProj.findType(fqClass);
          }
        } catch (JavaModelException jme) {
          System.out.println(jme);
          // Don't care. For whatever reason, iType will now be null
        }
        if (iType!=null) {
          context.newVariable(iType.getFullyQualifiedName(), varName, null);
        } else {
          addMarker(IMarker.SEVERITY_ERROR,
              "Expected class "+fqClass+" not found on project Classpath",
              srcEl.getStartChar()+expectation.getStart(),
              srcEl.getStartChar()+expectation.getStart()+expectation.getLength(),
              0// stream.getLocation().getLineNumber()); 
          );
        }
      }
    }

    if(DEBUG_PARSER) {
      IIQPlugin.logDebug("------=====-----");
      IIQPlugin.logDebug(finalSrc.toString());
      IIQPlugin.logDebug("------=====-----");
    }

    /*
     * See comments in IIQCodeSnippetRequestor IIQCodeSnippetRequestor requestor
     * = new IIQCodeSnippetRequestor(project, null, new File("d:\\temp")); try {
     * context.evaluateCodeSnippet(str, requestor, null); IJavaElement[]
     * els=context.codeSelect(str, str.indexOf("org.apache"), 70);
     */
    IIQCodeSnippetRequestor requestor = evaluateSource(finalSrc, context);
    if (requestor.hasProblems()) {
      try {
        setMarkers(jProj, srcEl, isWindowsLineEnding,
            requestor.adjustedProblems(CLASS_PREAMBLE, methodPreAmblePos,
                method_preamble), declarations);
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return null;
  }

  private String getQualifiedReturnType(IJavaProject proj, String returnType) {

    // check if it is a sailpoint.object class - otherwise just return the name
    IType theClass;
    try {
      theClass = proj.findType("sailpoint.object."+returnType);
      if(theClass!=null) {
        return "sailpoint.object."+returnType;
      }
    } catch (JavaModelException e) {
      // Do nothing; just return the passed in return type
    }

    return returnType;
  }

  private int getSeverityPreference(IResource resource, String preferenceName) {
    String odType = null;
    try {
      odType = resource.getPersistentProperty(new QualifiedName("",
          preferenceName));
    } catch (CoreException e) {
      e.printStackTrace();
    }
    int sev = 0;
    if (odType == null)
      odType = "error";
    switch (odType) {
      case "warn":
        sev = IMarker.SEVERITY_WARNING;
        break;
      case "none":
        sev = 0;
        break;
      default:
        sev = IMarker.SEVERITY_ERROR;
    }
    return sev;
  }

  public List<Variable> getVariables(SourceElement src) {
    return getVariables(null, src);
  }

  public List<Variable> getVariables(IResource resource, SourceElement src) {
    // TODO Auto-generated method stub
    List<Variable> vars = new ArrayList<Variable>();

    // Add our serilog: I write the plugin, I get to decide what untyped
    // variables are ok!
    vars.add(new Variable("org.apache.commons.logging.Log", "serilog",
        Variable.Source.CONTEXT));

    addDefaultVariables(vars);
    List<Variable> variables = src.getParent().getVariables();
    if (variables != null) {
      vars.addAll(variables);
    }
    if (DEBUG_PARSER) {
      for (Variable var : vars) {
        IIQPlugin.logDebug(var.getType() + " " + var.getName());
      }
    }
    return vars;

  }

  private IIQCodeSnippetRequestor evaluateSource(StringBuilder finalSrc,
      IEvaluationContext context) {
    IIQCodeSnippetRequestor requestor = new IIQCodeSnippetRequestor();
    try {
      context.evaluateCodeSnippet(finalSrc.toString(), requestor, null);
    } catch (JavaModelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return requestor;
  }

  private CompilationUnit compileAST(String str) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    // Map options;
    // parser.setCompilerOptions(options);

    parser.setSource(str.toCharArray());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    final Object cu = parser.createAST(null);
    CompilationUnit blk = (CompilationUnit) cu;
    return blk;
  }

  private String charString(int length, char c) {
    char[] chars = new char[length];
    Arrays.fill(chars, c);
    String s = new String(chars);
    return s;
  }

  private void addTaskMarker(int severity, String message, int charStart,
      int charEnd, int line) {
    createMarker(severity, message, charStart, charEnd, line, IMarker.TASK);
  }

  private void addMarker(int severity, String message, int charStart,
      int charEnd, int line) {
    if (severity != 0) {
      createMarker(severity, message, charStart, charEnd, line,
          IIQPlugin.RULE_PROBLEM_MARKER_TYPE);
    }
  }

  private String[] getImportList(List<BSImportDeclaration> declarations) {
    List<String> imports = new ArrayList<String>();
    // See http://www.beanshell.org/manual/syntax.html , 'Default Imports'
    // section
    imports.add("java.io.*");
    imports.add("java.net.*");
    imports.add("java.util.*");
    // All the SailPointContext.getXXXX methods throw GeneralException. This is
    // normally not caught
    // in BeanShell, but it is a checked exception so it should be (as far as
    // Java is concerned). This
    // results in lots of spurious 'GeneralException cannot be resolved to a
    // type' errors, so let's just
    // add an import for it :)
    imports.add("sailpoint.tools.GeneralException");
    for (BSImportDeclaration decl : declarations) {
      if (!imports.contains(decl.getImport())) {
        imports.add(decl.getImport());
      }
    }
    return imports.toArray(new String[imports.size()]);
  }

  /*
   * Add the default variables (context and log) to the environment This is for
   * Workflow steps, since Rules have them added by the rule registry
   */
  private void addDefaultVariables(List<Variable> vars) {
    vars.add(new Variable("sailpoint.api.SailPointContext", "context",
        Variable.Source.CONTEXT));
    vars.add(new Variable("org.apache.commons.logging.Log", "log",
        Variable.Source.CONTEXT));
  }

  private String gvName(String imp) {
    // Take all the full stops out of a fully qualified class name
    // this is for sticking it into the global variables to avoid unnecessary
    // warnings during the compilation
    // process
    return "scriptWrapper" + imp.replace(".", "");
  }

  private void setMarkers(IJavaProject jProj, SourceElement se, boolean isCRLF,
      List<BSProblem> problems, List<BSImportDeclaration> declarations)
          throws CoreException {

    if (problems != null) {
      for (BSProblem prob : problems) {
        // Don't create a marker for this problem - some errors/warnings
        // in Java are not in BeanShell
        boolean veto = false;

        // If the error occurs outside of the SourceElement (i.e.

        IMarker marker = prob.getProblemMarker();

        // set the defaults
        String msg = (String) marker.getAttribute("message");
        int internalLineNumber = getIntValue(marker, "lineNumber");
        int iMarkerCharStart = getIntValue(marker, "charStart");
        int startChar = se.getStartChar() + iMarkerCharStart;
        int endChar = se.getStartChar() + getIntValue(marker, "charEnd");
        int sev = getIntValue(marker, "severity");
        int line = se.getStartLine() + internalLineNumber;
        if (isCRLF) {
          int crlfAdjust = internalLineNumber - CLASS_PREAMBLE_LINE_ADJUST;
          startChar += crlfAdjust;
          endChar += crlfAdjust;
        }

        // If the marker is past the end of our section in the dummy class, then
        // we don't really care about it
        if (iMarkerCharStart > se.getLength()) {
          continue;
        }

        // modify marker based on certain known types
        String fragment = prob.getFragmentSource();
        switch (getIntValue(marker, "id")) {
          case IProblem.ImportNotFound:
            //msg = "Import not found";
            // Need to re-jig the start and end chars based on where the import
            // is
            // get the import that failed
            // the failed import class is in fragmentSource
            // now find the import that contains the failed import
            for (BSImportDeclaration decl : declarations) {
              if (decl.getImport().contains(fragment)) {
                startChar = se.getStartChar() + decl.getStart();
                if (isCRLF) {
                  startChar+=IIQPlugin.countLF(se.getSource().substring(0, decl.getStart()));
                }
                endChar = startChar + decl.getLength();
              }
            }
            break;
          case IProblem.Task:
            addTaskMarker(sev, msg, startChar, endChar, line);
            // now don't add an error/warning marker
            veto = true;
            break;
          case IProblem.UnusedImport:
            if ("java.io.*".equals(fragment) || "java.net.*".equals(fragment)
                || "java.util.*".equals(fragment)
                || "sailpoint.tools.GeneralException".equals(fragment)) {
              // in Beanshell you get these imported for free - we duplicate
              // this but then if you don't use it the compiler complains.
              veto = true;
            } else if (!se.getSource().contains(fragment)) {
              // This means that the import was declared in one of the
              // referenced Rules
              // So we don't care about it here
              veto = true;
            } else {
              for (BSImportDeclaration decl : declarations) {
                if (decl.getImport().contains(fragment)) {
                  startChar = se.getStartChar() + decl.getStart();
                  endChar = se.getStartChar() + decl.getStart()
                      + decl.getLength();
                  if (isCRLF) {
                    int fragmentAdjust = IIQPlugin.countLF(se
                        .getSource().substring(0, decl.getStart()));
                    startChar += fragmentAdjust;
                    endChar += fragmentAdjust;
                  }
                }
              }
            }
            break;
          case IProblem.UnusedPrivateType:
            // If this error is on a type called 'ScriptWrapper', that's our
            // wrapper.
            // Ignore the error
            if (msg.contains("type ScriptWrapper")) {
              veto = true;
            }
            break;
          case IProblem.UnusedPrivateMethod:
            // If this error is on a method called 'scriptWrapper', that's our
            // wrapper.
            // Ignore the error
            // Actually, ignore it all the time. This is reported when there are
            // methods in Rule Libraries as well
            // if(msg.contains("method scriptWrapper")) {
            veto = true;
            // }
            // TODO: Also ignore this if the artifact is flagged as a rule
            // library
            break;
          case IProblem.IncompatibleTypesInForeach:
            // Non-Parameterized Iterable in foreach. This is ok
          case IProblem.UnusedPrivateField:
            // fields are the external variables that would be passed into the
            // script. We model these as private
            // field variables inside our "class ScriptWrapper" wrapper. This is
            // ok
          case IProblem.UnsafeRawMethodInvocation:
          case IProblem.UnsafeTypeConversion:
          case IProblem.RawTypeReference:
            veto = true;
            break;
          case IProblem.TypeMismatch:
            // Lazy here. If it contains 'from Object' we won't care
            if (msg.contains("from Object")) {
              veto = true;
            }
            break;
            // case IProblem.UnresolvedVariable:
            // ASTParser parser = ASTParser.newParser(AST.JLS4);
            // parser.setSource("org.apache.commons.logging.LogFactory.getLog(\"SERI.Workflow.Importer.getJarData\")".toCharArray());
            // parser.setKind(ASTParser.K_EXPRESSION);
            // parser.setResolveBindings(true);
            // parser.setProject(JavaCore.create(resource.getProject()));
            //
            // final Object cu2 = parser.createAST(null);
            // if (DEBUG_PARSER) CoreActivator.logDebug("cu2="+cu2.getClass().getName());
            // Assignment ass=(Assignment)cu2;
            // Expression rightHandSide = ass.getRightHandSide();
            // if (DEBUG_PARSER) {
            //   CoreActivator.logDebug(rightHandSide);
            //   CoreActivator.logDebug(rightHandSide.resolveTypeBinding());
            // }

            // try to find out what is being assigned to this variable
            // and set a variable for it.
          case IProblem.ParsingErrorInvalidToken:
            // This is what we get if 'void' is discovered. If it is to do with
            // 'void', we skip it
            if (msg != null && msg.contains("\"void\"")) {
              veto = true;
              break;
            }
          case IProblem.UndefinedField:
            if (DEBUG_PARSER) IIQPlugin.logDebug("UndefinedField");
            // start/end chars represent the actual field name
            // previous char will be '.'
            // before that we need to go back to the last non alphanumeric char
            int end = se.getRelativeIndex(endChar);
            if (se.getSource().length() <= end)
              end = se.getSource().length() - 1;
            String fieldName = se.getSource().substring(
                se.getRelativeIndex(startChar), end);
            int varEnd = se.getRelativeIndex(startChar) - 1;
            int varStart = varEnd - 1; // char before the '.'
            while (varStart>0 && Character.isLetterOrDigit(se.getSource()
                .charAt(varStart - 1))) {
              varStart--;
            }
            String varname = se.getSource().substring(varStart, varEnd);
            if (DEBUG_PARSER) IIQPlugin.logDebug("var=" + varname);
            if (hasGetter(se, jProj, varname, fieldName)) {
              // the class of this script/rule arg variable has a related getter
              // this is what beanshell calls 'Convenience Syntax'
              veto = true;
            }
            break;
          case IProblem.UnhandledException:
            String excepClass = msg
            .substring(msg.indexOf("exception type ") + 15);
            if (excepClass.equals("GeneralException")) {
              // silently drop uncaught GeneralExceptions. Although this is a
              // checked exception in Java,
              // in Beanshell we don't really care since we assume the code is
              // good (ha!)
              veto = true;
            }
            sev = getSeverityPreference(jProj.getProject(),
                IIQPreferenceConstants.P_UNHANDLED_EXCEPTIONS);
            break;
          case IProblem.CodeCannotBeReached:
            if (DEBUG_PARSER) IIQPlugin.logDebug("code");
            break;
          case IProblem.ShouldReturnValue:
            // If there is no rule type specified, how do we know what should be
            // returned?
            if (se.getRuleType() == null) {
              veto = true;
            }
            break;
          default:
            if (DEBUG_PARSER) IIQPlugin.logDebug("(" + marker.getAttribute("id") + ") " + msg);
        }
        if (!veto) {
          if (fragment.length() > 20)
            fragment = fragment.substring(0, 20) + "...";
          if (DEBUG_PARSER) IIQPlugin.logDebug("Problem: fragment=" + fragment);
          addMarker(sev, msg, startChar, endChar, line);
        }
      }
    }
  }

  private boolean hasGetter(SourceElement se, IJavaProject jProj,
      String varname, String fieldName) {
    // Look through the variables declared for this source to find the type of
    // the variable
    // Then go look in the project for the relevant class
    // once we have that, look through the methods. Try upper and lower case
    // So, if we get passed 'firstname', we look for methods called:
    // - getlastname
    // - getLastname
    //
    List<Variable> vars = getVariables(jProj.getResource(), se);
    for (Variable v : vars) {
      if (v.getName().equals(varname)) {
        // get the class, and introspect for get<fieldName>() with upper or
        // lowercase first letter
        try {
          IType targetClass = jProj.findType(v.getType());
          if (targetClass == null) {
            if (DEBUG_PARSER) {
              IIQPlugin.logDebug("Unable to find class " + v.getType()
                  + " checking variable " + varname);
            }
            return false;
          }
          String name = "get" + fieldName;
          String altName = "get" + fieldName.substring(0, 1).toUpperCase()
              + fieldName.substring(1);
          IMethod[] methods = targetClass.getMethods();
          for (IMethod meth : methods) {
            if (meth.getElementName().equals(name)
                || meth.getElementName().equals(altName)) {
              return true;
            }
          }
          return false;
        } catch (JavaModelException e) {
          IIQPlugin.logException("Finding class " + v.getType()
              + " checking variable " + varname, e);
          return false;
        }
      }
    }
    return false;
  }

  private int getIntValue(IMarker marker, String string) {
    try {
      Object retval = marker.getAttribute(string);
      return Integer.parseInt(retval.toString());
    } catch (CoreException ce) {
      if (DEBUG_PARSER) IIQPlugin.logDebug("CoreException getting '" + string + "'");
      return -1;
    }
  }

  // Get the project for the current file/doc
  protected abstract IProject getProject();

  // Create a marker with no extra data required
  protected abstract void createMarker(int severity, String message,
      int charStart, int charEnd, int line, String markerType);

  // Create a marker with extra data required
  protected abstract void createMarker(int severity, String message,
      int charStart, int charEnd, int line, String markerType,
      Map<String, Object> attributes);

  protected boolean findReferencedObject(String type, String ruleName,
      IProject container) {
    // Try and find a rule resource in the project with the name of the rule
    // reference here.
    if (allTheArtifacts != null) {
      List<String> allTheObjects = allTheArtifacts.get(type);
      if (allTheObjects != null) {
        boolean hasIt = allTheObjects.contains(ruleName);
        return hasIt;
      }
      return false;
    }
    try {
      SimpleArtifactVisitor srrv = new SimpleArtifactVisitor(type, ruleName,
          IIQPlugin.getExcludedDirectories(container));
      container.accept(srrv);
      if (!srrv.found()) {
        // Get referenced projects of this project
        IProject[] referencedProjects=container.getReferencedProjects();
        srrv = new SimpleArtifactVisitor(type, ruleName, null);
        if(referencedProjects!=null) {
          for(IProject prj: referencedProjects) {
            if(!srrv.found()) {
              prj.accept(srrv);
            }
          }
        }
      }
      return srrv.found();
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException", e);
    }
    return false;
  }

  protected String getReferencedRule(String ruleName, IProject container) {
    // Try and find a rule resource in the project with the name of the rule
    // reference here.
    if (allTheRules != null) {
      String theRule = allTheRules.get(ruleName);
      return theRule;
    }
    // Try and find a rule resource in the project with the name of the rule
    // reference here.
    try {
      RuleVisitor rrv = new RuleVisitor(ruleName);
      container.accept(rrv);
      String ruleContents = rrv.getContents();
      if (ruleContents != null) {
        return ruleContents;
      }
      SystemRuleVisitor srrv = new SystemRuleVisitor(ruleName);
      IFolder fldr = container.getFolder(systemFolder);
      fldr.accept(srrv);
      return srrv.getContents();
    } catch (CoreException e) {
      IIQPlugin.logException("CoreException", e);
    }
    return null;
  }

  // Visitor Classes
  private class MethodCalculatingVisitor extends ASTVisitor {

    private int    startOfFirstMethod = -1;
    private int    endOfLastMethod    = -1;

    private String src;

    public MethodCalculatingVisitor(String src) {
      this.src = src;
    }

    public int getStartOfFirstMethod() {
      return startOfFirstMethod;
    }

    public int getEndOfLastMethod() {
      return endOfLastMethod;
    }

    public boolean visit(MethodDeclaration dec) {
      if (isDeclaringMethod(dec)) {
        if (startOfFirstMethod == -1) {
          startOfFirstMethod = dec.getStartPosition();
          if (DEBUG_PARSER) IIQPlugin.logDebug("Start of first Method (" + dec.getName() + "="
              + startOfFirstMethod);
        }
      }
      // Don't care about the contents..
      return false;
    }

    public void endVisit(MethodDeclaration dec) {
      if (isDeclaringMethod(dec)) {
        int end = dec.getStartPosition() + dec.getLength();
        if (DEBUG_PARSER) IIQPlugin.logDebug("End of Method (" + dec.getName() + "=" + end);
        endOfLastMethod = end;
      }
    }

    private boolean isDeclaringMethod(MethodDeclaration dec) {
      // We need to figure out if the MethodDeclaration is actually a
      // declaration
      // or a method call (that may have been separated from the variable or
      // class
      // by whitespace as a typo)
      //
      // We're going to do this by assuming that after the ')', if the first
      // non-whitespace character
      // is an opening brace or 'throws'
      if (DEBUG_PARSER) IIQPlugin.logDebug("isDeclaringMethod: " + dec.getName());
      int endPos = dec.getStartPosition() + dec.getName().toString().length();
      int closeParen = src.indexOf(')', endPos);
      if (closeParen == -1) {
        // some kind of weirdness in the source
        return false;
      }
      String restOf = src.substring(closeParen + 1);
      String trim = restOf.trim();
      if (trim.startsWith("{") || trim.startsWith("throws")) {
        if (DEBUG_PARSER) IIQPlugin.logDebug("is method declaration");
        return true;
      } else {
        if (DEBUG_PARSER) {
          IIQPlugin.logDebug("Is not method declaration>>"
              + (restOf.length() > 10 ? restOf.substring(0, 10) : restOf) + "<<");
        }
        return false;
      }

    }
  };

  public IArtifactRootElement getArtifact(String artifactName) {
    for (IArtifactRootElement art : artifacts) {
      if (artifactName.equals(art.getName())) {
        return art;
      }
    }
    return null;
  }
}
