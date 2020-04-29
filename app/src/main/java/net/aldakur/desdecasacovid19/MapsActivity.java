package net.aldakur.desdecasacovid19;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //ActivityCompat.OnRequestPermissionsResultCallback
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private FusedLocationProviderClient client;

    private LatLng home;


    private LocationRequest mLocationRequest;



    @Override
    protected void onResume() {
        super.onResume();
        // Log.i("posicion", "on Resume");
        startLocationUpdates();

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // Log.i("posicion", "on restart");
        launch(getCircleHasBeenDrawn());


    }

    @Override
    protected void onStart() {
        super.onStart();
        statusCheck();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Log.i("posicion", "on Create");

        // alert. Are you at home?
        if (!isHasBeenCircleDrawn()) {
            // Log.i("posicion", "onCreate. hasBeenAccepedHOmeAlertDentro del IF");
            homeAlert();
        }
        // May be it is not need. If user cancel alert app is going to close, so never go to else.
        else {
            // Log.i("posicion", "onCreate. Dentro del ELSE");
            //statusCheck();
            checkPermissions();
        }

        // info button. Open InfoActivity
        Button btnInfo = (Button) findViewById(R.id.activity_maps_btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Intent intent = new Intent(MapsActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });



    }

        private void startLocationUpdates() {
            // Log.i("posicion", "startLocationUpdates");
            // Create the location request to start receiving updates
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        }


    private void onLocationChanged(Location lastLocation) {
        // Log.i("posicion", "onLocationChanged");
        // New location has now been determined

        //String location
        // Log.i("posicion", "- Muestro localización -");
        // Log.i("posicion", "localizacion: "+ lastLocation);

        // home = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        // saveHome(home);
        if(getCircleHasBeenDrawn()==0){
            launch(0);
        }


    }


    private void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.maps_activity_settings_gps_alert)
                .setCancelable(false)
                .setPositiveButton(R.string.maps_activity_home_alert_button_accept, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.maps_activity_home_alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void homeAlert() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                // set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                // set title
                .setTitle(R.string.maps_activity_home_alert_title)
                // set message
                .setMessage(R.string.maps_activity_home_alert_text)
                // set positive button
                .setPositiveButton(R.string.maps_activity_home_alert_button_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // positive button is clicked
                        // Check permisions
                        checkPermissions();
                    }
                })

                //set negative button
                .setNegativeButton(R.string.maps_activity_home_alert_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // negative button is clicked
                        finishAndRemoveTask();
                    }
                })
                .show();



    }


    private void checkPermissions() {
        // Log.i("posicion", "checkPermissions");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Not permissions. Request it.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);


        }else {
            startMap();

        }
    }

    private void startMap() {
        // Log.i("posicion", "startMap");
        //statusCheck();
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
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                statusCheck();
                return false;
            }
        });

        // Log.i("posicion", "onMapReady");
        //  first time get location and draw a circle
        int value = getCircleHasBeenDrawn();
        launch(value);


    }

    private void launch(int value) {
        switch (value) {
            case 0:
                // Log.i("posicion", "case 0");
                // get home location
                client = LocationServices.getFusedLocationProviderClient(this);
                // NIK UTE EZ DEL BEHAR locationRequest = LocationRequest.create();
                // NIK UTE EZ DEL BEHAR locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            home = new LatLng(location.getLatitude(), location.getLongitude());
                            saveHome(home);

                            // Draw circle around home
                            // Log.i("posicion", "PINTO CIRCULO");
                            Resources res = getResources();
                            int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                            Circle circle = mMap.addCircle(new CircleOptions()
                                    .center(home)
                                    .radius(1000)
                                    .strokeColor(Color.RED)
                                    .fillColor(grayTransparent));
                            // Log.i("posicion", "- Muestro localización -");
                            // Log.i("posicion", "Latitud: "+ location);


                            // No vuelo a preguntar más lo de estar en casa.
                            setTrueCircleHasBeenDrawn();


                            // move camera
                            float zoom = 14.0f;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));


                        } else {
                            //String location

                            // Log.i("posicion", "Case 0. ELSE. Location is null");
                            Toast.makeText(MapsActivity.this, R.string.maps_activity_toast_alert_no_location, Toast.LENGTH_SHORT).show();

                        }

                    }
                });

                break;


            case 1:
                // app has been launched before
                // We know home
                // we need to print circle
                // Log.i("posicion", "case 1. App has been launched before");
                // draw circle around home

                home = getHome();
                Resources res = getResources();

                int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(getHome())
                        .radius(1000)
                        .strokeColor(Color.RED)
                        .fillColor(grayTransparent));
                //float zoom = 14.0f;
                float zoom = 14.0f;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));

                break;

            case 2:
                // when the version is new and first time launched
                // nowadays do nothing

        }
    }

    private LatLng getHome() {
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);

        String slatitud = sp.getString("HOME_LAT", "0");
        String slongitud = sp.getString("HOME_LNG", "0");

        double latitud = Double.parseDouble(slatitud);
        double longitud = Double.parseDouble((slongitud));

        home = new LatLng(latitud, longitud);

        return home;


    }


    // CIRCLE METHOD AND FUNCTIONS

    // o it has not been drawn
    // 1 it has been drawn
    private int getCircleHasBeenDrawn() {
        // Log.i("posicion", "getCircleHasBeenDrawn");
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        int result = sp.getInt("ISACCEPTEHOMEALERT", 0);

  /*
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        if (lastVersionCode == -1) result = 0; else
            result = (lastVersionCode == currentVersionCode) ? 1 : 2;
        sp.edit().putInt("FIRSTTIMERUN", currentVersionCode).apply();

   */
        return result;

    }


    // det 1
    private void setTrueCircleHasBeenDrawn() {
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        sp.edit().putInt("ISACCEPTEHOMEALERT", 1).apply();
    }


    // false it has not been drawn
    // true it has been drawn
    private boolean isHasBeenCircleDrawn() {
        // Log.i("posicion", "hasBeenAcceptedHomeAlert");
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int accepted = sp.getInt("ISACCEPTEHOMEALERT", 0);
        if (accepted == 0) {
            return false;
        }else {
            return true;
        }
    }


    private void saveHome(LatLng home) {

        // Save home location only the first time that the app launch
        SharedPreferences myPreferences = getSharedPreferences("MYAPP", 0);
        SharedPreferences.Editor myEditor = myPreferences.edit();

        double latitut = home.latitude;
        double longitud = home.longitude;

        myEditor.putString("HOME_LAT", Double.toString(latitut));
        myEditor.putString("HOME_LNG", Double.toString(longitud));
        myEditor.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMap();
            } else {
                Toast.makeText(this, R.string.maps_activity_toast_app_need_permissions, Toast.LENGTH_LONG).show();

            }
        }
    }

}
