package com.urbanshef.urbanshefapp;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate () {
        super.onCreate();
//        trustEveryone();
        //handleSSLHandshake();
    }
    /**
     * Enables https connections
     */
//    @SuppressLint("TrulyRandom")
//    public static void handleSSLHandshake() {
//        try {
//            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//
//                @Override
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                @Override
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            }};
//
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String arg0, SSLSession arg1) {
//                    return true;
//                }
//            });
//        } catch (Exception ignored) {
//        }
//    }
//    public static void trustEveryone() {
//        try {
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }});
//            SSLContext context = SSLContext.getInstance("TLS");
//            context.init(null, new X509TrustManager[]{new X509TrustManager(){
//                public void checkClientTrusted(X509Certificate[] chain,
//                                               String authType) throws CertificateException {}
//                public void checkServerTrusted(X509Certificate[] chain,
//                                               String authType) throws CertificateException {}
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }}}, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(
//                    context.getSocketFactory());
//        } catch (Exception e) { // should never happen
//            e.printStackTrace();
//        }
//    }
}
