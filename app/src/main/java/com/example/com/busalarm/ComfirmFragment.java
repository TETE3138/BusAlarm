package com.example.com.busalarm;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Vibrator;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by TETE on 4/13/2017.
 */

public class ComfirmFragment extends Fragment {

    public BusStop destinationBusStop;
    public LatLng currentLocation;
    public LatLng destinationLocation;

    private View thisView;

    public AlarmFragment alarmFragment;
    private static final int seekbar_minvalue = 50;

    public void setData(BusStop destinationBusStop, LatLng currentLocation, LatLng destinationLocation) {
        this.destinationBusStop = destinationBusStop;
        this.currentLocation = currentLocation;
        this.destinationLocation = destinationLocation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        View v = inflater.inflate(R.layout.fragment_comfirm, null);
        thisView = v;
        if (destinationBusStop != null) {
            TextView busStopName = (TextView) v.findViewById(R.id.busstopname_comfirm);
            busStopName.setText(destinationBusStop.name);
        }
        if (destinationLocation != null) {
            TextView busstopdistance_comfirm = (TextView) v.findViewById(R.id.busstopdistance_comfirm);
            int distance_meter = LocationTools.calculateDistanceInMeter(destinationBusStop.location.latitude, destinationBusStop.location.longitude
                    , destinationLocation.latitude, destinationLocation.longitude);
            busstopdistance_comfirm.setText(LocationTools.distanceMeterToString(distance_meter));
        }


        SeekBar seekBar = (SeekBar) v.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        seekBar.setProgress(100);

        Button backButton = (Button) v.findViewById(R.id.backButton);
        backButton.setOnClickListener(backButtonOnClickListener);

        Button startButton = (Button) v.findViewById(R.id.startButton);
        startButton.setOnClickListener(startButtonOnClickListener);

        updateLocation();
        return v;
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            View view = getView();
            if (view == null) {
                view = thisView;
            }
            TextView distance_alert = (TextView) view.findViewById(R.id.distance_alert);
            distance_alert.setText(LocationTools.distanceMeterToString(progress + seekbar_minvalue));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(20);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(20);
        }
    };


    public void updateLocation() {

        View view = getView();
        if (view == null) {
            view = thisView;
        }
        if (view == null)
            return;

        TextView yourdistance_comfirm = (TextView) view.findViewById(R.id.yourdistance_comfirm);
        if (currentLocation != null) {

            int distance_meter = LocationTools.calculateDistanceInMeter(destinationBusStop.location.latitude, destinationBusStop.location.longitude
                    , currentLocation.latitude, currentLocation.longitude);
            yourdistance_comfirm.setText(LocationTools.distanceMeterToString(distance_meter));
        } else {
            yourdistance_comfirm.setText("(ไม่มีข้อมูล)");
        }
    }
    //Back Button
    private View.OnClickListener backButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    };
    //Start Button
    private View.OnClickListener startButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alarmFragment = new AlarmFragment();
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
            alarmFragment.setData(destinationBusStop, destinationLocation, currentLocation, seekBar.getProgress() + seekbar_minvalue);
            getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_up, R.animator.slide_down, R.animator.slide_up, R.animator.slide_down)
                    .replace(R.id.fragment_container, alarmFragment, AlarmFragment.class.getName()).addToBackStack(AlarmFragment.class.getName()).commit();

        }
    };
}
