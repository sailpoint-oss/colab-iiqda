package sailpoint.iiqda.editors;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.IncrementalHelper;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.SourceElement;
import sailpoint.iiqda.exceptions.XMLArtifactParserException;
import sailpoint.iiqda.validation.IArtifactElement;
import sailpoint.iiqda.validation.IArtifactRootElement;
import sailpoint.iiqda.validation.RuleElement;
import sailpoint.iiqda.validation.XMLIDocumentArtifactParser;
import sailpoint.iiqda.validation.XMLIDocumentValidator;

@SuppressWarnings("restriction")
public class ArtifactValidator implements ISourceValidator, IValidator {

  private IDocument document;
  
  private static final boolean DEBUG_VALIDATOR = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/ArtifactValidator"));

  
  public ArtifactValidator() {
    super();
  }
  
  @Override
  public void cleanup(IReporter arg0) {
    //arg0.removeAllMessages(this);
  }
  @Override
  public void validate(IValidationContext helper, IReporter reporter)
      throws ValidationException {
    if(DEBUG_VALIDATOR) {
      IIQPlugin.logDebug("ArtifactValidator.validate: -skipping ");      
    }
    //		this.lastReporter=reporter;
    //		//reporter.removeAllMessages(this);
    //		this.validate(null, helper, reporter);

  }

  @Override
  public void connect(IDocument document) {
    this.document = document;
  }

  @Override
  public void disconnect(IDocument document) {
    this.document = null;
  }

/*
 * kmj - this version works, but rebuilds every source element when any region is dirty
 * this causes bad performance
  @Override
  public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
    if(document==null) {
      if(DEBUG_VALIDATOR) {
        CoreActivator.logDebug("document=null. Exiting");
      }
      return;
    }
    IncrementalHelper incHelper=null;

    if(helper instanceof IncrementalHelper) {
      incHelper=(IncrementalHelper)helper;
    } else {
      if(DEBUG_VALIDATOR) {
        CoreActivator.logDebug("ArtifactValidator: Helper is not IncrementalHelper, it is "+helper.getClass().getName());
      }
    }
    //TODO: This whole validate/parse/validateReferencedRules chunk is the same as IIQArtifactBuilder
    // Figure out how to merge them.
    XMLIDocumentValidator val=new XMLIDocumentValidator(this, document, reporter);
    try {
      val.validate();
    } catch (CoreException ce) {
      if(DEBUG_VALIDATOR) {
        CoreActivator.logException("CoreException validating Doc", ce);
      }
      // TODO: Do we need to do something with this?
    }
    if(val.hasErrors()) {
      // just return; if there are XML errors we won't be able to figure out where the Source elements are
      return;
    }

    // This is where we just need to parse the BEANSHELL_SOURCE

    try {
      XMLIDocumentArtifactParser p=new XMLIDocumentArtifactParser(this, document, reporter);
      try {
        // TODO: This chunk and 
        p.parse();
        p.validateReferencedRules(incHelper.getProject());
      } catch (XMLArtifactParserException pe) {
        // TODO: Throw up some kind of error message
        return;
      }

      List<SourceElement> elements=p.getSourceElements();
      if(elements!=null) {
        if(DEBUG_VALIDATOR) {
        CoreActivator.logDebug("found "+elements.size()+" source elements");
        }
      }

      // For the relevant source element, do the compile and mark all the problems

      if(elements==null) return;
      for (SourceElement se: elements) {
          List<IMarker> problems=p.parseSourceElement( se, JavaCore.create(incHelper.getProject()) );
      }
    } catch (CoreException ce) {}
  }
}
 */

  @Override
  public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
    if(document==null) {
      if(DEBUG_VALIDATOR) {
        IIQPlugin.logDebug("document=null. Exiting");
      }
      return;
    }
    int offs=dirtyRegion.getOffset();
    int len=dirtyRegion.getLength();
    
    try {
      ITypedRegion[] regions=document.computePartitioning(offs, len);
      for(ITypedRegion region: regions) {
        if(DEBUG_VALIDATOR) {
          IIQPlugin.logDebug("typedRegion="+region.getType());
          IIQPlugin.logDebug("-----------------------------------");
        }
        //		    if(region.getType().equals(CoreActivator.BEANSHELL_SOURCE)) {
        if(region.getType().equals("org.eclipse.wst.xml.XML_CDATA")) {
          IncrementalHelper incHelper=null;
          
          if(helper instanceof IncrementalHelper) {
            incHelper=(IncrementalHelper)helper;
          } else {
            if(DEBUG_VALIDATOR) {
              IIQPlugin.logDebug("ArtifactValidator: Helper is not IncrementalHelper, it is "+helper.getClass().getName());
            }
          }
          //TODO: This whole validate/parse/validateReferencedRules chunk is the same as IIQArtifactBuilder
          // Figure out how to merge them.
          XMLIDocumentValidator val=new XMLIDocumentValidator(this, document, reporter);
          try {
            val.validate();
          } catch (CoreException ce) {
            IIQPlugin.logException("CoreException validating Doc", ce);
            // TODO: Do we need to do something with this?
          }
          if(val.hasErrors()) {
            // just return; if there are XML errors we won't be able to figure out where the Source elements are
            return;
          }
          
          // This is where we just need to parse the BEANSHELL_SOURCE
          
          try {
            XMLIDocumentArtifactParser p=new XMLIDocumentArtifactParser(this, document, reporter);
            try {
              // TODO: This chunk and 
              p.parse();
              p.validateReferences(JavaCore.create(incHelper.getProject()));
            } catch (XMLArtifactParserException pe) {
              // TODO: Throw up some kind of error message
              return;
            }
            
            List<IArtifactRootElement> rootEls=p.getArtifacts();
            for (IArtifactRootElement rootEl: rootEls) {
              if (rootEl instanceof RuleElement) {
                RuleElement re=(RuleElement)rootEl;
                if ( !"beanshell".equals(re.getRuleLanguage()) ) {
                  continue;
                }
              }
              List<SourceElement> elements=((IArtifactElement)rootEl).getSourceElements();
              if(elements!=null) {
                if(DEBUG_VALIDATOR) {
                  IIQPlugin.logDebug("found "+elements.size()+" source elements");
                }
              }
              
              // For the relevant source element, do the compile and mark all the problems
              
              if(elements==null) return;
              for (SourceElement se: elements) {
                if(se.inDirtyRegion(offs,len)) {
                  /*List<IMarker> problems=*/p.parseSourceElement( se, JavaCore.create(incHelper.getProject()) );
                }
              }
            }
          } catch (CoreException ce) {}
        }
      }
    } catch (BadLocationException e) {
      IIQPlugin.logException("Invalid location for dirty Region!", e);
      return;
    }
  }
  
}

