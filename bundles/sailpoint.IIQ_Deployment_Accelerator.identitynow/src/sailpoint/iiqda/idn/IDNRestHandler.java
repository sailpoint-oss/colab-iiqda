package sailpoint.iiqda.idn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import sailpoint.iiqda.deployer.RESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.wizards.importresource.idn.IDNObject;

public class IDNRestHandler {

  public class APIPair {
    public String key;
    public String secret;

    public APIPair(String key, String secret) {
      this.key=key;
      this.secret=secret;
    }
  }

  private String organisation;

  private final String url_authDetails="https://{{realm}}.api.identitynow.com/v2/identities/{{username}}/auth-details?org={{realm}}";
  private final String url_orgDetails="https://{{realm}}.api.identitynow.com/v2/org?org={{realm}}";
  private final String url_v1_getObjects="https://{{realm}}.identitynow.com/api/{{objectType}}/list";
  private final String url_v1_getObject="https://{{realm}}.identitynow.com/api/{{objectType}}/get/{{objectName}}";
  private final String url_v1_createObject="https://{{realm}}.identitynow.com/api/{{objectType}}/create";
  private final String url_v1_updateObject="https://{{realm}}.identitynow.com/api/{{objectType}}/update";
  private final String url_validate="https://{{realm}}.api.identitynow.com/v2/identities?limit=1&org={{realm}}";
  private final String url_stepup="https://{{realm}}.identitynow.com/api/user/strongAuthn";
  private final String url_stepupmethods="https://{{realm}}.identitynow.com/api/user/getStrongAuthnMethods";
  private final String url_sendverificationtoken="https://{{realm}}.identitynow.com/api/user/sendVerificationToken";
  private final String url_keyset="https://{{realm}}.identitynow.com/api/client/create";
  private final String CSRF_variable="\"csrfToken\":\"";

  private String ssoServerUrl;

  private String authService;

  private String authEncryption;

  private String failUrl;

  private String CSRFToken;

  private IDNEnvironment env;
  
  public IDNRestHandler(IDNEnvironment env) {
	  System.out.println("This is a test");
	  System.out.println("This is a test");
	  System.out.println("This is a test");
	  System.out.println("This is a test");
	  System.out.println("This is a test");
    this.env=env;
  }

  public String getOrganisation() {
    return organisation;
  }

  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  public void getAuthDetails(RESTClient client, String username) throws ConnectionException {
    StringBuffer buf=new StringBuffer(url_authDetails);    
    String myUrl=StringUtils.replaceEach( url_authDetails, new String[] {"{{realm}}", "{{username}}"}, new String[] {organisation, username});

    Map <String,Object> ret=(Map<String,Object>)client.doGenericGet(myUrl);
    System.out.println("ret="+ret);
    ssoServerUrl=(String)ret.get("ssoServerUrl");
    Map<String,String> auth=(Map<String,String>)ret.get("auth");
    authService=(String)auth.get("service");
    authEncryption=(String)auth.get("encryption");
    failUrl=(String)ret.get("gotoOnFail");
  }

  private String hashCredentials(String username, String password) {
    String salt=username.toLowerCase();
    String sha256hex = DigestUtils.sha256Hex(salt.getBytes());
    String hashed=DigestUtils.sha256Hex(password+sha256hex);
    return hashed;
  }

  public void login(RESTClient client, String username, String password) throws ConnectionException {
    String hashed = hashCredentials(username, password);
    String myUrl=ssoServerUrl+"/login";
    Map<String,String> args=new HashMap<String,String>();
    args.put("service", authService);
    args.put("encryption", authEncryption);
    args.put("IDToken1", username);
    args.put("IDToken2", hashed);
    args.put("realm", organisation);
    args.put("goto",  "https://"+organisation+".identitynow.com/ui");
    args.put("gotoOnFail", failUrl);
    Object ret=client.doGenericFormURLEncodedPost(myUrl, args);

    String html=(String)ret;
    // Search for "csrfToken":"BCTpXylJAmJ126ccOKBEbVCilSu6KO4M"
    //TODO: Check response for stuff related to first login (set alt phone, challenge response etc)
    int csrfTokenVar=html.indexOf(CSRF_variable);
    if (csrfTokenVar==-1) {
    	System.out.println("Response was: "+html);
    	throw new ConnectionException("Login Failed");
    }
    int firstQuote=csrfTokenVar+CSRF_variable.length();
    int secondQuote=html.indexOf("\"", firstQuote+1);
    CSRFToken=html.substring(firstQuote, secondQuote);
    System.out.println("CSRFToken="+CSRFToken);
  }

