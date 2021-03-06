package com.broderickwestrope.whiteboard.student_records;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.broderickwestrope.whiteboard.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle b = getIntent().getExtras();
        address = b.getString("address");
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
        map = googleMap;

        if (address.equals("")) {
            // Add a marker in Sydney and move the camera
            LatLng position = new LatLng(-33.8688, 151.2093);
            map.addMarker(new MarkerOptions().position(position).title("Address Not Found"));
            map.moveCamera(CameraUpdateFactory.newLatLng(position));
        } else {
            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            try {        // max no results
                List<Address> addresses = geoCoder.getFromLocationName(address, 5);
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                LatLng position = new LatLng(latitude, longitude);
                map.addMarker(new MarkerOptions().position(position).title("Address: " + address));
                //Move the camera to the user's location and zoom in
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
            } catch (Exception ignored) {
            }
        }
    }


}