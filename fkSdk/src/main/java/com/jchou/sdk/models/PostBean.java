package com.jchou.sdk.models;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PostBean {

    private Map<String, Object> postMap;


    public PostBean(String[] keys, Object[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("key和value不匹配");
        }
        postMap = new HashMap<>();
        if (keys.length > 0) {

            for (int i = 0; i < keys.length; i++) {
                postMap.put(keys[i], values[i]);
            }
        }

    }


    public RequestBody toJson() {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(postMap));
    }
}