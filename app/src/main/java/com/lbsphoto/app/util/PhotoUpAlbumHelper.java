package com.lbsphoto.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;

import com.lbsphoto.app.application.RequestCode;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.bean.PhotoUpImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author lbsphoto
 */
public class PhotoUpAlbumHelper extends AsyncTask<Object, Object, Object>{
	private static final String TAG = PhotoUpAlbumHelper.class.getSimpleName();
	private ContentResolver cr;
	private HashMap<String, PhotoUpImageBucket> bucketList = new HashMap<String, PhotoUpImageBucket>();
	private GetAlbumList getAlbumList;
	
	private PhotoUpAlbumHelper() {}
	public static PhotoUpAlbumHelper getHelper() {
		return new PhotoUpAlbumHelper();
	}

	/**
	 * 初始化
	 * @param context context
	 */
	public void init(Context context) {
		cr = context.getContentResolver();
	}

	/**
	 * 得到图片集
	 */
	private void buildImagesBucketList(Uri uri) {
		// 构造相册索引
		String[] columns = new String[]{Media._ID, Media.BUCKET_ID,
				Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
				Media.SIZE, Media.BUCKET_DISPLAY_NAME};
		// 得到一个游标
		Cursor cur = cr.query(uri, columns, null, null,
				Media.DATE_MODIFIED+" desc");
		if (cur == null) {
			return;
		}

		if (cur.moveToFirst()) {
			// 获取指定列的索引
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
			/*
			 * Description:这里增加了一个判断：判断照片的名字是否合法，例如.jpg .png    图片名字是不合法的，直接过滤掉
			 */
			do {
				String photoPath = cur.getString(photoPathIndex);
				LogUtils.i(TAG, "photoPath : " + photoPath);
				int lastIndexOfXie = photoPath.lastIndexOf("/");
				int lastIndexOfPot = photoPath.lastIndexOf(".");
				if (lastIndexOfPot <= 0
						||lastIndexOfXie <= 0
						||photoPath.substring(
						photoPath.lastIndexOf("/")+1,
						photoPath.lastIndexOf("."))
						.replaceAll(" ", "").length()<=0)
				{
					Log.d(TAG, "出现了异常图片的地址：cur.getString(photoPathIndex)="+photoPath);
				} else {
					String id = cur.getString(photoIDIndex);
					String path = cur.getString(photoPathIndex);
					if (!reGeoLatLng(path)) {
						continue;
					}
					String bucketName = cur.getString(bucketDisplayNameIndex);
					String bucketId = cur.getString(bucketIdIndex);
					PhotoUpImageBucket bucket = bucketList.get(bucketId);
					if (bucket == null) {
						bucket = new PhotoUpImageBucket();
						bucketList.put(bucketId, bucket);
						bucket.imageList = new ArrayList<>();
						bucket.bucketName = bucketName;
					}
					bucket.count++;
					PhotoUpImageItem imageItem = new PhotoUpImageItem();
					imageItem.setImageId(id);
					imageItem.setImagePath(path);
					bucket.imageList.add(imageItem);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	/**
	 * 得到图片集
	 */
	private List<PhotoUpImageBucket> getImagesBucketList() {
		buildImagesBucketList(Media.EXTERNAL_CONTENT_URI);
		buildImagesBucketList(Media.INTERNAL_CONTENT_URI);
		scanLbsPhoto();

		List<PhotoUpImageBucket> tmpList = new ArrayList<>();
		Iterator<Entry<String, PhotoUpImageBucket>> itr = bucketList.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, PhotoUpImageBucket> entry = itr
					.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
	}

	private void scanLbsPhoto() {
		String path = Environment.getExternalStorageDirectory() + File.separator+  RequestCode.FILE_PATH;
		File parentFile = new File(path);
		if (!parentFile.exists()) {
			parentFile.mkdir();
		}

		File[] childFile = parentFile.listFiles();
		if (childFile == null || childFile.length == 0) {
			return;
		}

		for (File child : childFile) {
			String key = "";
			Set<Entry<String, PhotoUpImageBucket>> entrySet = bucketList.entrySet();
			for (Entry<String, PhotoUpImageBucket> entry : entrySet) {
			    PhotoUpImageBucket photoUpImageBucket = entry.getValue();
			    if (photoUpImageBucket.bucketName.equals(RequestCode.FILE_PATH)) {
			        key = entry.getKey();
			        break;
                }
            }

            if (TextUtils.isEmpty(key)) {
			    key = RequestCode.FILE_PATH;
            }

			PhotoUpImageBucket bucket = bucketList.get(key);
			if (bucket == null) {
				bucket = new PhotoUpImageBucket();
				bucketList.put(RequestCode.FILE_PATH, bucket);
				bucket.imageList = new ArrayList<>();
				bucket.bucketName = RequestCode.FILE_PATH;
			}
			PhotoUpImageItem imageItem = new PhotoUpImageItem();
			imageItem.setImageId(child.getName());
			imageItem.setImagePath(child.getAbsolutePath());
			boolean isHas = false;
			for (PhotoUpImageItem photoUpImageItem : bucket.imageList) {
			    if (photoUpImageItem.getImagePath().equals(imageItem.getImagePath())) {
                    //do nothing
					isHas = true;
					break;
                }
            }

            if (!isHas) {
				bucket.imageList.add(imageItem);
				bucket.count++;
			}
		}
	}

	public void setGetAlbumList(GetAlbumList getAlbumList) {
		this.getAlbumList = getAlbumList;
	}

	public interface GetAlbumList {
		/**
		 * 获取相册列表
		 * @param list list
		 */
		void getAlbumList(List<PhotoUpImageBucket> list);
	}

	@Override
	protected Object doInBackground(Object... params) {
		return getImagesBucketList();
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		getAlbumList.getAlbumList((List<PhotoUpImageBucket>)result);
	}

	private boolean reGeoLatLng(String path) {
		String latLngStr = LatLonUtil.getPhotoLocation(path);
		double lat = Double.parseDouble(latLngStr.split("-")[0]);
		double lon = Double.parseDouble(latLngStr.split("-")[1]);
		return !(lat - 0 <= RequestCode.DOUBLE_ZERO && lon - 0 <= RequestCode.DOUBLE_ZERO);
	}
}
