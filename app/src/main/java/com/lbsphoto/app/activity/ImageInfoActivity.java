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

import java.io.IOException;

/**
 * @author
 */
public class ImageInfoActivity extends BaseActivity implements OnGetGeoCoderResultListener {
    private ImageView srcIm;
    private TextView pathTx;
    private TextView locationTx;
    private TextView addressTx;
    private TextView phoneInfoTx;

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

        Glide.with(LbsPhotoApplication.getAppContext())
                .load("file://" + path)
                .into(srcIm);
        setDiffColor(pathTx, "路径：" + path);

        regeoLatlng(path);

        findViewById(R.id.image_item_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void regeoLatlng(final String path) {
        String latLngStr = getPhotoLocation(path);
        double lat = Double.parseDouble(latLngStr.split("-")[0]);
        double lon = Double.parseDouble(latLngStr.split("-")[1]);
        final LatLng latLng = new LatLng(lat, lon);
        // 如果经纬度为空并且是来源于拍照的话，那么就调用定位方法，此处逻辑还需要优化

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
                output1 = convertRationalLatLonToFloat(latValue, latRef);
                output2 = convertRationalLatLonToFloat(lngValue, lngRef);
            }
            setDiffColor(phoneInfoTx, "手机型号：" + deviceName + "," + deviceModel);
            setDiffColor(locationTx, "经纬度：" + output1 + ";" + output2);
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

    /**
     * TextView分段设置颜色等样式
     * @param textView text
     * @param str string
     */
    private void setDiffColor(TextView textView,String str){
        if (textView == null) {
            return;
        }
        SpannableString sp = new SpannableString(str);
        sp.setSpan(new ForegroundColorSpan(Color.RED),str.indexOf("：")+1,str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(sp);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
