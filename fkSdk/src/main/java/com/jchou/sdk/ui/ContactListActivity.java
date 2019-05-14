package com.jchou.sdk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jchou.sdk.R;
import com.jchou.sdk.adapter.BaseRecyclerAdapter;
import com.jchou.sdk.adapter.ContactListAdapter;
import com.jchou.sdk.models.ContactsInfo;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
    private TextView tvTitle;
    private RecyclerView rv;

    private ContactListAdapter mContactListAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        tvTitle=findViewById(R.id.tv_title);
        rv=findViewById(R.id.rv);
        initView();
        initData();
    }

    private void initView() {
        tvTitle.setText("选择通讯录");
        rv.setLayoutManager(new LinearLayoutManager(this));
        mContactListAdapter=new ContactListAdapter();
        mContactListAdapter.setDatas(list);
        rv.setAdapter(mContactListAdapter);

        mContactListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position, Object data) {
                ContactsInfo contactsInfo = list.get(position);
                Intent intent=new Intent();
                intent.putExtra("contactsInfo",contactsInfo);
                intent.putExtra("contactsInfos",list);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onItemLongClick(int position, Object data) {

            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        getContacts();
    }

    private ArrayList<ContactsInfo> list = new ArrayList<>();

    private void getContacts() {
        list.clear();
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
                    list.add(contactsInfo);
                }
            }
            cursor.close();//使用完后一定要将cursor关闭，不然会造成内存泄露等问题
            mContactListAdapter.notifyDataSetChanged();
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
