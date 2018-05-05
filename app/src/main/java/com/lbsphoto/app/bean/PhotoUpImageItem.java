package com.lbsphoto.app.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoUpImageItem implements Parcelable {

	//图片ID
	private String imageId;
	//原图路径
	private String imagePath;
	//是否被选择
	private boolean isSelected = false;

	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}


	public PhotoUpImageItem() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.imageId);
		dest.writeString(this.imagePath);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
	}

	protected PhotoUpImageItem(Parcel in) {
		this.imageId = in.readString();
		this.imagePath = in.readString();
		this.isSelected = in.readByte() != 0;
	}

	public static final Creator<PhotoUpImageItem> CREATOR = new Creator<PhotoUpImageItem>() {
		@Override
		public PhotoUpImageItem createFromParcel(Parcel source) {
			return new PhotoUpImageItem(source);
		}

		@Override
		public PhotoUpImageItem[] newArray(int size) {
			return new PhotoUpImageItem[size];
		}
	};
}