  // This method is for username and password step up
  public void stepUpAuth(RESTClient client, String username, String password) throws ConnectionException {
    String hashed = hashCredentials(username, password);
    Map<String,String> args=new HashMap<String,String>();
    args.put("password", hashed);
    
    stepUpAuth(client, args);
  }
    
  // This method is for token based step up (send to phone, or email, or whatever)
  public void stepUpAuth(RESTClient client, String token) throws ConnectionException {
    Map<String,String> args=new HashMap<String,String>();
    args.put("token", token);
    
    stepUpAuth(client, args);
  }
  
    
  public void stepUpAuth(RESTClient client, Map<String,String> args) throws ConnectionException  {  
    System.out.println("IDNRestHandler.stepUpAuth: ");
    String myUrl=url_stepup.replace("{{realm}}", organisation);

    //TODO: may need to check for step up methods and show a dialog - this method only works for orgs
    // where 'reenter password' is available. Enterprise510 does not have this
    

    Map<String,String> headers=new HashMap<String,String>();
    headers.put("X-CSRF-Token", CSRFToken);

    Map<String,String> retval=(Map<String,String>)client.doGenericFormURLEncodedPost(myUrl, args, headers);
  }

  public APIPair createAPIKeySet(RESTClient client) throws ConnectionException {
    
    System.out.println("IDNRestHandler.createAPIKeySet: ");

    String myUrl=url_keyset.replace("{{realm}}", organisation);

    Map<String,String> args=new HashMap<String,String>();
    args.put("type", "API");

    Map<String,String> headers=new HashMap<String,String>();
    headers.put("X-CSRF-Token", CSRFToken);

    Map<String,String> retval=(Map<String,String>)client.doGenericFormURLEncodedPost(myUrl, args, headers);

    String apiKey = retval.get("clientID");
    String apiSecret = retval.get("secret");
    APIPair pair=new APIPair(apiKey, apiSecret);
    System.out.println("key: "+apiKey);
    System.out.println("secret: "+apiSecret);

    return pair;
  }
  
  public List<StepUpMethod> getStepUpMethods(RESTClient client) throws ConnectionException{
    
    Map<String,String> headers=new HashMap<String,String>();
    headers.put("X-CSRF-Token", CSRFToken);
    
    String myUrl=url_stepupmethods.replace("{{realm}}", organisation);
    
    List<StepUpMethod> retval=(List<StepUpMethod>)client.doGenericGet(myUrl, headers, List.class);
    
    List<StepUpMethod> methods=new ArrayList<StepUpMethod>();
    for (Object oMeth: retval) {
      Map<String,String> method=(Map<String,String>)oMeth;
      try {
        StepUpMethod stepupmeth=new StepUpMethod(method.get("label"), method.get("description"), method.get("type"), method.get("strongAuthType"));
        methods.add(stepupmeth);
      } catch (UnsupportedStepUpMethodException e) {
        System.out.println("Skipping unsupported step up method "+method.get("description")+" (type="+method.get("type")+")");
      }
    }
    return methods;
  }
  
  public void sendVerificationToken(RESTClient client, StepUpMethod method) throws ConnectionException{
    
    Map<String,String> headers=new HashMap<String,String>();
    headers.put("X-CSRF-Token", CSRFToken);

    Map<String,String> args=new HashMap<String,String>();
    args.put("via", method.getStrongAuthType());
    
    String myUrl=url_sendverificationtoken.replace("{{realm}}", organisation);

    client.doGenericFormURLEncodedPost(myUrl, args, headers);
    
  }
  
  private String basicAuth(String user, String pass) {
    String payload=user+":"+pass;
    
    return Base64.getEncoder().encodeToString(payload.getBytes());
  }
  
  public boolean validateAPICredentials(String apikey, String apisecret) throws ConnectionException {
    
    RESTClient client=new RESTClient(10000);
    String myUrl=StringUtils.replaceEach( url_validate, 
        new String[] {"{{realm}}"},
        new String[] {organisation});
    
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    List retval=(List)client.doGenericGet(myUrl, apikey, apisecret, List.class);

    System.out.println("ret="+retval);
    
    return true;
    
  }

  public Map<String,String> getOrgDetails(String apikey, String apisecret) throws ConnectionException {
    
    RESTClient client=new RESTClient(10000);
    String myUrl=url_orgDetails.replace("{{realm}}", organisation);
    
    Map<String,String> headers=new HashMap<String,String>();
    headers.put("X-CSRF-Token", CSRFToken);
    
    Map<String,String> retval=(Map<String,String>)client.doGenericGet(myUrl, headers, apikey, apisecret);
    
    return retval;
  }

