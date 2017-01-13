package cn.asbest.http;

import android.content.Context;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by chenyanlan on 2016/11/4.
 */

public class HttpsHelper {
    private final static HttpsHelper instance = new HttpsHelper();
    private HttpsHelper(){

    }

    /**
     * 获取SSLSocketFactory
     * @param context
     * @param certificates 证书列表，raw下证书源ID, int[] certificates = {R.raw.myssl}
     * @return
     */
    public static SSLSocketFactory getSSLSocketFactory(Context context, int[] certificates) {

        if (context == null) {
            throw new NullPointerException("context == null");
        }

        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            InputStream certificate = null;
            for (int i = 0; i < certificates.length; i++) {
                certificate = context.getResources().openRawResource(certificates[i]);
                keyStore.setCertificateEntry(String.valueOf(i), certificateFactory.generateCertificate(certificate));
            }
            if (certificate != null) {
                certificate.close();
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static HostnameVerifier getHostnameVerifier(final String[] hostUrls) {

        HostnameVerifier TRUSTED_VERIFIER = new HostnameVerifier() {

            public boolean verify(String hostname, SSLSession session) {
                boolean ret = false;
                for (String host : hostUrls) {
                    if (host.equalsIgnoreCase(hostname)) {
                        ret = true;
                        break;
                    }
                }
                return ret;
            }
        };

        return TRUSTED_VERIFIER;
    }
}
