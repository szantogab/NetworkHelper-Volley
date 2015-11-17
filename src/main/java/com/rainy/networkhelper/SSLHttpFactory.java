package com.rainy.networkhelper;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLHttpFactory
{
	static HttpClient client=null;
	static HttpClient httpsClient=null;
	static ClientConnectionManager ccm;

	public static HttpClient createHttpsClient(int timeout)
	{

		if (httpsClient==null || ccm==null)
		{
			KeyStore trustStore;
			
			HttpParams httpParameters = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, timeout);

			try {
				trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	
		        trustStore.load(null, null);
		
		        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
			    SchemeRegistry registry = new SchemeRegistry();
			    registry.register(new Scheme("https", sf, 443));
			    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		        ccm = new ThreadSafeClientConnManager(httpParameters, registry);
		
		        httpsClient=new DefaultHttpClient(ccm, httpParameters);
		        
		        return httpsClient;
	        
			} catch (Exception e) {
				
		        return new DefaultHttpClient(httpParameters);
			}
		}else
		{
			return httpsClient;
		}
	    
	}
	
	private static class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}
}