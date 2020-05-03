package net.aldakur.bizitokitik;

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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;







public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

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

    private boolean printed = false;


    @Override
    protected void onResume() {
        super.onResume();
        // Log.i("posicion", "on Resume");
        // Log.i("posicion", "on Create. IS HOME SAVE: "+isHomeSaved());
        startLocationUpdates();

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // Log.i("posicion", "on restart");
        // Log.i("posicion", "on Create. IS HOME SAVE: "+isHomeSaved());
        launch(getHomeSaved());


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Log.i("posicion", "on restart");
        // Log.i("posicion", "on Create. IS HOME SAVE: "+isHomeSaved());
        statusCheck();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Log.i("posicion", "on Create");

        // Log.i("posicion", "on Create. IS HOME SAVE: "+isHomeSaved());
        // Log.i("posicion", "on Create. HOEM "+home);
        // alert. Are you at home?
        if (!isHomeSaved()) {
            // Log.i("posicion", "onCreate. isHomeSaved. Inside IF");
            homeAlert();
        }
        // May be it is not need. Because if user cancel alert app is going to close, so never go to else.
        else {
            // Log.i("posicion", "onCreate. isHomeSaved. Inside ELSE");
            // statusCheck();
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

        // Create the location request to start receiving updates.
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
        // Log.i("posicion", "onLocationChanged. - Show Location -");
        // Log.i("posicion", "onLocationChanged. localizacion: "+ lastLocation);

        if(getHomeSaved()==0){
            // Log.i("posicion", "onLocationChanged. home is saved");
            launch(0);
        }


    }


    private void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Log.i("posicion", "statusCheck");
        // GPS provider enable in user's settings
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
        // Log.i("posicion", "HOME ALERT");
        // AlertDialog alertDialog =
        new AlertDialog.Builder(this)
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

        // Log.i("posicion", "onMapReady");
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
        // Second time print circle (if it is necessary)
        int value = getHomeSaved();
        launch(value);


    }

    private void launch(int value) {
        switch (value) {
            case 0:
                // Log.i("posicion", "case 0");

                // get home location
                client = LocationServices.getFusedLocationProviderClient(this);
                client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Log.i("posicion", "Case 0. onSuccess");
                        if (location != null && !printed) {
                            // Log.i("posicion", "Case 0. onSuccess. Location true");
                            home = new LatLng(location.getLatitude(), location.getLongitude());
                            saveHome(home);
                            setTrueHomeSaved();


                            // Draw circle around home
                            // Log.i("posicion", "PRINT CIRCLE");
                            Resources res = getResources();
                            int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                            //Circle circle =
                            mMap.addCircle(new CircleOptions()
                                    .center(home)
                                    .radius(1000)
                                    .strokeColor(Color.RED)
                                    .fillColor(grayTransparent));
                            //Log.i("posicion", "Case 0. - Show location -");
                            //Log.i("posicion", "case 0 Home: "+home);



                            // Don't ask more. Are you at home?
                            printed = true;


                            // move camera
                            float zoom = 14.0f;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom), new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    // snackbar. if home location is wrong, recalculate it
                                    // Log.i("posicion", "ON FINISH -----------------");
                                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content),
                                            R.string.maps_activity_home_snackbar_text, Snackbar.LENGTH_LONG);
                                    mySnackbar.setAction(R.string.maps_activity_home_snackbar_button, MapsActivity.this)
                                            .setDuration(5500);
                                    mySnackbar.show();

                                    mySnackbar.addCallback(new Snackbar.Callback() {

                                        @Override
                                        public void onDismissed(Snackbar mySnackbar, int event) {
                                            //see Snackbar.Callback docs for event details
                                            // Log.i("posicion", "SnackBar. onDismissed");
                                        }

                                        @Override
                                        public void onShown(Snackbar mySnackbar) {
                                            // Log.i("posicion", "SnackBar. onShow");
                                        }
                                    });



                                }

                                @Override
                                public void onCancel() {
                                    //Log.i("posicion", "snackBar. onCancel");

                                }
                            });



                        } else {
                            // Log.i("posicion", "Case 0. onSuccess. Location is null");
                            Toast.makeText(MapsActivity.this, R.string.maps_activity_toast_alert_no_location, Toast.LENGTH_SHORT).show();

                        }

                    }

                });

                break;


            case 1:
                // app has been launched before
                // we know home
                // we need to print circle
                // Log.i("posicion", "case 1. App has been launched before");

                // draw circle around home
                home = getHome();
                Resources res = getResources();
                if (!printed) {
                    // Log.i("posicion", "case 1. Inside IF");
                    int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                    // Circle circle
                    mMap.addCircle(new CircleOptions()
                            .center(getHome())
                            .radius(1000)
                            .strokeColor(Color.RED)
                            .fillColor(grayTransparent));
                    printed = true;
                }
                // Log.i("posicion", "case 1. Outside IF");
                float zoom = 14.0f;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));


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

    // int o it has not been printed
    // int 1 it has been printed
    private int getHomeSaved() {
        // Log.i("posicion", "getHomeSaved");

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


    // set 1
    private void setTrueHomeSaved() {
        // Log.i("posicion", "setTrueHomeSaved");

        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        sp.edit().putInt("ISACCEPTEHOMEALERT", 1).apply();
    }


    // return false it has not been printed
    // return true it has been printed
    private boolean isHomeSaved() {
        // Log.i("posicion", "isHomeSaved");

        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        // int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int accepted = sp.getInt("ISACCEPTEHOMEALERT", 0);
        if (accepted == 0) {
            return false;
        }else {
            return true;
        }
    }


    private void saveHome(LatLng home) {
        // Log.i("posicion", "saveHome");

        // Save home location only the first time that the app launch
        // if user accepted home alert
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

    // Snackbar
    @Override
    public void onClick(View view) {
        // Log.i("posicion", "SnackBar. onClick");
        mMap.clear();
        printed = false;
        statusCheck();
        launch(0);
    }

}