package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import flexjson.ClassLocator;
import flexjson.JSONDeserializer;
import flexjson.ObjectBinder;
import flexjson.Path;
import sailpoint.iiqda.objects.idn.Transform;

public class DeserializeTransform {
  
  private static class MyLocator implements ClassLocator {

    @Override
    public Class locate(ObjectBinder context, Path currentPath)
        throws ClassNotFoundException {
      // TODO Auto-generated method stub
      System.out.println("ClassLocator.locate:");
      return null;
    }
    
  }

  public static void main(String[] args) throws Exception {
    
   String filename=args[0];
   File f=new File(filename);
   InputStream is=new FileInputStream(f);
      
   JSONDeserializer json=new JSONDeserializer()
        .use(null, Transform.class)
        ;
    Transform theTransform=(Transform)json.deserialize(new InputStreamReader(is));
    
    System.out.println("Transform="+theTransform);
    for (Entry<String, Object> x: theTransform.getAttributes().entrySet()) {
      System.out.println(x.getKey()+" = "+x.getValue().getClass().getName());
    }
    System.out.println("hasInput: "+theTransform.hasInput());
  }

}
