package com.lbsphoto.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.application.RequestCode;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.bean.PhotoUpImageItem;
import com.lbsphoto.app.dbmanager.DBManager;
import com.lbsphoto.app.util.ImageBitmapUtil;
import com.lbsphoto.app.util.LatLonUtil;
import com.lbsphoto.app.util.LogUtils;
import com.lbsphoto.app.util.PhotoUpAlbumHelper;
import com.lbsphoto.app.util.ThreadCenter;

import java.io.File;
import java.io.FileNotFoundException;
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
    /** 相机拍照返回文件图片 */
    private File cameraFile;
    private ImageView cameraImResult;

    /** 标记mark点击时间，防止重复点击 */
    private long mLastClickTime;
    /** 标记相机拍照返回 */
    private boolean isCameraResultFile = false;
    /** 标记第一次定位 */
    private boolean isFirstLocation = true;

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
        cameraImResult = findViewById(R.id.camera_result);

        PhotoUpAlbumHelper photoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
        photoUpAlbumHelper.init(MainActivity.this);
        photoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
            @Override
            public void getAlbumList(final List<PhotoUpImageBucket> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }
                dealPhotoImageBucketList(list);
            }
        });
        photoUpAlbumHelper.execute(false);
        getLocation();
    }

    /** 处理本地图片 找准位置显示在地图上 */
    private void dealPhotoImageBucketList(final List<PhotoUpImageBucket> list) {
        ThreadCenter.getInstance().excuteThread(new Runnable() {
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
                        options.inSampleSize = ImageBitmapUtil.calculateInSampleSize(options, 50, 50);
                        options.inJustDecodeBounds = false;
                        final Bitmap bitmap = BitmapFactory.decodeFile(photoUpImageItem.getImagePath(), options);
                        reGeoLatLng(photoUpImageItem.getImagePath(), bitmap);
                    }
                }
            }
        });
    }

    @Override
    protected void setListener() {
        albumIm.setOnClickListener(this);
        settingIm.setOnClickListener(this);
        cameraIm.setOnClickListener(this);
    }

    /** 定位 逻辑 */
    private void getLocation() {
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

    /** 循环定位  定位当前位置  每个10秒进行定位操作 */
    private Handler handler = new Handler();
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.i(TAG, "isStart:" + mLocationClient.isStarted());
            if (!mLocationClient.isStarted()) {
                getLocation();
            }
        }
    };

    /** 定位回调 */
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocationClient.unRegisterLocationListener(myListener);
            mLocationClient.stop();
            handler.postDelayed(locationRunnable, 10 * 1000);
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
            LogUtils.i(TAG, "update lat lng:" + latLng);
            LbsPhotoApplication.mCurLat = location.getLatitude();
            LbsPhotoApplication.mCurLng = location.getLongitude();
            if (isFirstLocation) {
                isFirstLocation = false;
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
            ThreadCenter.getInstance().excuteThread(new Runnable() {
                @Override
                public void run() {
                    getCameraResultFile(cameraFile.getAbsolutePath(), cameraFile.getName());
                }
            });
        }
    }

    private void getCameraResultFile(final String cameraFilePath, final String name) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(cameraFilePath, options);
        options.inSampleSize = ImageBitmapUtil.calculateInSampleSize(options, 50, 50);
        options.inJustDecodeBounds = false;
        final Bitmap bm = BitmapFactory.decodeFile(cameraFilePath, options);
        LogUtils.i(TAG, "camera file:" + cameraFilePath);
        mapView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String insert = MediaStore.Images.Media.insertImage(getContentResolver(),
                            cameraFilePath, name, "lbs photo image");
                    LogUtils.i(TAG, "insert:" + insert + ", file name:" + name);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "error: Images.Media.insertImage");
                }

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(cameraFilePath))));
                mediaConnection(cameraFilePath);

                isCameraResultFile = true;
                reGeoLatLng(cameraFilePath, bm);
            }
        });
    }

    private MediaScannerConnection mMediaConnection;
    private void mediaConnection(final String path) {
        try {
            mMediaConnection = new MediaScannerConnection(LbsPhotoApplication.getAppContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    LogUtils.i(TAG, "onMediaScannerConnected");
                    try {
                        mMediaConnection.scanFile(path,"image/jpeg");
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "onMediaScannerConnected error");
                    }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        mMediaConnection = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
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
        String mImagePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + RequestCode.FILE_PATH;
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

    private LatLng reGeoLatLng = null;
    private void reGeoLatLng(final String path, final Bitmap bitmap) {
        String latLngStr = LatLonUtil.getPhotoLocation(path);
        double lat = Double.parseDouble(latLngStr.split("-")[0]);
        double lon = Double.parseDouble(latLngStr.split("-")[1]);
        if (isCameraResultFile) {
            if (!(lat - 0 <= RequestCode.DOUBLE_ZERO && lon - 0 <= RequestCode.DOUBLE_ZERO)) {
            } else {
                lat = LbsPhotoApplication.mCurLat;
                lon = LbsPhotoApplication.mCurLng;
            }
            reGeoLatLng = new LatLng(lat, lon);
            DBManager.getInstance(LbsPhotoApplication.getAppContext()).insertCameraPath(path, lat+"-"+ lon);
        } else {
            if (!(lat - 0 <= RequestCode.DOUBLE_ZERO && lon - 0 <= RequestCode.DOUBLE_ZERO)) {
                reGeoLatLng = new LatLng(lat, lon);
            } else {
                String latlng = DBManager.getInstance(LbsPhotoApplication.getAppContext())
                        .getCameraPath(path);
                latLngStr = latlng;
                if (!TextUtils.isEmpty(latlng)) {
                    lat = Double.parseDouble(latLngStr.split("-")[0]);
                    lon = Double.parseDouble(latLngStr.split("-")[1]);
                    reGeoLatLng = new LatLng(lat, lon);
                }
            }
        }

        if (reGeoLatLng == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        CoordinateConverter coordinateConverter = new CoordinateConverter().coord(reGeoLatLng);
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

        if (isCameraResultFile) {
            isCameraResultFile = false;
            Glide.with(LbsPhotoApplication.getAppContext())
                    .load(path)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            startAnimate();
                            return false;
                        }
                    })
                    .into(cameraImResult);
        }
    }

    /** 启动动画   透明度逐渐透明  宽高逐渐缩放最小 */
    private void startAnimate() {
        cameraImResult.animate()
                .alpha(0.0f)
                .scaleX(0.1f)
                .scaleY(0.1f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        //定义地图状态
                        //MapStatus.Builder地图状态构造器
                        CoordinateConverter coordinateConverter = new CoordinateConverter().coord(reGeoLatLng);
                        MapStatus.Builder builder = new MapStatus.Builder();
                        //设置地图中心点,为我们的位置
                        builder.target(coordinateConverter.convert())
                                //设置地图缩放级别
                                .zoom(16.0f);
                        //animateMapStatus以动画方式更新地图状态，动画耗时 300 ms
                        mapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    }
                })
                .setDuration(1000)
                .setStartDelay(200)
                .start();
    }
}
