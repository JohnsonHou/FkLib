package com.jchou.sdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jchou.sdk.R;
import com.jchou.sdk.models.ContactsInfo;
import com.zhy.autolayout.utils.AutoUtils;


public class ContactListAdapter extends BaseRecyclerAdapter<ContactsInfo> {
    private Context mContext;

    @Override
    public RecyclerView.ViewHolder onCreate(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact_list, parent, false));
    }

    @Override
    public void onBind(final RecyclerView.ViewHolder viewHolder, int realPosition, ContactsInfo data) {
        ((ViewHolder) viewHolder).tvName.setText(data.getName());
        ((ViewHolder) viewHolder).tvMobile.setText(data.getPhone());
    }

    class ViewHolder extends Holder {
        private TextView tvName,tvMobile;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvMobile = (TextView) itemView.findViewById(R.id.tv_mobile);
            AutoUtils.auto(itemView);
        }
    }
}