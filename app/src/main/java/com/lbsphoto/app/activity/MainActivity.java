package com.lbsphoto.app.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.bean.PhotoUpImageItem;
import com.lbsphoto.app.util.LogUtils;
import com.lbsphoto.app.util.PhotoUpAlbumHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author pc
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_CAPTURE_CODE = 100;

    private ImageView albumIm;
    private ImageView settingIm;
    private ImageView cameraIm;
    private MapView mapView;
    private File cameraFile;

    private long mLastClickTime;
    private String mImagePath;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        albumIm = findViewById(R.id.album_logo);
        settingIm = findViewById(R.id.setting_logo);
        cameraIm = findViewById(R.id.camera_logo);
        mapView = findViewById(R.id.map_view);

        albumIm.setOnClickListener(this);
        settingIm.setOnClickListener(this);
        cameraIm.setOnClickListener(this);

        PhotoUpAlbumHelper photoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
        photoUpAlbumHelper.init(MainActivity.this);
        photoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
            @Override
            public void getAlbumList(final List<PhotoUpImageBucket> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (PhotoUpImageBucket bucket : list) {
                            List<PhotoUpImageItem> photoUpImageItems = bucket.imageList;
                            if (photoUpImageItems == null || photoUpImageItems.isEmpty()) {
                                continue;
                            }

                            for (final PhotoUpImageItem photoUpImageItem : photoUpImageItems) {
                                if (photoUpImageItem == null) {
                                    continue;
                                }

                                final BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(photoUpImageItem.getImagePath(), options);
                                options.inSampleSize = calculateInSampleSize(options, 50, 50);
                                options.inJustDecodeBounds = false;
                                final Bitmap bitmap = BitmapFactory.decodeFile(photoUpImageItem.getImagePath(), options);
                                mapView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        reGeoLatLng(photoUpImageItem.getImagePath(), bitmap);
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
        photoUpAlbumHelper.execute(false);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        mLocationClient.start();
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            MyLocationData locData = new MyLocationData.Builder()
                    //定位精度
                    .accuracy(location.getRadius())
                    //此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100)
                    //百度纬度坐标
                    .latitude(location.getLatitude())
                    //百度经度坐标
                    .longitude(location.getLongitude()).build();
            //设置定位数据, 只有先允许定位图层后设置数据才会生效
            mapView.getMap().setMyLocationData(locData);

            LatLng latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            //定义地图状态
            //MapStatus.Builder地图状态构造器
            MapStatus.Builder builder = new MapStatus.Builder();
            //设置地图中心点,为我们的位置
            builder.target(latLng)
                    //设置地图缩放级别
                    .zoom(16.0f);
            //animateMapStatus以动画方式更新地图状态，动画耗时 300 ms
            mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.album_logo:
                startActivity(new Intent(MainActivity.this, AlbumsActivity.class));
                break;

            case R.id.setting_logo:
                break;

            case R.id.camera_logo:
                selectPhoto(RESULT_CAPTURE_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CAPTURE_CODE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(cameraFile.getAbsolutePath(), options);
            options.inSampleSize = calculateInSampleSize(options, 50, 50);
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeFile(cameraFile.getAbsolutePath(), options);
            LogUtils.i(TAG, "camera file:" + cameraFile.getAbsolutePath());
            try {
                String insert = MediaStore.Images.Media.insertImage(getContentResolver(),
                        cameraFile.getAbsolutePath(), cameraFile.getName(), "lbs photo image");
                LogUtils.i(TAG, "insert:" + insert + ", file name:" + cameraFile.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cameraFile)));
            mediaConnection(cameraFile.getAbsolutePath());
            scanFilePath(cameraFile);

            reGeoLatLng(cameraFile.getAbsolutePath(), bm);
        }
    }
    private MediaScannerConnection mMediaConnection;
    private void mediaConnection(final String path) {
        try {
            mMediaConnection = new MediaScannerConnection(LbsPhotoApplication.getAppContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    LogUtils.i(TAG, "onMediaScannerConnected");
                    mMediaConnection.scanFile(path,"image/jpeg");
                }
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    LogUtils.i(TAG, "onScanCompleted");
                    mMediaConnection.disconnect();
                }
            });
            mMediaConnection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanFilePath(File path) {
        // 判断SDK版本是不是4.4或者高于4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] paths = new String[]{path.getAbsolutePath(), path.getParentFile().getAbsolutePath()};
            MediaScannerConnection.scanFile(LbsPhotoApplication.getAppContext(), paths, null, null);
        } else {
            final Intent intent;
            if (path.isDirectory()) {
                intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
                intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                Log.v(TAG, "directory changed, send broadcast:" + intent.toString());
            } else {
                intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(path));
                Log.v(TAG, "file changed, send broadcast:" + intent.toString());
            }
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();
        mMediaConnection = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void selectPhoto(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
        mImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "lbsphoto";
        final File tmpCameraFile = new File(mImagePath, timeStamp + ".jpg");
        if (!tmpCameraFile.getParentFile().exists()) {
            tmpCameraFile.getParentFile().mkdir();
        }
        if (type == RESULT_CAPTURE_CODE) {
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri cameraUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.lbsphoto.app.fileprovider", tmpCameraFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraFile = tmpCameraFile;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            } else {
                Uri cameraUri = Uri.fromFile(tmpCameraFile);
                cameraFile = tmpCameraFile;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            }
            startActivityForResult(intent, RESULT_CAPTURE_CODE);
        }
    }

    private void reGeoLatLng(final String path, final Bitmap bitmap) {
        String latLngStr = getPhotoLocation(path);
        double lat = Double.parseDouble(latLngStr.split("-")[0]);
        double lon = Double.parseDouble(latLngStr.split("-")[1]);
        final LatLng latLng = new LatLng(lat, lon);

        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        CoordinateConverter coordinateConverter = new CoordinateConverter().coord(latLng);
        OverlayOptions overlayOptions = new MarkerOptions()
                .position(coordinateConverter.convert())
                .icon(bitmapDescriptor)
                .extraInfo(bundle);
        mapView.getMap().addOverlay(overlayOptions);
        mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime <= 1000) {
                    return false;
                }
                mLastClickTime = curTime;

                Bundle markBundle = marker.getExtraInfo();
                Log.i(TAG, "marker:" + markBundle.getString("path"));
                Intent intent = new Intent(MainActivity.this, ImageInfoActivity.class);
                intent.putExtra("path", markBundle.getString("path"));
                startActivity(intent);
                return false;
            }
        });

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public String getPhotoLocation(String imagePath) {
        float output1 = 0;
        float output2 = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            // 拍摄时间
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
                output1 = convertRationalLatLonToFloat(latValue, latRef);
                output2 = convertRationalLatLonToFloat(lngValue, lngRef);
            }
        } catch (IllegalArgumentException|IOException e) {

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
