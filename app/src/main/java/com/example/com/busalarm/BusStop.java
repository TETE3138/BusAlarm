package com.example.com.busalarm;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by TETE on 4/11/2017.
 */

public class BusStop {
    public String name;
    public LatLng location;

    public BusStop(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }

}
