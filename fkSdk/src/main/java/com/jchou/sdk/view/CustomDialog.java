package com.jchou.sdk.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jchou.sdk.R;


public class CustomDialog extends ProgressDialog {
    private TextView tvLoad;
    CharSequence mMessage;

    public CustomDialog(Context context, @Nullable CharSequence message) {
        this(context, R.style.CustomDialog, message);
    }

    public CustomDialog(Context context) {
        this(context, R.style.CustomDialog, null);
    }

    public CustomDialog(Context context, int theme, @Nullable CharSequence message) {
        super(context, theme);
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context).inflate(R.layout.loadinglayout, null, false);
        setContentView(view);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        tvLoad = (TextView) view.findViewById(R.id.tv_load_dialog);
        if (!TextUtils.isEmpty(mMessage)) {
            tvLoad.setVisibility(View.VISIBLE);
            tvLoad.setText(mMessage);
        }
    }

    @Override
    public void show() {
        super.show();
    }
}