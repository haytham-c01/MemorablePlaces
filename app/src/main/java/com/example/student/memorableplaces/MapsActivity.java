package com.example.student.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                    Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(lastKnownLocation, "Your Location");
                }
            }
        }
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
        Intent intent= getIntent();
        if(intent.getIntExtra("placeNumber", 0) == 0){
            locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            mMap.setOnMapLongClickListener(this);

            locationListener= new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "Your location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your location");
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Location location= new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
            location.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);
            centerMapOnLocation(location, MainActivity.places.get(intent.getIntExtra("placeNumber", 0)));
        }

    }

    private void centerMapOnLocation(Location location, String title) {
        mMap.clear();
        LatLng userLocation= new LatLng(location.getLatitude(), location.getLongitude());

        if(title != "Your location") {
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder= new Geocoder(this, Locale.getDefault());
        StringBuilder sb= new StringBuilder();
        try {
            List<Address> addresses= geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if(addresses!= null && addresses.size() > 0){
                if(addresses.get(0).getThoroughfare() != null) {
                    if (addresses.get(0).getSubThoroughfare() != null) {
                        sb.append(addresses.get(0).getSubThoroughfare()).append(" ");
                    }
                    sb.append(addresses.get(0).getThoroughfare());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(sb.length() == 0 ){
            SimpleDateFormat sdf= new SimpleDateFormat("HH:mm yyyy-MM-dd");
            sb.append(sdf.format(new Date()));
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(sb.toString()));
        MainActivity.places.add(sb.toString());
        MainActivity.locations.add(latLng);

        MainActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();

        saveLocation();
    }

    private void saveLocation(){
        // save state
        SharedPreferences sharedPreferences= this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        try {
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> latitudes= new ArrayList<>();
        ArrayList<String> longitudes= new ArrayList<>();

        for(LatLng coordinates: MainActivity.locations){
            latitudes.add(String.valueOf(coordinates.latitude));
            longitudes.add(String.valueOf(coordinates.longitude));
        }
        try {
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
