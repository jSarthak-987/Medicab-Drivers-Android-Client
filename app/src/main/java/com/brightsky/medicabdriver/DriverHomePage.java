package com.brightsky.medicabdriver;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class DriverHomePage extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted = false;
    private static final int CURRENT_LOCATION_ZOOM = 17;
    private static final int DEFAULT_ZOOM = 5;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(28.613939, 77.209021);
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_page);

        registerReceiver(broadcastReceiver, new IntentFilter(DriversFirebaseMessagingService.INTENT_FILTER));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("fcmtoken");
                        reference.setValue(task.getResult());

                        Log.e("DriverSignup", "Token: " + userId);
                    }

                    else
                        Log.e("DriverSignup", "Can't send token to firebase database: " + task.getException());
                });
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(DriversFirebaseMessagingService.INTENT_FILTER)) {
                String clientKey = intent.getExtras().getString("ClientKey");
                String transactionId = intent.getExtras().getString("TransactionId");
                String clientFCMToken = intent.getExtras().getString("ClientToken");

                Bundle bundle = new Bundle();
                bundle.putString("transactionId", transactionId);
                bundle.putString("driverKey", userId);
                bundle.putString("clientFCMToken", clientFCMToken);

                Fragment messageNotificationFragment = new MessageNotificationFragment();
                messageNotificationFragment.setArguments(bundle);

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.notification_fragment, messageNotificationFragment)
                        .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                        .setReorderingAllowed(true)
                        .commit();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        getLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;

            updateLocationUI();
            getCurrentDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;

                updateLocationUI();
                getCurrentDeviceLocation();
            }
        }
    }

    private void updateLocationUI() {
        if (mMap == null) return;
        try {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getCurrentDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), CURRENT_LOCATION_ZOOM));
                        }
                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    public void onCameraIdle() {
        LatLng targetCoordinates = mMap.getCameraPosition().target;

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverLocation");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(targetCoordinates.latitude, targetCoordinates.longitude));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverLocation");
//
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
    }
}