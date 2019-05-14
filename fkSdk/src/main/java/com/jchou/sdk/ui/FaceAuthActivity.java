package com.jchou.sdk.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.jchou.sdk.R;
import com.jchou.sdk.utils.ApiStores;
import com.jchou.sdk.utils.ImgUtil;
import com.jchou.sdk.utils.RetrofitWrapper;
import com.jchou.sdk.view.CustomDialog;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.io.File;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class FaceAuthActivity extends AppCompatActivity implements View.OnClickListener{


    private CustomDialog mProgressDialog;

    ImageView iv;

    private String imgUrl = "";
    private Uri cameraUri;
    private File mFile1;

    TextView tvTitle;
    EditText etName;
    EditText etId;

    private String traceId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);

        iv = findViewById(R.id.iv);
        findViewById(R.id.iv).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        etId = findViewById(R.id.et_id);
        tvTitle.setText("人证识别");

        //camera
        File file = ImgUtil.getTempPhoto(getApplicationContext());
        imgUrl = file.getPath();
        cameraUri = ImgUtil.getUriForFile(getApplicationContext(), file);

        mProgressDialog = new CustomDialog(this, "加载中...");

        traceId = getIntent().getStringExtra("traceId");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv) {
            takePhoto();
        } else if (v.getId() == R.id.camera) {
            takePhoto();
        } else if (v.getId() == R.id.btn_next) {
            if (mFile1 == null) {
                Toast.makeText(FaceAuthActivity.this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = etName.getText().toString();
            String id = etId.getText().toString();
            if (TextUtils.isEmpty(name)||TextUtils.isEmpty(id)){
                Toast.makeText(FaceAuthActivity.this, "请先完善信息", Toast.LENGTH_SHORT).show();
                return;
            }
            faceAuth(name,id);
        }
    }


    private void takePhoto() {
        Acp.getInstance(getApplicationContext()).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//调用android自带的照相机
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);

                        startActivityForResult(camera, 1);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(FaceAuthActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            File mTempFile = new File(imgUrl);
            Luban.with(this)
                    .load(mTempFile)
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) {
                                mFile1 = file;
                                Glide.with(FaceAuthActivity.this)
                                        .load(mFile1)
                                        .centerCrop()
                                        .dontAnimate()
                                        .into(iv);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FaceCompareActivity", e.toString());
                        }
                    })
                    .launch();
        }
    }

    private void faceAuth(String name, String id) {
        RetrofitWrapper.getInstance().create(ApiStores.class)
                .faceAuth(id,name,mFile1, traceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mProgressDialog.show();
                        addDispose(d);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Log.e("jc", jsonObject.toJSONString());
                        /**
                         * {
                         "success": false,
                         "zqzn_trace_id": "455954320849174531",
                         "error_code": "000002",
                         "message": "PARAMS_CHECK_ERROR:[身份证号[33092064]校验不通过]"
                         }
                         */
                        /**
                         * {
                         "data": {
                         "reason": "人脸与公安网照片不一致，请确保是账户本人操作",
                         "similarity": 0,
                         "verify_status": 0
                         },
                         "success": true,
                         "zqzn_trace_id": "455946302144905223"
                         }
                         */
                        Intent intent=new Intent();
                        intent.putExtra("data",jsonObject);
                        setResult(RESULT_OK,intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(FaceAuthActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                        Log.e("jc",e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private CompositeDisposable mCompositeDisposable;//rxjava2，Disposable的OpenHashSet集合，可以做到切断的操作，让Observer观察者不再接收上游事件

    public void addDispose(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);//将所有disposable放入,集中处理
    }

    private void unDispose() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();//保证activity结束时取消所有正在执行的订阅
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unDispose();
        this.mCompositeDisposable = null;
    }
}
