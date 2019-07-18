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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengersActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Button btnRequestCar;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isUserCancelled=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar=findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        ParseQuery<ParseObject> carRequestQuery=ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size()>0){

                        isUserCancelled=false;
                        btnRequestCar.setText("Cancel your Uber!");

                    }
                    else{
                        Toast.makeText(PassengersActivity.this,"No user",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(PassengersActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    @Override
    public void onClick(View view) {

        if(isUserCancelled){

            if(ContextCompat.checkSelfPermission(PassengersActivity.this,new String(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
                Location passengerCurrentLocation= locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                if(passengerCurrentLocation != null){

                    ParseObject requestcar=new ParseObject("RequestCar");
                    requestcar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation= new ParseGeoPoint(passengerCurrentLocation.getLatitude(),passengerCurrentLocation.getLongitude());
                    requestcar.put("passengerLocation",userLocation);

                    requestcar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(PassengersActivity.this,"A car request is send",Toast.LENGTH_SHORT).show();
                                btnRequestCar.setText("Cancel your uber order");
                                isUserCancelled=false;
                            }
                            else{
                                Toast.makeText(PassengersActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else{
                    Toast.makeText(PassengersActivity.this,"Unknown Error: Something Went Wrong",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                ActivityCompat.requestPermissions(PassengersActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2000);
            }
        }
        else if(isUserCancelled==false){

            ParseQuery<ParseObject> carRequestQuery=ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {

                    if(e==null){

                        if(requestList.size()>0){

                            isUserCancelled=true;
                            btnRequestCar.setText("Request a car");

                            for(ParseObject object: requestList){

                                object.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        if(e==null){

                                            Toast.makeText(PassengersActivity.this, "Request deleted", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                });

                            }

                        }

                    }

                }
            });

        }

    }
}
