package com.example.booker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseUser user;
    private Marker marker;
    private Button buttonConfirm;
    private FirebaseFirestore db;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        String accessType = getIntent().getStringExtra("accessType");

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

        if (accessType.equals("WRITE")) {
            buttonConfirm.setVisibility(View.VISIBLE);

            mMap.getUiSettings().setAllGesturesEnabled(true); // enable map movement

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
        }

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
}