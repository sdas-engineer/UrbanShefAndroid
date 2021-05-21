package com.urbanshef.urbanshefapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.utils.CircleTransform;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DeliveryFragment extends Fragment implements OnMapReadyCallback {

    private TextView customerName;
    private TextView customerFlat;
    private TextView customerPhone;
    private TextView customerAddress;
    private ImageView customerImage;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Marker driverMarker;

    private LocationCallback mLocationCallback;
    private String orderId;



    public DeliveryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delivery, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        customerName = (TextView) getActivity().findViewById(R.id.customer_name);
        customerFlat = (TextView) getActivity().findViewById(R.id.customer_flat_number);
        customerPhone = (TextView) getActivity().findViewById(R.id.customer_phone);
        customerAddress = (TextView) getActivity().findViewById(R.id.customer_street_address);
        customerImage = (ImageView) getActivity().findViewById(R.id.customer_image);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.delivery_map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Get the latest order details
        getLatestOrder();

        // Handle the Complete Order Button
        handleButtonCompleteOrder();
    }

    private void getLatestOrder() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/driver/order/latest/?access_token=" + sharedPref.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("GET LATEST ORDER", response.toString());

                        // Get Order details in JSONArray type
                        JSONObject latestOrderJSONObject = null;
                        orderId = null;
                        Boolean orderIsDelivered = null;

                        try {
                            latestOrderJSONObject = response.getJSONObject("order");

                            orderId = latestOrderJSONObject.getString("id");
                            orderIsDelivered = latestOrderJSONObject.getString("status").equals("Delivered");

                            customerName.setText(latestOrderJSONObject.getJSONObject("customer").getString("name"));
                            customerFlat.setText(latestOrderJSONObject.getString("customer_flat_number"));
                            customerPhone.setText(latestOrderJSONObject.getString("phone"));
                            customerAddress.setText(latestOrderJSONObject.getString("customer_street_address"));
                            Picasso.get()
                                    .load(latestOrderJSONObject.getJSONObject("customer").getString("avatar"))
                                    .transform(new CircleTransform())
                                    .into(customerImage);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Check if there are no outstanding order then display the message.
                        if (latestOrderJSONObject == null || orderId == null || orderIsDelivered) {
                            TextView alertText = new TextView(getActivity());
                            alertText.setText("You have no outstanding order.");
                            alertText.setTextSize(17);
                            alertText.setId(alertText.generateViewId());

                            ConstraintLayout constraintLayout = (ConstraintLayout) getActivity().findViewById(R.id.delivery_layout);
                            constraintLayout.removeAllViews();
                            constraintLayout.addView(alertText);

                            ConstraintSet set = new ConstraintSet();
                            set.clone(constraintLayout);
                            set.connect(alertText.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
                            set.connect(alertText.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
                            set.connect(alertText.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
                            set.connect(alertText.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);
                            set.applyTo(constraintLayout);
                        }

                        // Draw route between locations
                        drawRouteOnMap(response);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Promt the user for permission
        getLocationPermission();

        // Get the device's location and set the position of the map
        getDeviceLocation();

        // Listen location update
        startLocationUpdates();
    }

    private void drawRouteOnMap(JSONObject response) {

        try {
            String chefAddress = response.getJSONObject("order").getJSONObject("chef").getString("chef_street_address");
            String orderAddress = response.getJSONObject("order").getString("customer_street_address");

            Geocoder coder = new Geocoder(getActivity());
            ArrayList<Address> cheAddresses = (ArrayList<Address>) coder.getFromLocationName(chefAddress, 1);
            ArrayList<Address> ordAddresses = (ArrayList<Address>) coder.getFromLocationName(orderAddress, 1);

            if (!cheAddresses.isEmpty() && !ordAddresses.isEmpty()) {
                LatLng chefPos = new LatLng(cheAddresses.get(0).getLatitude(), cheAddresses.get(0).getLongitude());
                LatLng orderPos = new LatLng(ordAddresses.get(0).getLatitude(), ordAddresses.get(0).getLongitude());

                DrawRouteMaps.getInstance(getActivity(), "AIzaSyB6onafeeu7zrjPHi_FdAJkBNjR_RMEJV0").draw(chefPos, orderPos, mMap);
                DrawMarker.getInstance(getActivity()).draw(mMap, chefPos, R.drawable.pin_chef, "Chef Address");
                DrawMarker.getInstance(getActivity()).draw(mMap, orderPos, R.drawable.pin_customer, "Customer Address");

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(chefPos)
                        .include(orderPos).build();
                Point displaySize = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            DeliveryFragment.this.requestPermissions(
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
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                LatLng pos = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                driverMarker = mMap.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .title("My Location")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car))
                                );

                                updateDriverLocation(mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude()); // "-31.0032,153.000"
                            }
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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

                    // Listen location update
                    startLocationUpdates();
                }
            }
        }
    }


    private void startLocationUpdates() {

        try {

            if (mLocationPermissionGranted) {

                // STEP 1: Set up a location request
                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(500);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                // STEP 2: Define the location update callback
                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }

                        for (Location location : locationResult.getLocations()) {
                            // update UI with location data

                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                            try {
                                driverMarker.remove();
                            } catch (Exception e) {}

                            driverMarker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title("My Location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car))
                            );
                            updateDriverLocation(location.getLatitude() + "," + location.getLongitude());

                            Log.d("NEW DRIVER LOCATION:", Double.toString(pos.latitude) + "," + Double.toString(pos.longitude));

                        }
                    }
                };

                // STEP 3: Request location updates
                mFusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        null
                );
            }

        } catch (SecurityException e) {
            Log.d("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop locaiton updates
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void updateDriverLocation(final String location) {
        String url = getString(R.string.API_URL) + "/driver/location/update/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("UPDATE DRIVER LOCATION", response);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the Place Order Button
                        Log.d("ERROR MESSAGE", error.toString());

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPref.getString("token", ""));
                params.put("location", location);

                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }

    private void handleButtonCompleteOrder() {
        Button buttonCompleteOrder = (Button) getActivity().findViewById(R.id.button_complete_order);
        buttonCompleteOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Show an alert
                AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
                builder.setTitle("Complete Order");
                builder.setMessage("Is this order completed?");
                builder.setPositiveButton("Cancel", null);
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        completeOrder(orderId);
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void completeOrder(final String orderId) {
        String url = getString(R.string.API_URL) + "/driver/order/complete/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("ORDER COMPLETED", response);

                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.content_frame, new OrderListFragment()).commit();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the Place Order Button
                        Log.d("ERROR MESSAGE", error.toString());

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPref.getString("token", ""));
                params.put("order_id", orderId);

                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }
}