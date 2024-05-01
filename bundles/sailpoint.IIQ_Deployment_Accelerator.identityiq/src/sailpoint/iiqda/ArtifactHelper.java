package sailpoint.iiqda;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.input.ReaderInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;

public class ArtifactHelper {


  private static String[] attributesToClean=new String[] {"id", "created", "modified"};
  
  public static String clean(String object) {
    List<String> components = new ArrayList<String>();
    for (String property : attributesToClean) {
      components.add(String.format("(?:\\b%s=\"[^\"]+\")", property));
    }          
    Pattern compiledCleanPattern = Pattern.compile(CoreUtils.join(components, "|"));
    return compiledCleanPattern.matcher(object).replaceAll("");
  }
  
  
  
  public static void writeObject(IIQRESTClient client, IFile file, String type, String name, 
      boolean shouldInsertCDATA, IProgressMonitor monitor) throws ConnectionException, CoreException, IOException {
    String obj=client.getObject(type, name);
    // KCS 2023-12-28
    String checkfor="\r\n";
    int beforeLen=obj.length();
    int afterLen=0;
    while(beforeLen > afterLen) {
      beforeLen=obj.length();
      obj = obj.replace("        "+checkfor,checkfor);
      obj = obj.replace("    "+checkfor,checkfor);
      obj = obj.replace("  "+checkfor,checkfor);
      obj = obj.replace(" "+checkfor,checkfor);
      afterLen=obj.length();
    }
    obj = obj.replace(checkfor+checkfor, checkfor);
    obj = obj.replace(checkfor+checkfor, checkfor);
    // KCS 2023-12-28
    Reader stream = null;

    stream=CoreUtils.stringDocumentAsStream(ArtifactHelper.clean(obj), shouldInsertCDATA);
    Reader  revStream=CoreUtils.doReverseSubstitution(stream, file.getProject());
    if (file.exists()) {
      file.setContents(new ReaderInputStream(revStream, StandardCharsets.UTF_8), true, true, monitor);
    } else {
      file.create(new ReaderInputStream(revStream, StandardCharsets.UTF_8), true, monitor);
    }
    stream.close();
    revStream.close();

  }


}
