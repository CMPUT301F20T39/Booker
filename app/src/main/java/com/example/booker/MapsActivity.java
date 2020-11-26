package com.example.booker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    Task<Location> locationTask;
    private GoogleMap mMap;
    private Marker marker;
    private Button buttonConfirm;
    private FirebaseFirestore db;
    private Book book;
    private TextView textViewPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        textViewPermission = findViewById(R.id.textViewPermission);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set up firestore
        db = FirebaseFirestore.getInstance();

        // get book
        book = (Book) getIntent().getSerializableExtra("book");

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar5);
        // toolbar back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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



        Float latitude = 0.0f;
        Float longitude = 0.0f;
        if (book.hasCoordinates()) {
            latitude = book.getLatitude();
            longitude = book.getLongitude();
        }

        // Add a marker and move the camera
        LatLng position = new LatLng(latitude, longitude);
        marker = mMap.addMarker(new MarkerOptions().position(position).title("Retrieval Point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10.0f));

        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setVisibility(View.GONE);

        mMap.getUiSettings().setAllGesturesEnabled(false); // disable map movement
        mMap.getUiSettings().setZoomControlsEnabled(true); // enable zoom controls

        String accessType = getIntent().getStringExtra("accessType");

        if (accessType.equals("WRITE")) {

            // check for location permission
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // permission not granted yet
                textViewPermission.setVisibility(View.VISIBLE);
                getLocationPermission();
                return;
            }
            else {
                // permission already granted
                textViewPermission.setVisibility(View.GONE);
                mMap.setMyLocationEnabled(true);
                buttonConfirm.setVisibility(View.VISIBLE);
                mMap.getUiSettings().setAllGesturesEnabled(true);

                // current location button listener, go to current location
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        getCurrentLocation();
                        return false;
                    }
                });

                // map click listener, place a marker
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (marker != null) {
                            marker.remove();
                        }
                        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Retrieval Point"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
                    }
                });

                // confirm button lister
                buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Book book = (Book) getIntent().getSerializableExtra("book");
                        Float latitude = (float) marker.getPosition().latitude;
                        Float longitude = (float) marker.getPosition().longitude;

                        book.setCoordinates(Arrays.asList(latitude, longitude));

                        HashMap<String, Object> data = book.getDataHashMap();

                        db.collection("Books").document(book.getUID()).set(data);

                        finish();
                    }
                });
            }
            // get location permission (check if granted)
            locationTask = fusedLocationProviderClient.getLastLocation();
        }
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission already granted, no need to ask again
        } else {
            // ask permission
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted successfully, continue with other tasks
            }
        }
    }

    // https://www.youtube.com/watch?v=Ak1O9Gip-pg
    private void getCurrentLocation() {
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
                }
            }
        });
    }
}