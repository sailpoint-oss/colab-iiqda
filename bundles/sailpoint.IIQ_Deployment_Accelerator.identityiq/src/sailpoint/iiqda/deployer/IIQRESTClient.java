package sailpoint.iiqda.deployer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;

import flexjson.JSONDeserializer;
import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.core.IIQDAConstants;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.exceptions.DetailedConnectionException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;

public class IIQRESTClient extends RESTClient {

  private static final boolean DEBUG_REST = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQDAConstants.PLUGIN_ID+"/debug/RESTClient"));
  
  public IIQRESTClient(IProject project, String environment) throws CoreException {
    try {
      this.fProject=project;
      Properties props=new Properties();
      
      IFile secretProps = project.getFile(environment+IIQDAConstants.SECRET_SUFFIX);
      if (secretProps.exists()) {
    	  try (InputStream contents = secretProps.getContents()) {
    		  props.load(contents);
    	  }
      }
      
      if (!props.containsKey(IIQPreferenceConstants.P_URL)) {
          IFile targetProps = project.getFile(environment+IIQDAConstants.TARGET_SUFFIX);
          try (InputStream contents = targetProps.getContents()) {
        	  props.load(contents);
          }
      }

      // Get connection details
      String iUrl = (String)props.get(IIQPreferenceConstants.P_URL);
      String iUsername = (String)props.get(IIQPreferenceConstants.P_USERNAME);
      String iPassword = (String)props.get(IIQPreferenceConstants.P_PASSWORD);
      
      this.timeout=1000;
      try {
        this.timeout=Integer.parseInt(project.getPersistentProperty(
            new QualifiedName("", IIQPreferenceConstants.P_CONNECTION_TIMEOUT)));
      } catch (NumberFormatException nfe) {}

      if(iUrl==null) {
        throw new CoreException(CoreUtils.toErrorStatus("No URL in target environment definition"));
      }
      if(iUsername==null) {
        throw new CoreException(CoreUtils.toErrorStatus("No username in target environment definition"));
      }
      if(iPassword==null) {
        throw new CoreException(CoreUtils.toErrorStatus("No password in target environment definition"));
      }
      this.iUrl=iUrl;
      this.iUsername=iUsername;
      this.iPassword=iPassword;
    } catch (IOException|CoreException e) {
      throw new CoreException(CoreUtils.toErrorStatus("Can't get IIQ preferences "+e));
    }
  }

  public IIQRESTClient(String url, String user, String pass) {
    super(url, user, pass);
  }
  
  public void sendFile(String xml) throws ConnectionException {

    // Generate a REST Client

    Map<String,String> args=new HashMap<String,String>();
    args.put("resource", xml);
    args.put("operation", "Import");

    /*Object response=*/doPost(args);

  }

  public int getJarSize(String jarName) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "jarSize");
    args.put("jar", jarName);

    String response=(String)doPost(args, String.class);
    // getJar returns a b64 encoded string
    // decode to a byte array
    return Integer.parseInt(response);
  }

  public byte[] getJarData(String jarName, int start, int length) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "jarData");
    args.put("jar", jarName);
    args.put("start", Integer.toString(start));
    args.put("length", Integer.toString(length));

    String response=(String)doPost(args, String.class);
    // getJar returns a b64 encoded string
    // decode to a byte array
    byte[] ret=Base64.getDecoder().decode(response);
    return ret;
  }

  public List<String> getObjectTypes() throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getObjectTypes");

    List<String> response=(List<String>)doPost(args, List.class);
    return response;
  }
  
  public byte[] getJarFile(String jarName, IProgressMonitor monitor) throws ConnectionException, InterruptedException {
    int jarSize=getJarSize(jarName);
    int chunksize=409600;
    int nextByte=0;
    byte[] theJar=new byte[jarSize];
    int chunks=jarSize/chunksize;
    monitor.beginTask("Downloading "+jarName+" .. 0/"+jarSize/1024+"k", chunks);
    while(nextByte<jarSize) {
      if(monitor.isCanceled()) {
        throw new InterruptedException();
      }
      byte[] buffer=getJarData(jarName, nextByte, chunksize);
      System.arraycopy(buffer, 0, theJar, nextByte, buffer.length);
      nextByte+=buffer.length;
      monitor.subTask("Downloading "+jarName+" .. "+nextByte/1024+"k/"+jarSize/1024+"k");
      monitor.worked(1);
    }
    monitor.done();

    return theJar;
  }

  public List<String> getRecentObjects(String types, int i) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getLatestObjects");
    args.put("classes", types);
    args.put("maxObjects", Integer.toString(i));

    Object response=doPost(args);
    if(!(response instanceof List)) {
      throw new ConnectionException("getRecentObjects:\nExpected: List\nGot: "+response.getClass().getName());
    }
    return (List<String>)response;
  }

  public List<String> getTasks() throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getTaskList");

    Object response=doPost(args);
    if(!(response instanceof List)) {
      throw new ConnectionException("getTasks:\nExpected: List\nGot: "+response.getClass().getName());
    }
    return (List<String>)response;
  }

  public String runTask(String task) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "runTask");
    args.put("taskName", task);
    Object response=doPost(args);
    if(!(response instanceof String)) {
      throw new ConnectionException("runTask:\nExpected: String\nGot: "+response.getClass().getName());
    }
    return (String)response;
  }

  public Map<String,Object> getTaskResult(String taskId) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getTaskResult");
    args.put("taskId", taskId);
    Object response=doPost(args);
    if(!(response instanceof Map)) {
      throw new ConnectionException("getTaskResult:\nExpected: Map\nGot: "+response.getClass().getName());
    }
    return (Map<String,Object>)response;
  }

  public void terminateTask(String taskId) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "terminateTask");
    args.put("taskId", taskId);
    Object response=doPost(args);
    if(!(response instanceof Map)) {
      throw new ConnectionException("getTaskResult:\nExpected: String\nGot: "+response.getClass().getName());
    }

  }
  
  public List<String> getObjects(String sObjectType) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getObjects");
    args.put("objectType", sObjectType);

    Object response=doPost(args);
    if(!(response instanceof List)) {
      throw new ConnectionException("getObjects:\nExpected: List\nGot: "+response.getClass().getName());
    }
    return (List<String>)response;
  }

  public String getObject(String sObjectType, String sObjectName) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("operation", "getObject");
    args.put("objectType", sObjectType);
    args.put("objectName", sObjectName);

    Object response=doPost(args);
    if(!(response instanceof String)) {
      throw new ConnectionException("getObject:\nExpected: String\nGot: "+response.getClass().getName());
    }
    return (String)response;
  }

  private Object doPost(Map<String,String> args) throws ConnectionException {
    return doPost(args, null);
  }

  private Object doPost(Map<String,String> args, Class expectedClass) throws ConnectionException {
    URI hostUri=null;
    try {
      hostUri=new URI(iUrl+"/");
      hostUri=hostUri.resolve("rest/workflows/Importer/launch");
    } catch (URISyntaxException ue) {
      throw new ConnectionException("Invalid URI: "+iUrl);
    }

    CloseableHttpClient httpclient = getPreEmptiveClient(hostUri, fProject);

    HttpPost post=new HttpPost(hostUri);

    StringEntity entity=null;

    try {
      WorkflowArgsPayload payload=new WorkflowArgsPayload(args);
      entity=payload.getEntity();
      post.setEntity(entity);
      post.setHeader("accept", "application/json");
    } catch (UnsupportedEncodingException ue) {
      throw new ConnectionException("Unsupported Encoding");
    }
    HttpClientContext localContext = HttpClientContext.create();
    try {
      CloseableHttpResponse response = httpclient.execute(post, localContext);
      if(DEBUG_REST) {
        IIQPlugin.logDebug("response="+response.getStatusLine());
      }
      if(response.getStatusLine().getStatusCode()==200) {
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        String output="";
        if(DEBUG_REST) {
          IIQPlugin.logDebug("Output from Server .... \n");
        }
        String line;
        while ((line= br.readLine()) != null) {
          output+=line+"\n";
        }
        if(DEBUG_REST) {
          IIQPlugin.logDebug(output);
        }
        JSONDeserializer<Map<String,Object>> deserializer = new JSONDeserializer<Map<String,Object>>();
        Map<String,Object> retMap= deserializer.deserialize(output);
        // We need the attributes.result string.
        // if it's 'failure', we throw a ConnectionException
        // if it's 'success', we return attributes.payload
        Map<String,Object> attributes=(Map<String,Object>)retMap.get("attributes");
        if(attributes==null) {
          List<String> errors=(List<String>)retMap.get("errors");          
          throw new DetailedConnectionException("No 'attributes' entry in response", errors);          
        }
        String result=(String)attributes.get("result");
        if(result==null) {
          throw new ConnectionException("No 'result' entry in response");          
        }
        Object payload = attributes.get("payload");
        switch(result) {
          case "failure":
            throw new ConnectionException("POST failed: "+payload);
          case "success":
            if(expectedClass!=null) {
              if(!(expectedClass.isInstance(payload))) {
                throw new ConnectionException("getObjectTypes:\nExpected: "+expectedClass.getName()+"\nGot: "+payload.getClass().getName());
              }
            }
            return payload;
          default:
            throw new ConnectionException("POST failed: Unexpected result value '"+result+"'");
        }
      } else {
        throw new ConnectionException("POST Failed: reason="+response.getStatusLine().getStatusCode());
      }

    } catch (ClientProtocolException e) {
      throw new ConnectionException("Connection failed - "+e, e);
    } catch (IOException e) {
      throw new ConnectionException("Connection failed - "+e, e);
    }
  }
  
  public IProject getProject() {
    return this.fProject;
  }

  
}
