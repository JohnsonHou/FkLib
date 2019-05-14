package com.jchou.sdk.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.jchou.sdk.R;
import com.jchou.sdk.SdkManager;
import com.jchou.sdk.models.ContactsInfo;
import com.jchou.sdk.models.PostBean;
import com.jchou.sdk.utils.ApiStores;
import com.jchou.sdk.utils.ImgUtil;
import com.jchou.sdk.utils.RetrofitWrapper;
import com.jchou.sdk.view.CustomDialog;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class FaceCompareActivity extends AppCompatActivity implements View.OnClickListener {


    private CustomDialog mProgressDialog;

    ImageView iv, iv2, iv3;

    private String imgUrl = "";
    private String macAddress = "";
    private Uri cameraUri;
    private String fontUrl, backUrl, faceUrl;
    private double longitude, latitude;

    private int index;

    TextView tvTitle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_compare);
        iv = findViewById(R.id.iv);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);
        findViewById(R.id.iv).setOnClickListener(this);
        findViewById(R.id.iv2).setOnClickListener(this);
        findViewById(R.id.iv3).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.camera2).setOnClickListener(this);
        findViewById(R.id.camera3).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("活体认证");

        //camera
        File file = ImgUtil.getTempPhoto(getApplicationContext());
        imgUrl = file.getPath();
        cameraUri = ImgUtil.getUriForFile(getApplicationContext(), file);

        mProgressDialog = new CustomDialog(this, "加载中...");

        initData();
    }

    private void initData() {
        getLocation();
        getContactlist();
        macAddress = SdkManager.getInstance().getMacAddress(this);
    }

    private void getLocation() {
        SdkManager.getInstance().getLocation(this, new SdkManager.LocationListener() {
            @Override
            public void locationEnd(double longitude, double latitude) {
                FaceCompareActivity.this.longitude = longitude;
                FaceCompareActivity.this.latitude = latitude;
            }

            @Override
            public void locationError() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv) {
            takePhoto(1);
        } else if (v.getId() == R.id.iv2) {
            takePhoto(2);
        } else if (v.getId() == R.id.iv3) {
            takePhoto(3);
        } else if (v.getId() == R.id.camera) {
            takePhoto(1);
        } else if (v.getId() == R.id.camera2) {
            takePhoto(2);
        } else if (v.getId() == R.id.camera3) {
            takePhoto(3);
        } else if (v.getId() == R.id.btn_next) {
            if (TextUtils.isEmpty(fontUrl) || TextUtils.isEmpty(backUrl) || TextUtils.isEmpty(faceUrl)) {
                Toast.makeText(FaceCompareActivity.this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
                return;
            }
            if (longitude == 0 || latitude == 0) {
                Toast.makeText(FaceCompareActivity.this, "请确认已打开网络和位置信息", Toast.LENGTH_SHORT).show();
                getLocation();
                return;
            }
            if (contactList == null || contactList.size() == 0) {
                Toast.makeText(FaceCompareActivity.this, "请确认同意手机通讯录权限", Toast.LENGTH_SHORT).show();
                getContactlist();
                return;
            }
            upload();
        }
    }

    private void takePhoto(int index) {
        this.index = index;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"拍照", "从手机相册选择"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Acp.getInstance(getApplicationContext()).request(new AcpOptions.Builder()
                                        .setPermissions(Manifest.permission.CAMERA,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                                        Toast.makeText(FaceCompareActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                    case 1:
                        Acp.getInstance(getApplicationContext())
                                .request(new AcpOptions.Builder()
                                                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        , Manifest.permission.READ_EXTERNAL_STORAGE)
                                                .build(),
                                        new AcpListener() {
                                            @Override
                                            public void onGranted() {
                                                Matisse.from(FaceCompareActivity.this)
                                                        .choose(MimeType.allOf())
                                                        .countable(true)
                                                        .maxSelectable(1)//最大数量
//                                                    .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                                        .thumbnailScale(0.85f)
                                                        .imageEngine(new GlideEngine())
                                                        .theme(R.style.Matisse_Dracula)
                                                        .forResult(2);
                                            }

                                            @Override
                                            public void onDenied(List<String> permissions) {
                                                Toast.makeText(FaceCompareActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                        break;
                }
            }
        }).create().show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = Matisse.obtainResult(data);
            File mTempFile = new File(ImgUtil.getRealPathFromUri(getApplicationContext(), uris.get(0)));
            Luban.with(getApplicationContext())
                    .load(mTempFile)
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) {
                            imgUpload(file);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FaceCompareActivity", e.toString());
                        }
                    })
                    .launch();
        }
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
                            imgUpload(file);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FaceCompareActivity", e.toString());
                        }
                    })
                    .launch();
        }
    }

    private void imgUpload(File file) {
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/png"), file);
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Observable<JSONObject> observable = null;
        ApiStores apiStores = RetrofitWrapper.getInstance().create(ApiStores.class);
        if (index == 1) {
            observable = apiStores.fontPut(body);
        } else if (index == 2) {
            observable = apiStores.backPut(body);
        } else if (index == 3) {
            observable = apiStores.filePut(body);
        }
        if (observable == null) return;
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mProgressDialog.show();
                        addDispose(d);
                    }

                    @Override
                    public void onNext(JSONObject map) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        String code = map.getString("code");
                        if (code.equals("99")) {
                            String url = map.getString("data");
                            if (index == 1) {
                                fontUrl = url;
                                Glide.with(FaceCompareActivity.this)
                                        .load(fontUrl)
                                        .centerCrop()
                                        .dontAnimate()
                                        .into(iv);
                            } else if (index == 2) {
                                backUrl = url;
                                Glide.with(FaceCompareActivity.this)
                                        .load(backUrl)
                                        .centerCrop()
                                        .dontAnimate()
                                        .into(iv2);
                            } else if (index == 3) {
                                faceUrl = url;
                                Glide.with(FaceCompareActivity.this)
                                        .load(faceUrl)
                                        .centerCrop()
                                        .dontAnimate()
                                        .into(iv3);
                            }
                        } else {
                            String msg = map.getString("msg");
                            Toast.makeText(FaceCompareActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(FaceCompareActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                        Log.e("SdkManager", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void upload() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(contactList);
        String[] keys = {"fontImgrUrl", "backImgUrl", "faceImg",
                "longitude", "latitude",
                "mac", "mchInfo", "systemInfo",
                "relationShip", "isBreak"};
        Object[] values = {fontUrl, backUrl, faceUrl,
                longitude, latitude,
                macAddress, Build.MODEL.replace("　", " "), "android " + Build.VERSION.RELEASE,
                jsonArray.toJSONString(), "0"};
        RetrofitWrapper.getInstance().create(ApiStores.class)
                .baseInfoPut(new PostBean(keys, values).toJson())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mProgressDialog.show();
                        addDispose(d);
                    }

                    @Override
                    public void onNext(JSONObject map) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        String code = map.getString("code");
                        if (code.equals("99")) {
                            Toast.makeText(FaceCompareActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent();
                            intent.putExtra("data",map.getJSONObject("data"));
                            setResult(RESULT_OK,intent);
                            finish();
                        } else {
                            String msg = map.getString("msg");
                            Toast.makeText(FaceCompareActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isFinishing() && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(FaceCompareActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                        Log.e("jc", e.toString());
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


    List<ContactsInfo> contactList = new ArrayList<>();

    private void getContactlist() {
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.READ_CONTACTS)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        getContacts();
//                        ctx.startActivityForResult(new Intent(ctx, ContactListActivity.class), REQUEST_CONTACT);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Log.e("SdkManager", "权限拒绝无法获取通讯录");
                        Toast.makeText(FaceCompareActivity.this, "权限拒绝无法获取通讯录", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void getContacts() {
        contactList.clear();
        try {
            Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor = getContentResolver().query(contactUri,
                    new String[]{"display_name", "sort_key", "contact_id", "data1"},
                    null, null, "sort_key");
            String contactName;
            String contactNumber;
            String contactSortKey;
            int contactId;
            while (cursor.moveToNext()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                contactSortKey = getSortkey(cursor.getString(1));
                ContactsInfo contactsInfo = new ContactsInfo(contactName, contactNumber.replace("-", ""), contactSortKey, contactId);
                if (contactName != null) {
                    contactList.add(contactsInfo);
                }
            }
            cursor.close();//使用完后一定要将cursor关闭，不然会造成内存泄露等问题
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private String getSortkey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        } else
            return "#";   //获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
    }
}
