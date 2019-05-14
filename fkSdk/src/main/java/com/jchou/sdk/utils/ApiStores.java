package com.jchou.sdk.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiStores {

    //https://face.zhiquplus.com/
    //人证识别
    @FormUrlEncoded
    @POST("api/faceid/1.1/oB2XVwfiVZqc54dATEaZub2KPVRPpGiwMpVR9o8pxf//real_auth")
    Observable<JSONObject> faceAuth(@Field("id_no") String id_no, @Field("name") String name,
                                    @Field("image") File image, @Field("trace_id") String trace_id);


    //人脸识别
    @FormUrlEncoded
    @POST("api/faceid/1.1/oB2XVwfiVZqc54dATEaZub2KPVRPpGiwMpVR9o8pxf//real_compare")
    Observable<JSONObject> faceCompare(@Field("image1") File image1,@Field("image2") File image2,
                                       @Field("trace_id") String trace_id);

    //http://api.test.xiangchaopai.com/
    @Multipart
    @POST("cert/font/put")
    Observable<JSONObject> fontPut(@Part MultipartBody.Part file);

    @Multipart
    @POST("cert/back/put")
    Observable<JSONObject> backPut(@Part MultipartBody.Part file);

    @Multipart
    @POST("file/put")
    Observable<JSONObject> filePut(@Part MultipartBody.Part file);

    /**
     * 基本信息提交
     */
    @Headers({"Content-Type: application/json;charset=UTF-8", "Accept: application/json"})
    @POST("base/info/put")
    Observable<JSONObject> baseInfoPut(@Body RequestBody content);



}
