package com.example.test;

import android.os.Parcel;
import android.os.Parcelable;

public class ShopInfo implements Parcelable {
    private final String businessName;
    private String roadAddress;

    public ShopInfo(String businessName, String roadAddress) {
        this.businessName = businessName.trim().replaceAll("^\"|\"$", ""); // 맨 뒤에 있는 큰따옴표만 제거
        this.roadAddress = roadAddress.trim();
    }

    protected ShopInfo(Parcel in) {
        businessName = in.readString();
        roadAddress = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(businessName);
        dest.writeString(roadAddress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final Creator<ShopInfo> CREATOR = new Creator<ShopInfo>() {
        @Override
        public ShopInfo createFromParcel(Parcel in) {
            return new ShopInfo(in);
        }

        @Override
        public ShopInfo[] newArray(int size) {
            return new ShopInfo[size];
        }
    };

    public String getBusinessName() {
        return businessName;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    @Override
    public String toString() {
        return businessName + "\n" + roadAddress;
    }
}
