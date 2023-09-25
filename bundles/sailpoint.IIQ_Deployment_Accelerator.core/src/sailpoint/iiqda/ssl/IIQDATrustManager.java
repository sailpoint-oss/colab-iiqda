package sailpoint.iiqda.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.X509TrustManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.core.CorePlugin;
import sailpoint.iiqda.core.CoreUtils;

public class IIQDATrustManager implements X509TrustManager {

  private static final boolean DEBUG_TRUST = "true".equalsIgnoreCase(Platform
      .getDebugOption(CoreUtils.PLUGIN_ID+"/debug/TrustManager"));
  
  private KeyStore keystore;
  private IFile fKSFile;
  
	public IIQDATrustManager(KeyStore keystore, IFile projectKSFile) {
		this.keystore=keystore;
		this.fKSFile=projectKSFile;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate,
			String paramString) throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certChain,
			String paramString) throws CertificateException {
		X509Certificate cert=certChain[certChain.length-1];
		boolean hasCert=false;
		hasCert=hasCert(cert, keystore);
		if(!hasCert) {
			if(fKSFile.exists()) {
					try {
	          KeyStore projectKeystore=KeyStore.getInstance(KeyStore.getDefaultType());
	          InputStream is=fKSFile.getContents();
	          projectKeystore.load(is, "changeit".toCharArray());
	          hasCert=hasCert(cert, projectKeystore);
          } catch (KeyStoreException | NoSuchAlgorithmException | CoreException
              | IOException e) {
	          throw new CertificateException("Error checking project keystore "+e);
          }
			}
		}
		if(!hasCert) {
			queryUserForAcceptance(cert, fKSFile);
		}
	}

	private boolean hasCert(X509Certificate cert, KeyStore keystore) {
		boolean hasCert=false;
		try {
			Enumeration<String> aliases=keystore.aliases();
			while (aliases.hasMoreElements() && !hasCert) {
				String alias=aliases.nextElement();
				try {
					cert.verify(keystore.getCertificate(alias).getPublicKey());
					hasCert=true;
					break;
				} catch (Exception e) {}
			}
    } catch (KeyStoreException e) {
	    CorePlugin.logException("Keystore exception", e);
    }
		return hasCert;
	}
	
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	private void queryUserForAcceptance(X509Certificate cert, IFile destKSFile) throws CertificateException {
	  
		CertQuery certQuery = new CertQuery(cert);
		PlatformUI.getWorkbench().getDisplay().syncExec(certQuery);

		int status = certQuery.getStatus();
		if (DEBUG_TRUST) {
      CorePlugin.logDebug("status="+status);
		}
		switch(status) {
			case 0:
				return;
			case 1:
				saveApprovedCert(cert);
				return; // TODO: Store this cert
			case 2:
			default:
				throw new CertificateException("not in keystore");
		}
	}

	private void saveApprovedCert(X509Certificate cert)
      throws CertificateException {
	  try {
	  	KeyStore projectKeystore=KeyStore.getInstance(KeyStore.getDefaultType());
	  	if(fKSFile.exists()) {
	  			try {
	          InputStream is=fKSFile.getContents();
	          projectKeystore.load(is, "changeit".toCharArray());
	        } catch (NoSuchAlgorithmException | CoreException | IOException e) {
	          e.printStackTrace();
	        }
	  	} else {
	  		projectKeystore.load(null, null);
	  	}
	  	projectKeystore.setCertificateEntry("cert"+System.currentTimeMillis(), cert);
	  	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	  	projectKeystore.store(baos, "changeit".toCharArray());
	  	byte[] contents=baos.toByteArray();
	  	if (fKSFile.exists()) {
	  		fKSFile.setContents(new ByteArrayInputStream(contents), IResource.FORCE, null);
	  	} else {
	  		fKSFile.create(new ByteArrayInputStream(contents), IResource.FORCE, null);	  		
	  	}
	  } catch (KeyStoreException |NoSuchAlgorithmException 
	  		|IOException |CoreException e) {
	    throw new CertificateException("Error writing new cert to keystore: "+e);
	  }
  }

	private final class CertQuery implements Runnable {
	  private final X509Certificate last;
    private int status;
    
	  private CertQuery(X509Certificate last) {
		  this.last = last;
	  }

	  public void run() {
      
      Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	    
	    String dlgTitle="Untrusted CA Cert";
	    String dlgMessage="Trust this Certificate?\n\n"+
	    		"Issuer: "+last.getIssuerDN()+"\n"+
	    		"Valid from: "+last.getNotBefore()+"\n"+
	    		"Valid to: "+last.getNotAfter()+"\n";
	    String[] dlgButtons={ "Just Once", "Always", "No" };
	    MessageDialog dlg=new MessageDialog(activeShell, dlgTitle, null, 
	    		dlgMessage, MessageDialog.QUESTION, dlgButtons, 2);
	    status=dlg.open();
	    if (DEBUG_TRUST) {
        CorePlugin.logDebug("status="+status);
	    }
	  }
	  
	  public int getStatus() {
	  	return status;
	  }
  }
}
