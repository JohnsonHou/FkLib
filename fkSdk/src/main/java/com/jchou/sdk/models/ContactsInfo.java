package com.jchou.sdk.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactsInfo implements Parcelable {
    private String name;
    private String phone;
    private String sortKey;
    private int id;


    public ContactsInfo(String name, String phone, String sortKey, int id) {
        this.name = name;
        this.phone = phone;
        this.sortKey = sortKey;
        this.id = id;
    }

    protected ContactsInfo(Parcel in) {
        name = in.readString();
        phone = in.readString();
        sortKey = in.readString();
        id = in.readInt();
    }

    public static final Creator<ContactsInfo> CREATOR = new Creator<ContactsInfo>() {
        @Override
        public ContactsInfo createFromParcel(Parcel in) {
            return new ContactsInfo(in);
        }

        @Override
        public ContactsInfo[] newArray(int size) {
            return new ContactsInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ContactsInfo{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", id=" + id +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(sortKey);
        dest.writeInt(id);
    }
}