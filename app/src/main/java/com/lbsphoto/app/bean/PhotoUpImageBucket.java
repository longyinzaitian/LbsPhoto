package com.lbsphoto.app.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 一个目录下的相册对象
 */
public class PhotoUpImageBucket implements Parcelable {
	
	public int count = 0;
	public String bucketName;
	public List<PhotoUpImageItem> imageList;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public List<PhotoUpImageItem> getImageList() {
		return imageList;
	}
	public void setImageList(List<PhotoUpImageItem> imageList) {
		this.imageList = imageList;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.count);
		dest.writeString(this.bucketName);
		dest.writeList(this.imageList);
	}

	public PhotoUpImageBucket() {
	}

	protected PhotoUpImageBucket(Parcel in) {
		this.count = in.readInt();
		this.bucketName = in.readString();
		this.imageList = new ArrayList<PhotoUpImageItem>();
		in.readList(this.imageList, PhotoUpImageItem.class.getClassLoader());
	}

	public static final Parcelable.Creator<PhotoUpImageBucket> CREATOR = new Parcelable.Creator<PhotoUpImageBucket>() {
		@Override
		public PhotoUpImageBucket createFromParcel(Parcel source) {
			return new PhotoUpImageBucket(source);
		}

		@Override
		public PhotoUpImageBucket[] newArray(int size) {
			return new PhotoUpImageBucket[size];
		}
	};
}
