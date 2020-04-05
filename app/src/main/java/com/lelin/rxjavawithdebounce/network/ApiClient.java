package com.lelin.rxjavawithdebounce.network;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static Retrofit retrofit=null;
    private static int REQUEST_TIME_OUT=60;
    private static OkHttpClient okHttpClient;
    public static final String BASE_URL = "https://api.androidhive.info/json/";


    public static Retrofit getClient(){
        if (retrofit==null){
            initOkHttp();
        }

        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    private static void initOkHttp() {

        OkHttpClient.Builder httpClient=new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIME_OUT,TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIME_OUT,TimeUnit.SECONDS);

        HttpLoggingInterceptor interceptor=new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(interceptor);

        httpClient.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request original=chain.request();
                Request.Builder requestBuilder=original.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Request-Type", "Android")
                        .addHeader("Content-Type", "application/json");

                Request request=requestBuilder.build();

                return chain.proceed(request);
            }
        });

    okHttpClient=httpClient.build();
    }

    public static void resetApiClient() {
        retrofit = null;
        okHttpClient = null;
    }
}
