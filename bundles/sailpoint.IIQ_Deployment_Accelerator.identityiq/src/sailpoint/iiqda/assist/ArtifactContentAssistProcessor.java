package sailpoint.iiqda.assist;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorPart;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.BSImportDeclaration;
import sailpoint.iiqda.builder.SourceElement;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.validation.BaseXMLArtifactParser;
import sailpoint.iiqda.validation.IArtifactElement;
import sailpoint.iiqda.validation.IArtifactRootElement;
import sailpoint.iiqda.validation.ReferenceElement;
import sailpoint.iiqda.validation.RuleNotFoundException;
import sailpoint.iiqda.validation.Variable;
import sailpoint.iiqda.validation.XMLIDocumentArtifactParser;
import sailpoint.iiqda.validation.XMLNonValidatingIDocumentArtifactParser;

@SuppressWarnings("restriction")
public class ArtifactContentAssistProcessor implements IContentAssistProcessor {

  private static final boolean DEBUG_CONTENT_ASSIST = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/ContentAssist"));

  public ArtifactContentAssistProcessor(IEditorPart editor){
  }

  @Override
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
      int offset) {

    int modifiedOffset=0;
    int offsetAdjustment=0;
    int preambleInsertionPoint=0;

    
    IDocument doc=viewer.getDocument();

    // Get the project this IDocument's file is in
    ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
    ITextFileBuffer buf=bufferManager.getTextFileBuffer(doc);
    IPath path=buf.getLocation();

    String projectName=path.segment(0);
    if (DEBUG_CONTENT_ASSIST) {
      IIQPlugin.logDebug("I think project is :"+projectName);
    }

    IProject ip=ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    IFile file=ip.getFile("src/tmp.java");

    try {
      if(file.exists()) file.delete(true, null);
      XMLIDocumentArtifactParser p=new XMLNonValidatingIDocumentArtifactParser(doc);
      p.parse();
      p.dumpArtifacts();
      List<SourceElement> srcEls=new ArrayList<SourceElement>();
      for (IArtifactRootElement rEl: p.getArtifacts()) {
        srcEls=((IArtifactElement)rEl).getSourceElements();
        for(SourceElement srcEl: srcEls) {
          if(offset>=srcEl.getStartChar() && offset<=srcEl.getStartChar()+srcEl.getLength()) {
            // Now we need to wrap the source
            // So, get the import declarations (use the AST Parser);
            PreParsedSourceElement ppSrc=new PreParsedSourceElement();
            String sourceCode = srcEl.getSource();
            ppSrc.parseSource(sourceCode);


            StringBuilder modSourceBldr=new StringBuilder();
            modSourceBldr.append(sourceCode);
            modSourceBldr.append(BaseXMLArtifactParser.METHOD_POSTAMBLE); // End of the method signature we are going to put in later

            List<BSImportDeclaration> decs=ppSrc.getImportDeclarations();
            
            preambleInsertionPoint=ppSrc.getEndOfImports();

            /* Now we have the insertion point (after the existing imports)
             * Next we need to insert these (and keep a count of how much we add)
             * Do it in this (reverse) order so we only need to worry about one insertion point
             * - referenced source codes (rules)
             * - method/class preamble
             * - imports for global variables
             * 
             */
            List<ReferenceElement> rules=srcEl.getReferencedRules();          
            List<String> newImports=new ArrayList<String>();
            for(ReferenceElement rule: rules) {
              PreParsedSourceElement ppse=new PreParsedSourceElement();
              try {
                ppse.parseRule(rule.getName(), ip);
                for(BSImportDeclaration bsi: ppse.getImportDeclarations()) {
                  addImport(decs, newImports, bsi);
                }
                modSourceBldr.append("\n");
                modSourceBldr.append(ppse.getSource());
              } catch (RuleNotFoundException e) {
                if (DEBUG_CONTENT_ASSIST) {
                  IIQPlugin.logDebug("Unable to find rule "+rule.getName()+" - some completions may be missing");
                }
              }
            }
            // issue #165
            newImports.add("import java.util.*;");

            // build the preamble          
            StringBuilder sb=new StringBuilder();
            sb.append(BaseXMLArtifactParser.CLASS_PREAMBLE);
            // now put in globals for all the passed in variables
            List<Variable> vars=p.getVariables(srcEl);
            for(Variable var: vars) {
              sb.append(var.getType()+" "+var.getName()+";\n");
            }
            sb.append("private ");
            sb.append(srcEl.needsReturn()?srcEl.getReturnType():"void");
            sb.append(" scriptWrapper() {\n");
            String preamble=sb.toString();

            modSourceBldr.insert(preambleInsertionPoint, preamble);
            offsetAdjustment+=preamble.length();
            for(String newImport: newImports) {
              modSourceBldr.insert(preambleInsertionPoint, newImport+"\n");
              offsetAdjustment+=(newImport.length()+1);
            }
            modSourceBldr.append(BaseXMLArtifactParser.CLASS_POSTAMBLE);

            String modSource = modSourceBldr.toString();
            if (DEBUG_CONTENT_ASSIST) {
              IIQPlugin.logDebug("modSource:\n"+modSource+"\n-------------------");
              IIQPlugin.logDebug("Source Element found");
              IIQPlugin.logDebug("10 chars before cursor are:");
            }
            int relativeOffset=offset-srcEl.getStartChar();
            boolean isCRLF="\r\n".equals(TextUtilities.getDefaultLineDelimiter(doc));
            if(isCRLF) {
              // take a char off the offset for each \n between the start of the source element and the insertion
              // point for the content assist, to account for overshoot
              relativeOffset-=IIQPlugin.countLF(srcEl.getSource().substring(0, relativeOffset));
              // But then add back one for each of the lines specified in the preamble - they are always just \n, not
              // \r\n
              relativeOffset+=BaseXMLArtifactParser.CLASS_PREAMBLE_LINE_ADJUST;
            }
            int relativeStart=relativeOffset-10;
            if(relativeStart<0) relativeStart=0;
            modifiedOffset=relativeOffset;
            // this is to cope with code completions before the preamble (imports)
            if (relativeOffset>preambleInsertionPoint) {
              modifiedOffset+=offsetAdjustment;
            }
            if (DEBUG_CONTENT_ASSIST) {
              IIQPlugin.logDebug(">>"+modSource.substring(relativeStart+offsetAdjustment, relativeOffset+offsetAdjustment)+"<<");
            }
            file.create(new ByteArrayInputStream(modSource.getBytes()), true, null);
            break;
          }
        }
      }

    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XMLArtifactParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    ICompilationUnit blk=JavaCore.createCompilationUnitFrom(file);


    CompletionProposalCollector requestor = new CompletionProposalCollector(blk);
    try {
      // was modifiedOffset
      blk.codeComplete(modifiedOffset, requestor);
      if (DEBUG_CONTENT_ASSIST) {
        IIQPlugin.logDebug("requestor="+requestor);
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }

    if (DEBUG_CONTENT_ASSIST) {
      IIQPlugin.logDebug("ArtifactContentAssistProcessor.computeCompletionProposals:");
    }
    ICompletionProposal[] ret=requestor.getJavaCompletionProposals();
    ICompletionProposal[] newProposals=new ICompletionProposal[ret.length];
    for(int i=0;i<ret.length;i++) {
      //      if(ret[i] instanceof LazyJavaCompletionProposal) {
      //        LazyJavaCompletionProposal prop=(LazyJavaCompletionProposal)ret[i];
      if(ret[i] instanceof AbstractJavaCompletionProposal) {
        AbstractJavaCompletionProposal prop=(AbstractJavaCompletionProposal)ret[i];
        //if (DEBUG_CONTENT_ASSIST) {
        //  CoreActivator.logDebug("replacement="+prop.getReplacementString()+"("+prop.getReplacementLength()+") - relevance=");
        //}

        // Make a new completion proposal, with the original java data, but in an normal CompletionProposal,
        // and updated location
        // replacementString the actual string to be inserted into the document
        // replacementOffset the offset of the text to be replaced
        // replacementLength the length of the text to be replaced
        // cursorPosition the position of the cursor following the insert relative to replacementOffset
        // image the image to display for this proposal
        // displayString the string to be displayed for the proposal
        // contextInformation the context information associated with this proposal
        // additionalProposalInfo the additional information associated with this proposal

        CompletionProposal cp=new CompletionProposal(prop.getReplacementString(), offset-(modifiedOffset-prop.getReplacementOffset()),
            prop.getReplacementLength(),
            prop.getReplacementString().length(),
            prop.getImage(),
            prop.getDisplayString(),
            prop.getContextInformation(), null);
        newProposals[i]=cp;
        // additional info seems to be javadoc (it's html anyway) if (DEBUG_CONTENT_ASSIST) CoreActivator.logDebug((prop.getAdditionalProposalInfo());
      } else {
        if (DEBUG_CONTENT_ASSIST) {
          IIQPlugin.logDebug("proposal was not lazy java ("+ret[i].getClass().getName());
        }
        newProposals[i]=new CompletionProposal("", offset, 0, 0, null, "Unable to modify "+ret[i].getClass().getName(), null, null);
      }
    }
    try {
      file.delete(true, null);
    } catch (Exception e) {}
    //if (DEBUG_CONTENT_ASSIST) {
    //  CoreActivator.logDebug("ret.length="+ret.length);
    //}
    return sortProposals(newProposals);
  }

  private ICompletionProposal[] sortProposals(ICompletionProposal[] props) {

    // Sort the proposals; methods first (in alphabetical order) - identified by () in the displayname
    // then variables/constants

    List<ICompletionProposal> methods=new ArrayList<ICompletionProposal>();
    List<ICompletionProposal> others=new ArrayList<ICompletionProposal>();

    for(ICompletionProposal prop: props) {
      if(prop.getDisplayString().contains("(")) {
        methods.add(prop);
      } else {
        others.add(prop);
      }
    }

    Comparator<ICompletionProposal> comp=new Comparator<ICompletionProposal>() {
      @Override
      public int compare(ICompletionProposal o1, ICompletionProposal o2) {
        return o1.getDisplayString().compareTo(o2.getDisplayString());
      }
    };

    Collections.sort(methods, comp);
    Collections.sort(others, comp);

    ICompletionProposal[] newProps=new ICompletionProposal[props.length];

    ICompletionProposal[] aMethods=methods.toArray(new ICompletionProposal[methods.size()]);
    ICompletionProposal[] aOthers=others.toArray(new ICompletionProposal[others.size()]);

    System.arraycopy(aMethods, 0, newProps, 0, aMethods.length);
    System.arraycopy(aOthers, 0, newProps, aMethods.length, aOthers.length);

    return newProps;
  }

  private void addImport(List<BSImportDeclaration> decs,
      List<String> newImports, BSImportDeclaration bsi) {

    String test=bsi.getImport();
    if (DEBUG_CONTENT_ASSIST) {
      IIQPlugin.logDebug("ArtifactContentAssistProcessor.addImport: ("+test+")");
    }

    // if the declaration isn't in the basic declarations (or one of the other references)
    // we need to add it
    for(BSImportDeclaration dec: decs) {
      if(dec.getImport().equals(test)) return;
    }
    for(String newImport: newImports) {
      if(newImport.equals(test)) return;
    }
    newImports.add(test);
  }

  @Override
  public IContextInformation[] computeContextInformation(ITextViewer viewer,
      int offset) {
    return null;
  }

  @Override
  public char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] {'.'};
  }

  @Override
  public char[] getContextInformationAutoActivationCharacters() {
    return null;
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }

}
