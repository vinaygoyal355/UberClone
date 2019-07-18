package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassengersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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

        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

               updateCameraPaseengerLocation(location);

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

        if(Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

        }
        else if(Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(PassengersActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(PassengersActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);

            }
            else{

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                updateCameraPaseengerLocation(currentPassengerLocation);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(PassengersActivity.this,new String(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                updateCameraPaseengerLocation(currentPassengerLocation);

            }

        }
    }

    private void updateCameraPaseengerLocation(Location location){

        LatLng passengerLocation=new LatLng(location.getLatitude(),location.getLongitude());

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation,17));

        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here"));

    }
}
