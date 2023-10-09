package sailpoint.iiqda.deployer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import sailpoint.iiqda.core.CorePlugin;
import sailpoint.iiqda.core.IIQDAConstants;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.ssl.IIQDATrustManager;

@SuppressWarnings("rawtypes")

public class RESTClient {

  private static final boolean DEBUG_REST = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQDAConstants.PLUGIN_ID+"/debug/RESTClient"));

  protected String iUrl;
  protected String iUsername;
  protected String iPassword;
  protected int timeout=1000;
  protected IProject fProject=null;



  private CookieStore cookieStore;

  private HttpClientContext localContext;

  protected boolean bDoSubstOnCmp;

  public enum HTTPMethod {
    GET,
    POST,
    PUT,
    PATCH;
  };

  public RESTClient() {
    this.iUrl=null;
    this.iUsername=null;
    this.iPassword=null;
  }

  public RESTClient(int timeout) {
    this();
    this.timeout=timeout;
  }

  public RESTClient(String url, String user, String pass) {

    this.iUrl=url;
    this.iUsername=user;
    this.iPassword=pass;
  }

  public RESTClient(String url, String user, String pass, int timeout) {
    this(url, user, pass);
    this.timeout=timeout;
  }

  protected CloseableHttpClient getPreEmptiveClient(URI hostUri, IProject hostProject) {

    IFile projectKSFile=null;
    if (hostProject!=null) {
      projectKSFile = hostProject.getFile(".keystore");
    }

    return getPreEmptiveClient(hostUri, iUsername, iPassword, projectKSFile);

  }

  private CloseableHttpClient getPreEmptiveClient(URI hostUri, String username, String password, IFile customTrustStore) {

    CredentialsProvider credsProvider = null;
    HttpHost target=null;

    if (username!=null && password !=null) {
      target=new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());    

      credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
          new AuthScope(target.getHostName(), target.getPort()),
          new UsernamePasswordCredentials(username, password));
    }


    FileInputStream f=null;
    SSLConnectionSocketFactory sslsf=null;
    try {

      KeyStore keystore=KeyStore.getInstance(KeyStore.getDefaultType());
      f=new FileInputStream(System.getProperty("java.home")+"/lib/security/cacerts");
      keystore.load(f, "changeit".toCharArray());

      SSLContext mSSLContextInstance = SSLContext.getInstance("TLS");
      TrustManager trustManager = new IIQDATrustManager(keystore, customTrustStore);
      TrustManager[] tms = new TrustManager[] { trustManager };
      mSSLContextInstance.init(null, tms, new SecureRandom());

      sslsf = new SSLConnectionSocketFactory(mSSLContextInstance);

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } finally {
      try {
        f.close();
      } catch (Exception e) {}
    }

    //    X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
    RequestConfig config = RequestConfig.custom()
        .setSocketTimeout((int) TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.SECONDS))
        .setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.SECONDS))
        .setConnectionRequestTimeout((int) TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.SECONDS))
        .setCircularRedirectsAllowed(true)
        .build();

    HttpClientBuilder bldr=HttpClients.custom()
        .setSSLSocketFactory(sslsf)
        .setDefaultRequestConfig(config)
        .setRedirectStrategy(new LaxRedirectStrategy());
    //   		.setHostnameVerifier(hostnameVerifier)
    if (credsProvider!=null) {
      bldr.setDefaultCredentialsProvider(credsProvider);
    }
    CloseableHttpClient httpclient = bldr.build(); 

    if (target!=null) {
      // Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local
      // auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(target, basicAuth);

      localContext = HttpClientContext.create();
      localContext.setAuthCache(authCache);
    }

    return httpclient;
  }

  public Object doGenericGet(String url) throws ConnectionException {
    return doGenericGet(url, (String)null, (String)null);
  }

  public Object doGenericGet(String url, String username, String password) throws ConnectionException {
    return doGenericGet(url, null, username, password);
  }

  public Object doGenericGet(String url, String username, String password, Class returnType) throws ConnectionException {
    return doGenericGet(url, null, username, password, returnType);
  }

  public Object doGenericGet(String url, Map<String,String> headers, String username, String password) throws ConnectionException {
    return doGenericGet(url, null, username, password, Map.class);
  }    

  public Object doGenericGet(String url, Map<String,String> headers, Class returnType) throws ConnectionException {
    return doGenericGet(url, headers, null, null, returnType);
  }


  public Object doGenericJSONPost(String url, Map<String,String> args, String username, String password) throws ConnectionException {
    return doGenericJSONPost(url, args, username, password, null);
  }
  
  public Object doGenericJSONPost(String url, Map<String,String> args, String username, String password, Class returnType) throws ConnectionException {
    
    JSONSerializer serializer=new JSONSerializer();	      
    String jsonString = serializer.serialize(args);
    return doGenericJSONPost(url, jsonString, username, password, returnType);
  }

  private UrlEncodedFormEntity toFormUrlEncodedContent(Map<String,String> args) throws UnsupportedEncodingException {
    List<NameValuePair> nvc = new ArrayList<NameValuePair>();
    for (Entry<String, String> itm: args.entrySet()) {
      nvc.add(new BasicNameValuePair(itm.getKey(), itm.getValue()));
    }
    return new UrlEncodedFormEntity(nvc);
  }

  public Object doGenericFormURLEncodedPost(String url, Map<String,String> args) throws ConnectionException {
    return doGenericFormURLEncodedPost(url, args, null, null);
  }

  public Object doGenericFormURLEncodedPost(String url, Map<String,String> args, String username, String password) throws ConnectionException {
    return doGenericFormURLEncodedPost(url, args, null, username, password);
  }

  public Object doGenericFormURLEncodedPost(String url, Map<String,String> args, Map<String,String> headers) throws ConnectionException {
    return doGenericFormURLEncodedPost(url, args, headers, null, null);
  }

  public Object doGenericFormURLEncodedPost(String url, Map<String,String> args, Map<String,String> headers, String username, String password) throws ConnectionException {


    if (headers==null) {
      headers=new HashMap<String,String>();
    }

    headers.put("accept", "application/json");
    try {
      UrlEncodedFormEntity entity=toFormUrlEncodedContent(args);
      return doIDNRestCall(HTTPMethod.POST, url, headers, entity, username, password, null);    
    } catch (UnsupportedEncodingException ue) {
      throw new ConnectionException("Unsupported Encoding");
    }

  }

  public Object doGenericJSONPost(String url, String jsonString, String username, String password, Class returnType) throws ConnectionException {

    Map<String, String> headers=new HashMap<String,String>();
    headers.put("Content-Type", "application/json");
    headers.put("accept", "application/json");

    try {
      StringEntity entity=new StringEntity(jsonString);
      return doIDNRestCall(HTTPMethod.POST, url, headers, entity, username, password, returnType);    
    } catch (UnsupportedEncodingException ue) {
      throw new ConnectionException("Unsupported Encoding");
    }


  }

  public Object doGenericGet(String url, Map<String,String> headers, String username, String password, Class returnType) throws ConnectionException {

    if (headers==null) {
      headers=new HashMap<String,String>();
    }
    headers.put("accept", "application/json");

    return doIDNRestCall(HTTPMethod.GET, url, headers, null, username, password, returnType);    

  }

  private Object doIDNRestCall(HTTPMethod method, String url, Map<String,String> headers, HttpEntity entity, String username, String password, Class returnType) throws ConnectionException {

    if (returnType==null) returnType=Map.class; // default to Map (like a JSON structure)
    
    URI hostUri=null;
    try {
      hostUri=new URI(url);
    } catch (URISyntaxException ue) {
      throw new ConnectionException("Invalid URI: "+url);
    }


    CloseableHttpClient httpclient = getPreEmptiveClient(hostUri, username, password, null);

    HttpUriRequest request=null;
    switch (method) {
      case GET:
        request=new HttpGet(hostUri);
        break;
      case POST:
        request=new HttpPost(hostUri);
        break;        
      case PUT:
        request=new HttpPut(hostUri);
        break;        
      case PATCH:
        request=new HttpPatch(hostUri);
        break;
      default:
        throw new ConnectionException("Unknown method '"+method.toString()+" : "+url);
    }

    // Set any headers
    if (headers!=null) {
      for (Entry<String,String> itm: headers.entrySet()) {
        request.setHeader(itm.getKey(), itm.getValue());
      }
    }

    // some kind of payload
    if (entity!=null) {
      ((HttpEntityEnclosingRequest)request).setEntity(entity);
    }
    // make sure we have some kind of context to hold the cookies. Mmmmmm, cooookies
    if (localContext==null) {
      localContext = HttpClientContext.create();
    }
    if (cookieStore!=null) {
      localContext.setCookieStore(this.cookieStore);
    }

    try {
      CloseableHttpResponse response = httpclient.execute(request, localContext);
      cookieStore=localContext.getCookieStore();
      System.out.println("Cookies: "+cookieStore);
      if(DEBUG_REST) {
        CorePlugin.logDebug("response="+response.getStatusLine());
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

      String output="";
      if(DEBUG_REST) {
        CorePlugin.logDebug("Output from Server .... \n");
      }
      String line;
      while ((line= br.readLine()) != null) {
        output+=line+"\n";
      }
      if(DEBUG_REST) {
        CorePlugin.logDebug(output);
      }
      if(response.getStatusLine().getStatusCode()==200) {
        if (response.getEntity().getContentType().getValue().startsWith("application/json")) {
          // Deserialize will not allow us to return JSON as a string - so for this special
          // case we'll skip the deserializing
          Object retMap=output;
          if (returnType!=String.class) {
            JSONDeserializer<Map> deserializer = new JSONDeserializer<Map>();
            // deserializer
            retMap= deserializer.deserialize(output);
            // check return type?
//            if (!(retMap.getClasses()==returnType)){
//              throw new ConnectionException(url+" - Expected return "+returnType.getName()+", got "+retMap.getClass().getName());
//            }
          }
          return retMap;            
        } else if (response.getEntity().getContentType().getValue().startsWith("text/html")) {
          return output;
        } else {
          throw new ConnectionException("Unexpected content type "+response.getEntity().getContentType());
        }
      } else {
        System.out.println("fail: payload=\n"+output);
        throw new ConnectionException(method.toString() + " Failed: reason="+response.getStatusLine().getStatusCode());
      }

    } catch (ClientProtocolException e) {
      throw new ConnectionException("Connection failed - "+e, e);
    } catch (IOException e) {
      throw new ConnectionException("Connection failed - "+e, e);
    }

  }
}

