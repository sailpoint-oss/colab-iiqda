package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ScriptElement extends AbstractArtifactElement implements IScriptContainerElement{

//  private static final boolean DEBUG_ELEMENTS = "true".equalsIgnoreCase(Platform
//      .getDebugOption(CoreActivator.PLUGIN_ID+"/debug/Elements"));

  /* The script element is a dummy element
   It always just contains a <Source> element
   So, we need to make it a ScriptContainer

   BUT we don't want to add it into the hierarchy of 'saved' elements
   Or attach the source directly to it

   So, we will override setParent to just maintain a link to the parent
   Ignoring the AbstractArtifactElement's method which also adds a child link
   in the parent

   And, we will attach the source to the parent

   Really we just have this element to maintain the push/pop structure in the parser
   without having to code in some if's in the START_ELEMENT and END_ELEMENT

   */

  @Override
  public boolean needsReturn() {
    return false;
  }

  @Override
  public String getReturnType() {
    return null;
  }

  @Override
  public void addSource(SourceElement se) {
    ((IScriptContainerElement)parent).addSource(se);
  }

  @Override
  public SourceElement getSource() {
    return null;
  }
}