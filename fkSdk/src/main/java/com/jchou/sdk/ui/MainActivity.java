package com.jchou.sdk.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jchou.sdk.R;
import com.jchou.sdk.SdkManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void getLocation(View v){
//        SdkManager.getInstance().getLocation(this, new SdkManager.LocationListener() {
//            @Override
//            public void locationEnd(double longitude, double latitude) {
//            }
//
//            @Override
//            public void locationError() {
//
//            }
//        });
//        Log.e("jc",SdkManager.getInstance().getMacAddress(this));
        SdkManager.getInstance().liveAuthen(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SdkManager.REQUEST_AUTH&&resultCode==RESULT_OK){
            Log.e("jc","onActivityResult:ok");
        }
    }
}
