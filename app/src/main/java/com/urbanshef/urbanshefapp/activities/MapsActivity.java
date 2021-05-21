// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.urbanshef.urbanshefapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.urbanshef.urbanshefapp.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Double myLat;
    Double myLong;
    Marker marker;
    LatLng mylatLong ;
    TextView txtDeliveryAddress;
    ImageView imgback;
    ImageView imgMyLocation;
    String CurrentAddress="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        myLat=getIntent().getExtras().getDouble("Lat");
        myLong=getIntent().getExtras().getDouble("Long");
        mylatLong= new LatLng(myLat, myLong);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imgback=findViewById(R.id.imgBack);
        imgMyLocation=findViewById(R.id.imgMyLocation);

        txtDeliveryAddress=findViewById(R.id.txtDeliveryAddress);
        txtDeliveryAddress.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        txtDeliveryAddress.setSelected(true);








        imgback.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        imgMyLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pickCurrentPlace();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MarkerOptions markerOptions = new MarkerOptions();
        marker=mMap.addMarker(markerOptions.position(mylatLong));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylatLong,15));

        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);


        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()
        {
            @Override
            public void onCameraIdle()
            {
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                LatLng newLatLong = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                loadAddressOnBackGround(newLatLong);
            }
        });
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()
        {
            @Override
            public void onCameraMove()
            {
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                LatLng newLatLong = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                marker.setPosition(newLatLong);
                txtDeliveryAddress.setText("Loading........");
            }
        });

    }

    private void loadAddressOnBackGround(LatLng newLatLong)
    {
        

        new AsyncTask<String, String, String>()

        {

            @Override
            protected String doInBackground(String... strings)
            {
                String address = getAddress(newLatLong.latitude, newLatLong.longitude);
                return address;
            }

            @Override
            protected void onPostExecute(String string)
            {

                CurrentAddress=string;
                txtDeliveryAddress.setText(CurrentAddress);
                super.onPostExecute(string);
            }
        }.execute();


    }


    public String getAddress(double lat, double lng)   //This Function Returns Full Address In Text Format If We Provide it Lat And Long Of Some Palce
    {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

        if(addresses.size()>=1)
        {
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            System.out.println("ADDRESS : "+obj.getLocality());
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            return  obj.getAddressLine(0);
        }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
           // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return  "";
    }


    private void pickCurrentPlace()
    {
        if (mMap == null) {
            return;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylatLong,15));
        marker.setPosition(mylatLong);


    }

    @Override
    public void onBackPressed()
    {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();

    }


    public void onClickDone(View view)
    {
        if(!CurrentAddress.isEmpty())
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("NewLocation",CurrentAddress);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else
        {
            Snackbar.make(findViewById(android.R.id.content), "Please Select Valid Location", BaseTransientBottomBar.LENGTH_LONG).show();
        }


    }
}