  public List<String> findObjects(String sObjectType, String stub) throws ConnectionException {
    RESTClient client=new RESTClient(10000);
    System.out.println("IDNRestHandler.getObjects:");

    String myUrl=StringUtils.replaceEach( url_v1_getObjects, 
        new String[] {"{{realm}}", "{{objectType}}"},
        new String[] {env.getOrganisation(), sObjectType});
    
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    Object retObj=client.doGenericGet(myUrl, env.getApiKey(), env.getApiSecret());
    List objects=null;
    if (sObjectType.equals("Transform")) {
      objects=(List)((Map)retObj).get("items");
    } else {
      objects=(List)retObj;
    }
    return objects;
//    List<String> retval=new ArrayList<String>();
//    for (Object o: objects) {
//      Map map=(Map)o;
//      String id=(String)map.get("id");
//      if (id!=null) {
//        retval.add(id);
//      } else {
//        System.out.println("found an empty id: "+map);
//      }
//    }
//    System.out.println("ret="+retval);
//    return retval;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<IDNObject> getObjects(String sObjectType, Class clazz) throws ConnectionException {
    RESTClient client=new RESTClient(10000);
    System.out.println("IDNRestHandler.getObjects:");
    
    String myUrl=StringUtils.replaceEach( url_v1_getObjects, 
        new String[] {"{{realm}}", "{{objectType}}"},
        new String[] {env.getOrganisation(), sObjectType});
    
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    Object retObj=client.doGenericGet(myUrl, env.getApiKey(), env.getApiSecret());
    List objects=null;
    if (sObjectType.equals("Transform")) {
      objects=(List)((Map)retObj).get("items");
    } else {
      objects=(List)retObj;
    }
    
    List<IDNObject> retval=new ArrayList<IDNObject>();
    for (Object o: objects) {
      Map map=(Map)o;
      try {
				Constructor con=clazz.getConstructor(Map.class);
				IDNObject obj=(IDNObject) con.newInstance(map);
				retval.add(obj);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    System.out.println("ret="+retval);
    return retval;
  }

  public String getObject(String sObjectType, String sObjectName) throws ConnectionException {
    return getObject(sObjectType, sObjectName, env.getOrganisation(), env.getApiKey(), env.getApiSecret());
  }
  
  public String getObject(String sObjectType, String sObjectName, String organisation, String apikey, String apisecret) throws ConnectionException {
    RESTClient client=new RESTClient(10000);
    System.out.println("IDNRestHandler.getObject:");
    
    String myUrl=StringUtils.replaceEach( url_v1_getObject, 
        new String[] {"{{realm}}", "{{objectType}}", "{{objectName}}"},
        new String[] {organisation, sObjectType, URLEncoder.encode(sObjectName)});
    
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    String retval=(String)client.doGenericGet(myUrl, apikey, apisecret, String.class);
    
    System.out.println("ret="+retval);
    return retval;
  }
  
  public String putObject(String sObjectType, String content, String organisation, String apikey, String apisecret) throws ConnectionException {
    System.out.println("IDNRestHandler.putObject:");
    
    try {
      createObject(sObjectType, content, organisation, apikey, apisecret);
      return "ok";
    } catch (ConnectionException ce) {
      System.out.println("IDNRestHandler.putObject: Create failed: trying update");
      updateObject(sObjectType, content, organisation, apikey, apisecret);
      return "ok";
    }
  }
  
  public String createObject(String sObjectType, String content, String organisation, String apikey, String apisecret) throws ConnectionException {
    RESTClient client=new RESTClient(10000);
    String myUrl=StringUtils.replaceEach( url_v1_createObject, 
        new String[] {"{{realm}}", "{{objectType}}"},
        new String[] {organisation, sObjectType});
    System.out.println("POST: "+myUrl);
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    String retval=(String)client.doGenericJSONPost(myUrl, content, apikey, apisecret, String.class);
    
    System.out.println("ret="+retval);
    return retval;
  }

  public String updateObject(String sObjectType, String content, String organisation, String apikey, String apisecret) throws ConnectionException {
    RESTClient client=new RESTClient(10000);
    String myUrl=StringUtils.replaceEach( url_v1_updateObject, 
        new String[] {"{{realm}}", "{{objectType}}"},
        new String[] {organisation, sObjectType});
    System.out.println("POST: "+myUrl);
//    Map<String,String> headers=new HashMap<String,String>();
//    headers.put("X-CSRF-Token", CSRFToken);
//    
    String retval=(String)client.doGenericJSONPost(myUrl, content, apikey, apisecret, String.class);
    
    System.out.println("ret="+retval);
    return retval;
  }
  
}


