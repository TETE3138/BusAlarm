package com.example.com.busalarm;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by TETE on 4/14/2017.
 */


public class AlarmFragment extends Fragment {
    BusStop destinationBusStop;
    LatLng destinationPosition;
    LatLng currentPosition;
    int alarmDistance;
    View thisView;
    int startDistance;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        View v = inflater.inflate(R.layout.fragment_alarm, null);
        thisView = v;

        TextView busstopname_comfirm = (TextView) v.findViewById(R.id.busstopname_comfirm);
        busstopname_comfirm.setText(destinationBusStop.name);


        TextView busstopdistance_comfirm = (TextView) v.findViewById(R.id.busstopdistance_comfirm);
        int busstopdistance = LocationTools.calculateDistanceInMeter(destinationBusStop.location.latitude, destinationBusStop.location.longitude, destinationPosition.latitude, destinationPosition.longitude);
        busstopdistance_comfirm.setText(LocationTools.distanceMeterToString(busstopdistance));

        TextView distance_alert = (TextView) v.findViewById(R.id.distance_alert);
        distance_alert.setText(LocationTools.distanceMeterToString(alarmDistance));

        Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(cancelButtonOnClickListener);

        updateLocation();

        return v;


    }

    boolean firstTime = true;

    public void updateLocation() {
        View view = getView();
        if (view == null) {
            view = thisView;
        }
        if (view == null) return;


        TextView yourdistance_comfirm = (TextView) view.findViewById(R.id.yourdistance_comfirm);
        TextView distance_remaining = (TextView) view.findViewById(R.id.distance_remaining);
        if (currentPosition == null) {
            yourdistance_comfirm.setText("(ไม่มีข้อมูล)");
            distance_remaining.setText("(ไม่มีข้อมูล)");
        } else {
            int yourdistance = LocationTools.calculateDistanceInMeter(currentPosition.latitude, currentPosition.longitude,
                    destinationBusStop.location.latitude, destinationBusStop.location.longitude);
            yourdistance_comfirm.setText(LocationTools.distanceMeterToString(yourdistance));


            int remaining_distance = yourdistance - alarmDistance;


            ProgressBar progressBarAlarm = (ProgressBar) view.findViewById(R.id.progressBarAlarm);
            if (firstTime) {
                progressBarAlarm.setMax(yourdistance - alarmDistance);
                firstTime = false;
            } else {
                progressBarAlarm.setProgress(progressBarAlarm.getMax() - (yourdistance - alarmDistance));
            }
            if (remaining_distance <= 0) {
                remaining_distance = 0;
                Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
                cancelButton.setText("หยุด");
                alarm(true);
            }
            distance_remaining.setText("เหลืออีก " + LocationTools.distanceMeterToString(remaining_distance));
        }

        MapsActivity mapsActivity = (MapsActivity) getActivity();
        mapsActivity.removeAllBusStopMarkerExcept(destinationBusStop);
    }

    private boolean firstTime2 = true;

    void alarm(boolean vibrate) {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibratePattern = {0, 1000, 500, 1000, 500};
        if (vibrate) {
            if (firstTime2) {
                v.vibrate(vibratePattern, 0);
                firstTime2 = !firstTime2;
            }
        } else {
            v.cancel();
        }
    }

    public void setData(BusStop destinationBusStop, LatLng destinationPosition, LatLng currentPosition, int alarmDistance) {
        this.destinationBusStop = destinationBusStop;
        this.destinationPosition = destinationPosition;
        this.currentPosition = currentPosition;
        this.alarmDistance = alarmDistance;
    }

    private Button.OnClickListener cancelButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestExit();

        }
    };
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    MapsActivity activity = (MapsActivity)getActivity();
                    activity.clearAll();


                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

public void requestExit(){
    if (firstTime2 == false) {
        alarm(false);

        MapsActivity activity = (MapsActivity)getActivity();
        activity.clearAll();

    } else {
        Context c = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("ต้องการยกเลิกการเตือน?").setPositiveButton("ใช่", dialogClickListener)
                .setNegativeButton("ไม่", dialogClickListener).show();
    }

}


}