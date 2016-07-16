package org.edu.sample.utils;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.*;
import java.io.*;

/**
 * Created by
 * User: vkhodyre
 * Date: 5/19/14
 * <p>
 * <p>
 * <p>
 * Queries:
 * curl -v -k -X GET -x "emea-proxy-pool.eu.alcatel-lucent.com:8000" -H "Authorization: Basic dmtob2R5cmU6VG9kYXkuMTA4" https://aww.crms.bsf.alcatel.fr/cqweb/oslc/repo/CQDB/db/crqms/query/34599875
 * /cqweb/oslc/repo/$db/db/crms/query/$id
 */
public class WebClientConnector {
    public static final String PROXY_HOST = System.getProperty("http.proxyhost");
    public static final int PROXY_PORT = Integer.parseInt(System.getProperty("http.proxyport", "8080"));

    private static Credentials proxyCredentials;

    private static final boolean useProxy = !StringUtils.isBlank(System.getProperty("http.proxyhost"));

    public static InputStream execute(String url, Credentials authinfo) throws IOException {
        HttpClient client = wrapClient(new DefaultHttpClient(), authinfo);
        HttpGet get = makeGetRequest(url, authinfo);
        HttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Unable to access ClearQuest: " + response.getStatusLine());
        }
        InputStream in = new BufferedInputStream(response.getEntity().getContent());
        return in;
    }

    private static HttpGet makeGetRequest(String downloadUrl, Credentials authInfo) {
        HttpGet get = new HttpGet(downloadUrl);
        if (authInfo != null) {
            get.addHeader(new BasicScheme().authenticate(authInfo, "utf-8", false));
        }
        return get;
    }

    private static HttpPut makePutRequest(String downloadUrl, Credentials authInfo) {
        HttpPut put = new HttpPut(downloadUrl);
        if (authInfo != null) {
            put.addHeader(new BasicScheme().authenticate(authInfo, "utf-8", false));
        }
        return put;
    }

    public static HttpClient wrapClient(HttpClient base, Credentials proxyCredentials) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString)
                        throws java.security.cert.CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            X509HostnameVerifier verifier = new X509HostnameVerifier() {

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, java.security.cert.X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                }

                @Override
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(verifier);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
            if (useProxy) {
                HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT, "http");
                base.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            DefaultHttpClient rc = new DefaultHttpClient(ccm, base.getParams());

            if (useProxy && proxyCredentials != null) {
                rc.getCredentialsProvider().setCredentials(new AuthScope(PROXY_HOST, PROXY_PORT),
                        getProxyCredentials(proxyCredentials));
            }
            return rc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static synchronized Credentials getProxyCredentials() {
        return proxyCredentials;
    }

    private static synchronized Credentials getProxyCredentials(Credentials credentials) {
        if (proxyCredentials == null) {
            proxyCredentials = credentials;
        }
        return proxyCredentials;
    }

    public static JSON requestJson(String url, Credentials authinfo) throws IOException {
        HttpClient client = wrapClient(new DefaultHttpClient(), getProxyCredentials());

        HttpGet get = makeGetJsonRequest(url, authinfo);
        StringBuilder result = new StringBuilder();
        InputStream in;
        HttpResponse response = client.execute(get);
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = br.readLine()) != null)
            result.append(line);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() >= 300) throw new IOException("Reason: " + statusLine.getReasonPhrase() +
                " Status Code:" + statusLine.getStatusCode() +
                " Result: " + result.toString());
        return result.length() > 0 ? JSONSerializer.toJSON(result.toString()) : null;
    }

    public static void putRequest(String url, String content, Credentials authinfo) throws IOException {
        HttpClient client = wrapClient(new DefaultHttpClient(), getProxyCredentials());
        HttpPut put = makePutJsonRequest(url, authinfo);
        StringEntity entity = new StringEntity(content);
        put.setEntity(entity);
        StringBuilder result = new StringBuilder();
        HttpResponse response = client.execute(put);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() >= 300) {
            InputStream in;
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = br.readLine()) != null)
                result.append(line);
            throw new IOException("Reason: " + statusLine.getReasonPhrase() +
                    " Status Code:" + statusLine.getStatusCode() +
                    " Result: " + result.toString());
        }
    }

    private static HttpGet makeGetJsonRequest(String downloadUrl, Credentials authInfo) {
        HttpGet get = makeGetRequest(downloadUrl, authInfo);
        get.addHeader("Accept", "application/json");
        return get;
    }

    private static HttpPut makePutJsonRequest(String downloadUrl, Credentials authInfo) {
        HttpPut put = makePutRequest(downloadUrl, authInfo);
        put.addHeader("Content-Type", "application/json");
        return put;
    }
}
