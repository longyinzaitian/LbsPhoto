package com.lbsphoto.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;
import android.util.Log;

import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.bean.PhotoUpImageItem;

import java.io.File;
import java.io.IOException;
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
	/** 缩略图列表 */
	private HashMap<String, String> thumbnailList = new HashMap<String, String>();
	/** 专辑列表 */
	private List<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
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
	 * 得到缩略图
	 */
	private void getThumbnail() {
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
				Thumbnails.DATA};
		Cursor cursor1 = Thumbnails.queryMiniThumbnails(cr, Thumbnails.EXTERNAL_CONTENT_URI,
				Thumbnails.MINI_KIND, projection);
		getThumbnailColumnData(cursor1);
		cursor1.close();
	}

	/**
	 * 从数据库中得到缩略图
	 * @param cur cursor
	 */
	private void getThumbnailColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int imageId;
			String imagePath;
			int imageIdColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
			do {
				imageId = cur.getInt(imageIdColumn);
				imagePath = cur.getString(dataColumn);
				thumbnailList.put("" + imageId, imagePath);
			} while (cur.moveToNext());
		}
	}

	/**
	 * 得到原图-n
	 */
	private void getAlbum() {
		String[] projection = { Albums._ID, Albums.ALBUM, Albums.ALBUM_ART,
				Albums.ALBUM_KEY, Albums.ARTIST, Albums.NUMBER_OF_SONGS };
		Cursor cursor1 = cr.query(Albums.EXTERNAL_CONTENT_URI, projection, null,
				null, Albums.ALBUM_ID+" desc");
		if (cursor1 == null) {
			return;
		}

		getAlbumColumnData(cursor1);
		cursor1.close();
	}

	/**
	 * 从本地数据库中得到原图
	 */
	private void getAlbumColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int id;
			String album;
			String albumArt;
			String albumKey;
			String artist;
			int numOfSongs;
			int idColumn = cur.getColumnIndex(Albums._ID);
			int albumColumn = cur.getColumnIndex(Albums.ALBUM);
			int albumArtColumn = cur.getColumnIndex(Albums.ALBUM_ART);
			int albumKeyColumn = cur.getColumnIndex(Albums.ALBUM_KEY);
			int artistColumn = cur.getColumnIndex(Albums.ARTIST);
			int numOfSongsColumn = cur.getColumnIndex(Albums.NUMBER_OF_SONGS);
			do {
				id = cur.getInt(idColumn);
				album = cur.getString(albumColumn);
				albumArt = cur.getString(albumArtColumn);
				albumKey = cur.getString(albumKeyColumn);
				artist = cur.getString(artistColumn);
				numOfSongs = cur.getInt(numOfSongsColumn);
				HashMap<String, String> hash = new HashMap<>(8);
				hash.put("_id", id + "");
				hash.put("album", album);
				hash.put("albumArt", albumArt);
				hash.put("albumKey", albumKey);
				hash.put("artist", artist);
				hash.put("numOfSongs", numOfSongs + "");
				albumList.add(hash);
			} while (cur.moveToNext());
		}
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
				if (cur.getString(photoPathIndex).substring(
						cur.getString(photoPathIndex).lastIndexOf("/")+1,
						cur.getString(photoPathIndex).lastIndexOf("."))
						.replaceAll(" ", "").length()<=0)
				{
					Log.d(TAG, "出现了异常图片的地址：cur.getString(photoPathIndex)="+cur.getString(photoPathIndex));
					Log.d(TAG, "出现了异常图片的地址：cur.getString(photoPathIndex).substring="+cur.getString(photoPathIndex)
							.substring(cur.getString(photoPathIndex).lastIndexOf("/")+1,cur.getString(photoPathIndex).lastIndexOf(".")));
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
		String path = Environment.getExternalStorageDirectory() + File.separator+  "lbsphoto";
		File parentFile = new File(path);
		if (!parentFile.exists()) {
			parentFile.mkdir();
		}

		File[] childFile = parentFile.listFiles();
		if (childFile == null || childFile.length == 0) {
			return;
		}

		for (File child : childFile) {
			if (!reGeoLatLng(child.getAbsolutePath())) {
				continue;
			}

			String key = "";
			Set<Entry<String, PhotoUpImageBucket>> entrySet = bucketList.entrySet();
			for (Entry<String, PhotoUpImageBucket> entry : entrySet) {
			    PhotoUpImageBucket photoUpImageBucket = entry.getValue();
			    if (photoUpImageBucket.bucketName.equals("lbsphoto")) {
			        key = entry.getKey();
			        break;
                }
            }

            if (TextUtils.isEmpty(key)) {
			    key = "lbsphoto";
            }

			PhotoUpImageBucket bucket = bucketList.get(key);
			if (bucket == null) {
				bucket = new PhotoUpImageBucket();
				bucketList.put("lbsphoto", bucket);
				bucket.imageList = new ArrayList<>();
				bucket.bucketName = "lbsphoto";
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

	public void destoryList()
	{
		thumbnailList.clear();
		thumbnailList = null;
		albumList.clear();
		albumList = null;
		bucketList.clear();
		bucketList = null;
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
		String latLngStr = getPhotoLocation(path);
		double lat = Double.parseDouble(latLngStr.split("-")[0]);
		double lon = Double.parseDouble(latLngStr.split("-")[1]);
		return !(lat - 0 <= 0.001 && lon - 0 <= 0.001);
	}

	private String getPhotoLocation(String imagePath) {
		float output1 = 0;
		float output2 = 0;

		try {
			ExifInterface exifInterface = new ExifInterface(imagePath);
			String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
			String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
			String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
			String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
			if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
				output1 = convertRationalLatLonToFloat(latValue, latRef);
				output2 = convertRationalLatLonToFloat(lngValue, lngRef);
			}
		} catch (IllegalArgumentException|IOException e) {
			output1 = 0;
			output2 = 0;
		}
		return output1 + "-" + output2;
	}

	private static float convertRationalLatLonToFloat(
			String rationalString, String ref) {

		String[] parts = rationalString.split(",");

		String[] pair;
		pair = parts[0].split("/");
		double degrees = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		pair = parts[1].split("/");
		double minutes = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		pair = parts[2].split("/");
		double seconds = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
		if ((ref.equals("S") || ref.equals("W"))) {
			return (float) -result;
		}
		return (float) result;
	}
}
