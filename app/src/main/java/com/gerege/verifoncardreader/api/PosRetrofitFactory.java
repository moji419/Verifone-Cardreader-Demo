package com.gerege.verifoncardreader.api;

import com.gerege.verifoncardreader.pos.PosConstants;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PosRetrofitFactory {

    private static Retrofit retrofit;
    static int time = 60;

    public static Retrofit getInstanceTDB() {

        retrofit = new Retrofit.Builder()
                .baseUrl(PosConstants.getUrl())
                .addConverterFactory(GsonConverterFactory.create(
                        new Gson()
                ))
                .client(getUnsafeOkHttpClient())
                .build();

        return retrofit;
    }

    /**
     * TPTP
     */
    public static Retrofit getInstanceTDBTPTP() {
        int time = 30;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://tdbm.gerege.mn")
                .addConverterFactory(GsonConverterFactory.create(
                        new Gson()
                ))
                .client(getUnsafeOkHttpClient())
                .build();

        return retrofit;
    }

    static class HeaderInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            request = request.newBuilder()
                    .build();
//            if (Auth.isLoggedIn()) {
//                request = request.newBuilder()
//                        .addHeader("Authorization", "Bearer ".concat(Auth.getToken()))
//                        .addHeader("token", Auth.getToken())
//                        .build();
//            } else {
//                request = request.newBuilder()
//                        .build();
//            }


            return chain.proceed(request);
        }
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager


            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.addInterceptor(new HeaderInterceptor());    // HEADER
            builder.connectTimeout(time, TimeUnit.SECONDS);
            builder.writeTimeout(time, TimeUnit.SECONDS);
            builder.readTimeout(time, TimeUnit.SECONDS);

//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            // set your desired log level
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//            builder.addInterceptor(logging);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.equals("tdbp.gerege.mn")
                            || hostname.equals("golomtpay.gerege.mn")
                            || hostname.equals("sbpay.gerege.mn")
                            || hostname.equals("49.0.132.152");
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

