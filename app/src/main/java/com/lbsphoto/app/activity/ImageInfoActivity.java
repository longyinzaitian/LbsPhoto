package com.lbsphoto.app.activity;

import android.graphics.Color;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.application.RequestCode;
import com.lbsphoto.app.dbmanager.DBManager;
import com.lbsphoto.app.util.LatLonUtil;
import com.lbsphoto.app.util.LogUtils;

import java.io.IOException;

/**
 * @author LBSPHOTO
 */
public class ImageInfoActivity extends BaseActivity implements OnGetGeoCoderResultListener {
    private ImageView srcIm;
    private TextView pathTx;
    private TextView locationTx;
    private TextView addressTx;
    private TextView phoneInfoTx;
    private TextView timeTx;

    private GeoCoder geoCoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_layout);
        String path = getIntent().getStringExtra("path");

        srcIm = findViewById(R.id.image_item_src);
        pathTx = findViewById(R.id.image_item_path);
        locationTx = findViewById(R.id.image_item_location);
        addressTx = findViewById(R.id.image_item_address);
        phoneInfoTx = findViewById(R.id.image_item_phone_info);
        timeTx = findViewById(R.id.image_item_time);
        setData(path);
    }

    private void setData(String path) {
        Glide.with(LbsPhotoApplication.getAppContext())
                .load("file://" + path)
                .into(srcIm);
        setDiffColor(pathTx, "路径：" + path);

        reGeoLatLng(path);

        findViewById(R.id.image_item_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void reGeoLatLng(final String path) {
        String latLngStr = getPhotoLocation(path);
        double lat = Double.parseDouble(latLngStr.split("-")[0]);
        double lon = Double.parseDouble(latLngStr.split("-")[1]);
        LatLng latLng = new LatLng(lat, lon);
        if (!(lat - 0 <= RequestCode.DOUBLE_ZERO && lon - 0 <= RequestCode.DOUBLE_ZERO)) {
            //do nothing
        } else {
            String latlng = DBManager.getInstance(LbsPhotoApplication.getAppContext())
                    .getCameraPath(path);
            LogUtils.i("ImageInfoActivity", "find lat lng:" + latlng);
            latLngStr = latlng;
            if (!TextUtils.isEmpty(latlng)) {
                lat = Double.parseDouble(latLngStr.split("-")[0]);
                lon = Double.parseDouble(latLngStr.split("-")[1]);
                latLng = new LatLng(lat, lon);
            }
        }
        // 如果经纬度为空并且是来源于拍照的话，那么就调用定位方法，此处逻辑还需要优化
        LogUtils.i("ImageInfoActivity", "latLng:" + latLng.toString());
        setDiffColor(locationTx, "经纬度：" + lat + ";" + lon);
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption();
        reverseGeoCodeOption.location(latLng);
        geoCoder.reverseGeoCode(reverseGeoCodeOption);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (!TextUtils.isEmpty(reverseGeoCodeResult.getAddress())) {
            setDiffColor(addressTx, "地理位置：" + reverseGeoCodeResult.getAddress());
        } else {
            setDiffColor(addressTx, "地理位置：" + reverseGeoCodeResult.getBusinessCircle());
        }
    }

    public String getPhotoLocation(String imagePath) {
        float output1 = 0;
        float output2 = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            // 拍摄时间
            String datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            // 设备品牌
            String deviceName = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            // 设备型号
            String deviceModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
                output1 = LatLonUtil.convertRationalLatLonToFloat(latValue, latRef);
                output2 = LatLonUtil.convertRationalLatLonToFloat(lngValue, lngRef);
            }
            setDiffColor(phoneInfoTx, "手机型号：" + deviceName + "," + deviceModel);
            setDiffColor(timeTx, "拍摄时间：" + datetime);
        } catch (IllegalArgumentException|IOException e) {
            output1 = 0;
            output2 = 0;
        }
        return output1 + "-" + output2;
    }

    /**
     * TextView分段设置颜色等样式
     * @param textView text
     * @param str string
     */
    private void setDiffColor(TextView textView,String str) {
        if (textView == null) {
            return;
        }
        SpannableString sp = new SpannableString(str);
        sp.setSpan(new ForegroundColorSpan(Color.RED),str.indexOf("：")+1,str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(sp);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geoCoder != null) {
            geoCoder.destroy();
        }
    }
}
