package com.urbanshef.urbanshefapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.urbanshef.urbanshefapp.R;

import java.io.IOException;
import java.util.ArrayList;

public class ConfirmOrder extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private GoogleMap mMap;

    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;

    private EditText customer_flat_number;
    private EditText customer_street_address;
    private EditText delivery_instructions;
    private EditText phone;
    Button buttonAddPayment;
    Context context;


    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        phone = findViewById(R.id.phone);

        // Address EditText
        customer_flat_number = findViewById(R.id.customer_flat_number);
        customer_street_address = findViewById(R.id.customer_street_address);
        delivery_instructions = findViewById(R.id.delivery_instructions);
        buttonAddPayment = findViewById(R.id.button_add_payment);
        context=this;


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.basket_map);
        mapFragment.getMapAsync(this);

        // Phone EditText

        // Handle Map Address
        handleMapAddress();

        // Handle Add Payment Button Click event
        handleAddPayment();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    // Get the last-know location of the device and set the position of the map.
                    getDeviceLocation();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Do other setup activities here too, as described elsewhere in this tutorial.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            getLocationPermission();
            getDeviceLocation();
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick (LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                mMap.addMarker(new MarkerOptions().position(latLng));

                // Set address field from the position on the map
                Geocoder coder=new Geocoder(ConfirmOrder.this);
                try
                {
                    ArrayList<Address> addresses=(ArrayList<Address>) coder.getFromLocation(
                            latLng.latitude , latLng.longitude , 1
                    );

                    if (!addresses.isEmpty())
                    {
                        customer_street_address.setText(addresses.get(0).getAddressLine(0));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api=Build.VERSION_CODES.M)
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                                ));

                                // Set address field from the position on the map
                                Geocoder coder = new Geocoder(ConfirmOrder.this);
                                try {
                                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocation(
                                            mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1
                                    );

                                    if (!addresses.isEmpty()) {
                                        customer_street_address.setText(addresses.get(0).getAddressLine(0));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void handleMapAddress() {
        customer_street_address.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_DONE) {
                    Geocoder coder = new Geocoder(getActivity());

                    try {
                        ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(textView.getText().toString(), 1);
                        if (!addresses.isEmpty()) {
                            double lat = addresses.get(0).getLatitude();
                            double lng = addresses.get(0).getLongitude();

                            LatLng pos = new LatLng(lat, lng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM));
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(pos));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
    }

    private Context getActivity () {
        return this;
    }

    private void handleAddPayment()
    {

        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (phone.getText().toString().equals("")) {


                    phone.setError("Phone number cannot be blank");

                }

                else if (customer_flat_number.getText().toString().equals("")) {

                    customer_flat_number.setError("Flat or building number cannot be blank");

                }

                else if (customer_street_address.getText().toString().equals("")) {

                    customer_street_address.setError("Full address number cannot be blank");

                }
                else {
                    Intent intent = new Intent(ConfirmOrder.this, PaymentActivity.class);
                    intent.putExtras(getIntent().getExtras());
//                    intent.putExtra("delivery_info", delivery_instructions.getText().toString());
                    intent.putExtra("phone", phone.getText().toString());
                    intent.putExtra("customer_flat_number", customer_flat_number.getText().toString());
                    intent.putExtra("customer_street_address", customer_street_address.getText().toString());
                    intent.putExtra("delivery_instructions", delivery_instructions.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }


}