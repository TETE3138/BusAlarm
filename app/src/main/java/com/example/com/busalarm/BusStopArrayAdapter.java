package com.example.com.busalarm;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by TETE on 4/11/2017.
 */

public class BusStopArrayAdapter extends ArrayAdapter<BusStop> {

    private final Context context;
    private final BusStop[] values;
    private LatLng destinationLatLng;

    public BusStopArrayAdapter(Context context, BusStop[] values, LatLng destinationLatLng) {
        super(context, R.layout.busline_item, values);
        this.context = context;
        this.destinationLatLng = destinationLatLng;
        this.values = sortBusStops(values);

    }

    public int size() {
        return values.length;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.busline_item, parent, false);

        TextView busStopNameTextView = (TextView) rowView.findViewById(R.id.busstopnameLabel);
        TextView distanceTextView = (TextView) rowView.findViewById(R.id.distanceLabel);
        busStopNameTextView.setTextColor(rowView.getResources().getColor(R.color.colorNormalFront));
        distanceTextView.setTextColor(rowView.getResources().getColor(R.color.colorLightFront));

        busStopNameTextView.setText(values[position].name);
        distanceTextView.setText(LocationTools.calculateDistanceInMeter(values[position].location.latitude
                , values[position].location.longitude, destinationLatLng.latitude, destinationLatLng.longitude) + " เมตร");

        return rowView;
    }

    BusStop[] sortBusStops(BusStop[] array) {
        LinkedList<BusStop> sortedArray = new LinkedList<BusStop>();
        LinkedList<BusStop> list = new LinkedList<BusStop>(Arrays.asList(array));


        for (int i = 0; i < array.length; i++) {
            BusStop min = list.getFirst();
            for (BusStop item : list) {
                int item_val = LocationTools.calculateDistanceInMeter(item.location.latitude, item.location.longitude, destinationLatLng.latitude, destinationLatLng.longitude);
                int min_val = LocationTools.calculateDistanceInMeter(min.location.latitude, min.location.longitude, destinationLatLng.latitude, destinationLatLng.longitude);
                if (item_val < min_val) {
                    min = item;
                }
            }
            list.remove(min);
            sortedArray.add(min);
        }

        BusStop[] ret_array = new BusStop[sortedArray.size()];
        sortedArray.toArray(ret_array);
        return ret_array;

    }
}
