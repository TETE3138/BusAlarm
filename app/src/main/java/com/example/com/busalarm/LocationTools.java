package com.example.com.busalarm;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by TETE on 4/12/2017.
 */

public class LocationTools {
    public final static double AVERAGE_RADIUS_OF_EARTH_M = 6371000;

    public static int calculateDistanceInMeter(double userLat, double userLng,
                                               double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_M * c));
    }

    public static String distanceMeterToString(int meter) {
        if (meter >= 1000){
            double km = (double)meter / 1000;
            return String.format("%.2f", km)  + " กิโลเมตร";
        } else{
            return String.valueOf(meter) + " เมตร";
        }
    }
    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
