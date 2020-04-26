package net.aldakur.desdecasacovid19;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // alert. Are you at home?
        if (!isAcceptedHomeAlert()) {
            homeAlert();
        }else {
            checkPermissions();
        }


        Button btnInfo = (Button) findViewById(R.id.activity_maps_btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Intent intent = new Intent(MapsActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean isAcceptedHomeAlert() {
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int accepted = sp.getInt("ISACCEPTEHOMEALERT", 0);
        if (accepted == 0) {
            return false;
        }else {
            return true;
        }
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

                        // SharePreferences ISACCEPTEHOMEALERT to 1
                        setTrueisAcceptedAlert();
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

    private void setTrueisAcceptedAlert() {
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        sp.edit().putInt("ISACCEPTEHOMEALERT", 1).apply();
    }

    private void checkPermissions() {

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


        //  first time get location and draw a circle
        switch (getFirstTimeRun()) {
            case 0:

                // get home location
                client = LocationServices.getFusedLocationProviderClient(this);
                client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            home = new LatLng(location.getLatitude(), location.getLongitude());
                            saveHome(home);

                        } else {

                            Toast toast = Toast.makeText(MapsActivity.this, R.string.maps_activity_toast_alert_no_location, Toast.LENGTH_LONG);
                            toast.show();
                        }

                        // Draw circle around home

                        Resources res = getResources();
                        int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                        Circle circle = mMap.addCircle(new CircleOptions()
                                .center(home)
                                .radius(1000)
                                .strokeColor(Color.RED)
                                .fillColor(grayTransparent));
                        float zoom = 14.0f;

                        // move camera
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
                    }
                });

                break;

            case 1:
                // app has been launched before

                // draw circle around home
                home = getHome();
                Resources res = getResources();

                int grayTransparent = res.getColor(R.color.colorGrayTransparent);
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(getHome())
                        .radius(1000)
                        .strokeColor(Color.RED)
                        .fillColor(grayTransparent));
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


    private int getFirstTimeRun() {
        SharedPreferences sp = getSharedPreferences("MYAPP", 0);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        if (lastVersionCode == -1) result = 0; else
            result = (lastVersionCode == currentVersionCode) ? 1 : 2;
        sp.edit().putInt("FIRSTTIMERUN", currentVersionCode).apply();
        return result;

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
