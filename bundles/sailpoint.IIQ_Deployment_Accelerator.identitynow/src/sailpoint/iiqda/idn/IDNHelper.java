package sailpoint.iiqda.idn;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.preferences.IDNPreferenceConstants;
import sailpoint.iiqda.widgets.idn.KeyValuePair;

public class IDNHelper {

  public static void writeObject(IDNRestHandler client, IFile file, String type, String name, 
      IProgressMonitor monitor) throws ConnectionException, CoreException, IOException {
    String obj=client.getObject(type, name);

    // pretty print
    JSONDeserializer de=new JSONDeserializer();
    Object jsonObj=de.deserialize(obj);
    JSONSerializer se=new JSONSerializer().prettyPrint(true);
    se.include("attributes.values");
    obj=se.serialize(jsonObj);
    System.out.println(obj);
    Reader stream = new StringReader(obj);
    
    // TODO: do we need reverse substitution here? It's all based
    // around XPath, IDN is JSON: Reader  revStream=doReverseSubstitution(stream, file.getProject());
    if (file.exists()) {
      file.setContents(new ReaderInputStream(stream, StandardCharsets.UTF_8), true, true, monitor);
    } else {
      file.create(new ReaderInputStream(stream, StandardCharsets.UTF_8), true, monitor);
    }
    stream.close();
    //revStream.close();

  }

  public static List<IDNEnvironment> getEnvironments(IProject project) throws CoreException {
    String prop=project.getPersistentProperty(
        new QualifiedName("", IDNPreferenceConstants.P_IDN_ENDPOINTS));
    return convertStringToEnvironments(prop);
  }
  
  public static List<IDNEnvironment> convertStringToEnvironments(String prop) {
    List<IDNEnvironment> ret=new ArrayList<IDNEnvironment>();
    if (prop!=null) {
      String[] envs=prop.split(",");
      for (String env: envs) {
        String[] parts=env.split("\\|");
        IDNEnvironment iEnv=new IDNEnvironment(parts[0],  parts[1], parts[2], parts[3]);
        ret.add(iEnv);
      }
    
    }
    return ret;
  }

  public static IDNEnvironment getEnvironment(IProject project,
      String environment) throws CoreException {
    // TODO Auto-generated method stub
    List<IDNEnvironment> envs=getEnvironments(project);
    for (IDNEnvironment env: envs) {
      if (env.getName().equals(environment)) {
        return env;
      }
    }
    return null;
  }

  public static String convertEnvironmentstoString(List<IDNEnvironment> environments) {
    // TODO Auto-generated method stub
    StringBuilder bldr=new StringBuilder();
    boolean first=true;
    for (IDNEnvironment env: environments) {
      if (first) {
        first=false;
      } else {
        bldr.append(",");
      }
      bldr.append(env.toString());
    }
    return bldr.toString();
  }


  public static String transformToJSON(Transform transform) {
    GsonBuilder gb=new GsonBuilder()
        .registerTypeHierarchyAdapter(Transform.class, new JsonSerializer<Transform>() {

          @Override
          public JsonElement serialize(Transform src, Type typeOfSrc,
                     JsonSerializationContext context) {

                 JsonObject obj = new JsonObject();
                 if (src.getId()!=null) {
                   obj.addProperty("id", src.getId());
                 }
                 obj.addProperty("type", src.getType());
                 JsonObject attrObj=new JsonObject();
                 if (src.getAttributes()!=null) {
                   for (String key: src.getAttributes().keySet()) {
                     attrObj.add(key, context.serialize(src.getAttribute(key)));
                   }
                 }
                 if (src.getInput()!=null) {
                     attrObj.add("input", context.serialize(src.getInput()));
                 }
                 if (attrObj.entrySet().size()>0) {
                   obj.add("attributes", attrObj);
                 }
                 return obj;
             }
         }
    ).registerTypeAdapter(KeyValuePair.class, new JsonSerializer<KeyValuePair>() {

      @Override
      public JsonElement serialize(KeyValuePair arg0, Type arg1,
          JsonSerializationContext arg2) {
        // TODO Auto-generated method stub
        System.out.println("JsonSerializer<KeyValuePair>.serialize:");
        return null;
      }
      
    });
    gb.setPrettyPrinting();
    Gson gson=gb.create();
    String out=gson.toJson(transform);
    return out;
  }  

}
