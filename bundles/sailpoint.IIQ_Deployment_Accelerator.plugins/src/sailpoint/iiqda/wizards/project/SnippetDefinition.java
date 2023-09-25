package sailpoint.iiqda.wizards.project;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class SnippetDefinition extends AbstractModelObject {
  
  private String regex;
  private String rightRequired;
  private List<JSInclude> scriptIncludes;
  private List<CSSInclude> cssIncludes;
  public String getRegex() {
    return regex;
  }
  public void setRegex(String regex) {
    this.regex = regex;
  }
  public String getRightRequired() {
    return rightRequired;
  }
  public void setRightRequired(String rightRequired) {
    this.rightRequired = rightRequired;
  }
  public List<JSInclude> getScriptIncludes() {
    return scriptIncludes;
  }
  public void setScriptIncludes(List<JSInclude> scriptIncludes) {
    this.scriptIncludes = scriptIncludes;
  }
  public void setScriptIncludes(JSInclude[] scriptIncludes) {
    List<JSInclude> scripts=Arrays.asList(scriptIncludes);
    this.scriptIncludes=scripts;
  }
  public List<CSSInclude> getCssIncludes() {
    return cssIncludes;
  }
  public void setCssIncludes(List<CSSInclude> cssIncludes) {
    this.cssIncludes = cssIncludes;
  }
  public void setCssIncludes(CSSInclude[] cssIncludes) {
    List<CSSInclude> css=Arrays.asList(cssIncludes);
    this.cssIncludes=css;
  }
  
  public static abstract class Include {
    
    private String sourceFile;
    private String destFile;
    private Properties replace;
    
    public abstract String getIncludeType();

    public Include(String sourceFile, String destFile) {
      this(sourceFile, destFile, null);
    }
    
    public Include(String sourceFile, String destFile, Properties replace) {
      this.sourceFile=sourceFile;
      this.destFile=destFile;
      this.replace=replace;
    }
    
    public String getSourceFile() {
      return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
      this.sourceFile = sourceFile;
    }

    public String getDestinationFile() {
      return destFile;
    }

    public void setDestFile(String destFile) {
      this.destFile = destFile;
    }

    public Properties getReplace() {
      return replace;
    }

    public void setReplace(Properties replace) {
      this.replace = replace;
    }
    
  }
  
  public static class JSInclude extends Include {
    
    public JSInclude(String sourceFile, String destFile) {
      super(sourceFile, destFile);
    }
    
    public JSInclude(String sourceFile, String destFile, Properties replace) {
      super(sourceFile, destFile, replace);
    }

    public String getIncludeType() {
      return "js";
    }
  }
  
  public static class CSSInclude extends Include {

    public CSSInclude(String sourceFile, String destFile) {
      super(sourceFile, destFile);
    }
    
    public CSSInclude(String sourceFile, String destFile, Properties replace) {
      super(sourceFile, destFile, replace);
    }
    
    public String getIncludeType() {
      return "css";
    }
  }
  
}
