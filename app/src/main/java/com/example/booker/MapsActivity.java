package com.example.booker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * controls map viewing: either read only or edit mode
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Task<Location> locationTask;
    private GoogleMap mMap;
    private Marker marker;
    private Button buttonConfirm;
    private FirebaseFirestore db;
    private Book book;
    private TextView textViewPermission;
    private TextView textViewAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // initialize everything
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        buttonConfirm = findViewById(R.id.buttonConfirm);

        textViewPermission = findViewById(R.id.textViewPermission);
        textViewAddress = findViewById(R.id.textViewAddress);

        // set up firestore
        db = FirebaseFirestore.getInstance();

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
     * checks if permission is already granted or asks permission
     */
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

    /**
     * pop up screen for selecting location permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted successfully, continue with other tasks
            }
        }
    }

    /**
     * https://www.youtube.com/watch?v=Ak1O9Gip-pg
     * gets the user's current location and moves the camera to it
     * @param smoothTransition
     */
    private void getCurrentLocation(final boolean smoothTransition) {
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (smoothTransition) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
                    }
                    else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f));
                    }
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
        // check for location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted yet
            getLocationPermission();
            return;
        }
        locationTask = fusedLocationProviderClient.getLastLocation();

        // hide text permission screen
        textViewPermission.setVisibility(View.GONE);

        String accessType = getIntent().getStringExtra("accessType");

        // get book
        book = (Book) getIntent().getSerializableExtra("book");

        mMap = googleMap;

        // if book has corrdinates, set a marker and move to it
        if (book.hasCoordinates()) {
            LatLng latLng = new LatLng(book.getLatitude(), book.getLongitude());

            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Retrieval Point"));
            setAddressBarText();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f));
        }

        // no marker placed, go to current location
        if (marker == null) {
            getCurrentLocation(false);
        }

        // read only disables editing functionality
        buttonConfirm.setVisibility(View.GONE); // hide confirm button
        mMap.getUiSettings().setAllGesturesEnabled(false); // disable map movement
        mMap.getUiSettings().setZoomControlsEnabled(true); // enable zoom

        // borrower map view type
        if (accessType.equals("WRITE")) {
            // enable my location button and enable movement gestures
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);

            // enable confirm button if marker placed
            if (marker != null) {
                buttonConfirm.setVisibility(View.VISIBLE);
            }

            // current location button listener, go to current location
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    getCurrentLocation(true);
                    return false;
                }
            });

            // map click listener, place a marker
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    // remove last marker
                    if (marker != null) {
                        marker.remove();
                    }
                    // enable confirm button and place new marker
                    buttonConfirm.setVisibility(View.VISIBLE);
                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Retrieval Point"));
                    setAddressBarText();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
                }
            });

            // confirm button lister
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get book's data and current marker's position
                    Book book = (Book) getIntent().getSerializableExtra("book");
                    Double latitude = marker.getPosition().latitude;
                    Double longitude = marker.getPosition().longitude;

                    // set book's location to current marker's location
                    book.setCoordinates(Arrays.asList(latitude, longitude));

                    // save new book's location
                    HashMap<String, Object> data = book.getDataHashMap();
                    db.collection("Books").document(book.getUID()).set(data);

                    finish();
                }
            });
        }
    }

    /**
     * https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
     * sets the address bar text using the marker's current position
     */
    private void setAddressBarText() {
        // initialize geocoder
        Geocoder geocoder;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addressList = null;

        // get latitude and longitude from marker
        Double latitude = marker.getPosition().latitude;
        Double longitude = marker.getPosition().longitude;

        // try to get first address from latitude and longitude
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get first address line as a string
        if (!addressList.isEmpty()) {
            String address = addressList.get(0).getAddressLine(0);
            textViewAddress.setText(address);
        }

    }
}