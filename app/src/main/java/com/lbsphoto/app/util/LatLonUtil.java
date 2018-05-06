package com.lbsphoto.app.util;

import android.media.ExifInterface;

import java.io.IOException;

/**
 * @author
 */
public class LatLonUtil {

    public static String getPhotoLocation(String imagePath) {
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
                output1 = LatLonUtil.convertRationalLatLonToFloat(latValue, latRef);
                output2 = LatLonUtil.convertRationalLatLonToFloat(lngValue, lngRef);
            }
        } catch (IllegalArgumentException|IOException e) {
            e.printStackTrace();
        }
        return output1 + "-" + output2;
    }

    public static float convertRationalLatLonToFloat(
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
