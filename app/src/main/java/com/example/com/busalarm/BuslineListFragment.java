package com.example.com.busalarm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by TETE on 4/10/2017.
 */

public class BuslineListFragment extends Fragment {
    public BusStopArrayAdapter currentArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        View v = inflater.inflate(R.layout.fragment_busline_list, null);
        if (currentArrayAdapter != null) {
            ListView busStopListView = (ListView) v.findViewById(R.id.busStopListView);
            busStopListView.setAdapter(currentArrayAdapter);

            busStopListView.setOnItemClickListener(listViewOnItemClickListener);

            TextView title = (TextView) v.findViewById(R.id.listviewtitle);
            if (currentArrayAdapter.size() == 0) {
                title.setText("ไม่พบป้ายรถเมล์");
            } else {
                title.setText("เลือกป้ายรถเมล์ที่ต้องการลง");
            }
        }

        return v;
    }

    private ListView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BusStop busStop = currentArrayAdapter.getItem(position);
            if (busStop == null)
                return;
            MapsActivity mapsActivity = (MapsActivity) getActivity();
            mapsActivity.navigateTo(busStop);
        }
    };
}
