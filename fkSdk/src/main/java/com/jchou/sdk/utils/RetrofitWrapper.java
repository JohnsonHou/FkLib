package com.jchou.sdk.utils;

import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2016/10/19.
 */
public class RetrofitWrapper {
    private static RetrofitWrapper instance;
    private Retrofit retrofit;
    private static OkHttpClient okHttpClient;
    private static Retrofit build;
    private RetrofitWrapper(){
        //普通式
//        retrofit=new Retrofit.Builder().baseUrl("http://apis.baidu.com").addConverterFactory(GsonConverterFactory.create()).build();

        //rx式
//        HttpLoggingInterceptor httpLoggingInterceptor=new HttpLoggingInterceptor();
//        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpLoggerInterceptor httpLoggingInterceptor=new HttpLoggerInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggerInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor);
        retrofit=new Retrofit.Builder()
                .baseUrl(HttpUrl.parse("http://api.test.xiangchaopai.com/"))
//                .baseUrl(HttpUrl.parse("https://face.zhiquplus.com/"))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .client(builder.build())
                .build();
    }


    //双重检查锁定
    public static RetrofitWrapper getInstance(){
        if(instance==null){
            synchronized (RetrofitWrapper.class){
                if(instance==null) {
                    instance = new RetrofitWrapper();
                }
            }
        }

        return instance;
    }


    public <T>T create(Class<T> service){
        return retrofit.create(service);
    }
}
