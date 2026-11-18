package io.student.rococo.service.api;

import io.qameta.allure.okhttp3.AllureOkHttp3;
import io.student.rococo.api.core.ThreadSafeCookieStore;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RestClient {

    private final Retrofit retrofit;

    public RestClient(String baseUrl, boolean followRedirect, Interceptor... interceptors) {
        this(baseUrl, followRedirect, JacksonConverterFactory.create(),
                HttpLoggingInterceptor.Level.HEADERS, interceptors);
    }

    public RestClient(String baseUrl,
                      boolean followRedirect,
                      retrofit2.Converter.Factory converterFactory,
                      HttpLoggingInterceptor.Level level,
                      Interceptor... interceptors) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .cookieJar(ThreadSafeCookieStore.INSTANCE)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(followRedirect)
                .followSslRedirects(followRedirect);

        httpClient.addNetworkInterceptor(
                new AllureOkHttp3()
                        .setRequestTemplate("http-request.ftl")
                        .setResponseTemplate("http-response.ftl")
        );

        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                httpClient.addInterceptor(interceptor);
            }
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(level);
        httpClient.addInterceptor(logging);

        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(converterFactory)
                .build();
    }

    protected <T> T create(Class<T> service) {
        return retrofit.create(service);
    }
}