package com.example.com.busalarm;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentposMarker;
    private Marker desctinationMarker;

    public List<Marker> busstopMarkerList = new LinkedList<Marker>();
    private static final int REQUEST_LOCATION_CODE = 3;
    private static final int STREET_ZOOM_VALUE = 16;
    private static final int BUSSTOP_RADIUS = 500;//meter

    private BuslineListFragment buslineListFragment = new BuslineListFragment();
    private ComfirmFragment confirmFragment = new ComfirmFragment();

    private FragmentManager fragmentManager;

    private LatLng selectedLocation;

    private Boolean isMenuHidden = true;
    private LatLng destinationLocation = null;


    FloatingActionButton upButton;
    FloatingActionButton currentPosButton;

    private FragmentManager.OnBackStackChangedListener backStackChangedListener() {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    buslineListFragment.currentArrayAdapter = null;
                    removeMapMarker(busstopMarkerList);
                    setMenuVisible(false);
                }

            }
        };

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setInitButtonsPosition();

        fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(backStackChangedListener());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Google place fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("TH")
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("", "Place: " + place.getName());


                final LatLng selectedLocation = place.getLatLng();
                if (desctinationMarker != null)
                    desctinationMarker.remove();

                desctinationMarker = mMap.addMarker(new MarkerOptions().position(selectedLocation).title(place.getName().toString()));
                moveCameraToLocation(selectedLocation, STREET_ZOOM_VALUE);


                // Instantiate the RequestQueue.
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                        String.valueOf(selectedLocation.latitude) + "," + String.valueOf(selectedLocation.longitude) + "&type=bus_station&radius=" + BUSSTOP_RADIUS + "&language=th" + "&key=" + getString(R.string.google_maps_key);
                Map<String, String> params = new LinkedHashMap();
                JSONObject parameters = new JSONObject(params);
                // Request a string response from the provided URL.
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, parameters, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (!(response.getString("status").equals("OK") || response.getString("status").equals("ZERO_RESULTS")))
                                return;

                            removeMapMarker(busstopMarkerList);
                            busstopMarkerList.clear();

                            JSONArray results = response.getJSONArray("results");
                            List<BusStop> busstopList = new ArrayList<BusStop>();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                JSONObject location = item.getJSONObject("geometry").getJSONObject("location");
                                LatLng markerLatLng = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
                                String markerName = item.getString("name");
                                //Log.v("TAG",markerName);

                                BusStop newbusStop = new BusStop(markerName, markerLatLng);

                                Marker marker = mMap.addMarker(new MarkerOptions().position(markerLatLng).title(markerName).icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop)));
                                marker.setTag(newbusStop);
                                busstopMarkerList.add(marker);


                                busstopList.add(newbusStop);
                            }
                            BusStop[] busStopArray = new BusStop[busstopList.size()];
                            busstopList.toArray(busStopArray);
                            showBusList(new BusStopArrayAdapter(getApplicationContext(), busStopArray, selectedLocation));
                            setMenuVisible(true, false);

                            // buslineListFragment.setListViewArrayAdapter(new BusStopArrayAdapter(getApplicationContext(), busstopList.toArray(busStopArray), selectedLocation));

                        } catch (JSONException e) {
                            //TODO: handle parse failure
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        //TODO: handle failure
                    }
                });
                popAllBackStack();

                //  if (fragmentManager.findFragmentByTag(BuslineListFragment.class.getName()) instanceof BuslineListFragment) {
                // } else {

                // }


                // Add the request to the RequestQueue.
                Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }


        });


        //Busline List fragment up
        FloatingActionButton upButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_up);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buslineListFragment == null) {
                    buslineListFragment = new BuslineListFragment();
                }


                if (!isMenuHidden) {

                    //fragmentManager.popBackStack();
                    setMenuVisible(false);

                } else {


                    setMenuVisible(true);
                }


            }
        });


        // My location button
        FloatingActionButton mylocationButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_mylocation);
        mylocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentposMarker != null) {
                    moveCameraToLocation(currentposMarker.getPosition(), STREET_ZOOM_VALUE);
                }
            }
        });
        this.upButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_up);
        this.currentPosButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_mylocation);
    }

    // move camera to a specified location with animation.
    public void moveCameraToLocation(LatLng location, int level) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(level)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void popAllBackStack() {
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStackImmediate();
        }
    }

    void removeMapMarker(List<Marker> markers) {
        for (Marker item : markers) {
            item.remove();
        }
    }

    void setMenuVisible(Boolean value) {
        if (value) {
            //If there is no fragment to show then create a new one.
            if (fragmentManager.findFragmentByTag(BuslineListFragment.class.getName()) instanceof BuslineListFragment) {
            } else {
                showBusList(null);
            }
            LinearLayout container = (LinearLayout) findViewById(R.id.fragment_container);
            container.setVisibility(View.VISIBLE);

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            container.startAnimation(animation);


            isMenuHidden = false;
            setButtonsPostionUp(true);
        } else {

            LinearLayout container = (LinearLayout) findViewById(R.id.fragment_container);

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);

            container.startAnimation(animation);

            setButtonsPostionUp(false);
            container.setVisibility(View.GONE);
            isMenuHidden = true;
        }
    }


    void setMenuVisible(Boolean value, Boolean CheckIfNoFragment) {
        if (value) {
            LinearLayout container = (LinearLayout) findViewById(R.id.fragment_container);
            container.setVisibility(View.VISIBLE);
            isMenuHidden = false;
            setButtonsPostionUp(true);
        } else {
            setButtonsPostionUp(false);
            LinearLayout container = (LinearLayout) findViewById(R.id.fragment_container);
            container.setVisibility(View.GONE);
            isMenuHidden = true;
        }
    }

    void showBusList(BusStopArrayAdapter aa) {
        // buslineListFragment.onDestroy();
        buslineListFragment = new BuslineListFragment();
        buslineListFragment.currentArrayAdapter = aa;
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_up, R.animator.slide_down, R.animator.slide_up, R.animator.slide_down)
                .replace(R.id.fragment_container, buslineListFragment, BuslineListFragment.class.getName())
                .addToBackStack(BuslineListFragment.class.getName())
                .commit();
        setButtonsPostionUp(true);
    }

    private int init_upButton_marginTop;
    private int init_upButton_marginRight;
    private int init_upButton_marginLeft;
    private int init_upButton_marginBottom;

    // Use in setMenuVisible only()
    void setButtonsPostionUp(boolean arg) {
        FloatingActionButton upButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_up);
        if (arg) {

/*
            Animation animation_button_up = AnimationUtils.loadAnimation(this, R.animator.enter_from_left);

            animation_button_up.setFillAfter(true);
            upButton.startAnimation(animation_button_up);
            currentPosButton.startAnimation(animation_button_up);
*/
            upButton.animate().translationY(LocationTools.dipToPixels(this, -230));
            currentPosButton.animate().setDuration(500).translationY(LocationTools.dipToPixels(this, -230));

            //((RelativeLayout.LayoutParams)upButton.getLayoutParams()).setMarginStart(300);
            ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).setMargins(init_upButton_marginLeft, init_upButton_marginTop,
                    init_upButton_marginRight, init_upButton_marginBottom + 0);

        } else {
            /*
            Animation animation_button_down = AnimationUtils.loadAnimation(this, R.anim.slide_button_down);
            animation_button_down.setFillAfter(true);
            upButton.startAnimation(animation_button_down);
            currentPosButton.startAnimation(animation_button_down);
*/

            upButton.animate().translationY(LocationTools.dipToPixels(this, 0));
            currentPosButton.animate().translationY(LocationTools.dipToPixels(this, 0));

            ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).setMargins(init_upButton_marginLeft, init_upButton_marginTop
                    , init_upButton_marginRight, init_upButton_marginBottom);
        }
    }

    void setInitButtonsPosition() {
        FloatingActionButton upButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_up);
        init_upButton_marginTop = ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).topMargin;
        init_upButton_marginRight = ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).rightMargin;
        init_upButton_marginLeft = ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).leftMargin;
        init_upButton_marginBottom = ((RelativeLayout.LayoutParams) upButton.getLayoutParams()).bottomMargin;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Check if location permission is not granted then send the request
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        //setup for onLocationChanged
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            String provider = lm.getBestProvider(criteria, true);
            try {
                lm.requestLocationUpdates(provider, 1L, 1f, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        //create a new if current position marker is null
                        if (currentposMarker == null) {
                            currentposMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(getString(R.string.mylocation)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.mylocation)));
                            //zoom to current position if user hasn't search anything.
                            if (fragmentManager.getBackStackEntryCount() == 0) {
                                moveCameraToLocation(currentposMarker.getPosition(), STREET_ZOOM_VALUE);
                            }

                        } else
                            currentposMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

                        //update distance in confirmFragment
                        if (fragmentManager.findFragmentByTag(ComfirmFragment.class.getName()) instanceof ComfirmFragment && confirmFragment != null) {
                            confirmFragment.currentLocation = currentposMarker.getPosition();
                            confirmFragment.updateLocation();
                            //update distance in alarmFragment
                            if (fragmentManager.findFragmentByTag(AlarmFragment.class.getName()) instanceof AlarmFragment && confirmFragment.alarmFragment != null) {
                                confirmFragment.alarmFragment.currentPosition = currentposMarker.getPosition();
                                confirmFragment.alarmFragment.updateLocation();
                            }
                        }

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            } catch (SecurityException e) {
                // lets the user know there is a problem with the gps
            }
            //Marker CLick
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    // Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();

                    //Check if busstop marker is clicked
                    if (busstopMarkerList.contains(marker)) {
                        navigateTo((BusStop) marker.getTag());
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            //TODO: Notifiy user that the location request was rejected.
        }
    }

    public void navigateTo(BusStop des) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        confirmFragment = new ComfirmFragment();
        if (currentposMarker == null) {
            confirmFragment.setData(des, null, desctinationMarker.getPosition());
        } else {
            confirmFragment.setData(des, currentposMarker.getPosition(), desctinationMarker.getPosition());
        }

        confirmFragment.destinationBusStop = des;
        ft.setCustomAnimations(R.animator.slide_up, R.animator.slide_down, R.animator.slide_up, R.animator.slide_down)
                .replace(R.id.fragment_container, confirmFragment, ComfirmFragment.class.getName());

        if (fragmentManager.findFragmentByTag(ComfirmFragment.class.getName()) instanceof ComfirmFragment) {
            fragmentManager.popBackStack();
        }

        ft.addToBackStack(ComfirmFragment.class.getName());
        ft.commit();

        if (isMenuHidden) {
            setMenuVisible(true);
        }
        moveCameraToLocation(des.location, STREET_ZOOM_VALUE);

    }

    public void removeAllBusStopMarkerExcept(BusStop except) {
        for (Marker marker : busstopMarkerList) {
            if ((BusStop) marker.getTag() != except) {
                marker.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //when back button is pressed while navigating.
        AlarmFragment fragment = (AlarmFragment) fragmentManager.findFragmentByTag(AlarmFragment.class.getName());
        if (fragment instanceof AlarmFragment) {
            fragment.requestExit();
        } else {
            super.onBackPressed();
        }

    }

    public void clearAll() {
        removeMapMarker(busstopMarkerList);
        desctinationMarker.remove();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
    }
}
