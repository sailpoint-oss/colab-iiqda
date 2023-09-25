package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import flexjson.JSONDeserializer;
import sailpoint.iiqda.objects.idn.CorrelationConfig;
import sailpoint.iiqda.objects.idn.Health;
import sailpoint.iiqda.objects.idn.Owner;
import sailpoint.iiqda.objects.idn.Source;

public class DeserializeSource {

  public static void main(String[] args) throws Exception {
    
   String filename=args[0];
   File f=new File(filename);
   InputStream is=new FileInputStream(f);
      
   JSONDeserializer json=new JSONDeserializer()
        .use(null, Source.class)
        .use("owner", Owner.class)
        .use("correlationConfig", CorrelationConfig.class)
        .use("health", Health.class)
        ;
    Source theSource=(Source)json.deserialize(new InputStreamReader(is));
    
    System.out.println("Source="+theSource);
  }

}
