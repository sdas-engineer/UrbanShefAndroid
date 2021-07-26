package com.urbanshef.urbanshefapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.adapters.BasketAdapter;
import com.urbanshef.urbanshefapp.objects.Basket;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class OrderFragment extends Fragment implements OnMapReadyCallback {

    private ArrayList<Basket> basketList;
    private BasketAdapter adapter;
    private Button statusView;

    private GoogleMap mMap;
    private Timer timer = new Timer();
    private Marker driverMarker;
    private static final int DEFAULT_ZOOM = 15;

    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        basketList = new ArrayList<Basket>();
        adapter = new BasketAdapter(this.getActivity(), basketList);

        ListView listView = (ListView) view.findViewById(R.id.basket_list);
        listView.setAdapter(adapter);

        statusView = (Button) view.findViewById(R.id.status);

        // Get The Latest Order Data
        getLatestOrder();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.order_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Get the Driver's location
        getDriverLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getDriverLocation();
            }
        };

        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    private void drawRouteOnMap(JSONObject response) {

        try {
            String chefAddress = response.getJSONObject("chef").getString("chef_street_address");
            String orderAddress = response.getJSONObject("customer").getString("customer_street_address");

            Geocoder coder = new Geocoder(getActivity());
            ArrayList<Address> cheAddresses = (ArrayList<Address>) coder.getFromLocationName(chefAddress, 1);
            ArrayList<Address> ordAddresses = (ArrayList<Address>) coder.getFromLocationName(orderAddress, 1);

            if (!cheAddresses.isEmpty() && !ordAddresses.isEmpty()) {
                LatLng chefPos = new LatLng(cheAddresses.get(0).getLatitude(), cheAddresses.get(0).getLongitude());
                LatLng orderPos = new LatLng(ordAddresses.get(0).getLatitude(), ordAddresses.get(0).getLongitude());

                DrawRouteMaps.getInstance(getActivity(), "AIzaSyB6onafeeu7zrjPHi_FdAJkBNjR_RMEJV0").draw(chefPos, orderPos, mMap);
                DrawMarker.getInstance(getActivity()).draw(mMap, chefPos, R.drawable.pin_chef, "Chef Location");
                DrawMarker.getInstance(getActivity()).draw(mMap, orderPos, R.drawable.pin_customer, "Customer Location");

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

    private void getLatestOrder() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/customer/order/latest/?access_token=" + sharedPref.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("LATEST ORDER", response.toString());

                        // Get Order details in JSONArray type
                        JSONArray orderDetailsArray = null;
                        String status = "";

                        try {
                            orderDetailsArray = response.getJSONArray("order_details");
                            status = response.getString("status");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Check if the current user have no order, then show a message
                        if (orderDetailsArray == null || orderDetailsArray.length() == 0) {
                            TextView alertText = new TextView(getActivity());
                            alertText.setText(getString(R.string.you_have_no_order));
                            alertText.setTextSize(17);
                            alertText.setGravity(Gravity.CENTER);
                            alertText.setLayoutParams(
                                    new TableLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            1
                                    ));

                            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.order_layout);
                            linearLayout.removeAllViews();
                            linearLayout.addView(alertText);
                        }

                        // Add this to the ListView. Convert JSON object to Basket object
                        if (orderDetailsArray != null) {
                            for (int i = 0; i < orderDetailsArray.length(); i++) {
                                Basket basket = new Basket();
                                try {
                                    JSONObject orderDetail = orderDetailsArray.getJSONObject(i);
                                    basket.setMealName(orderDetail.getJSONObject("meal").getString("name"));
                                    basket.setMealPrice((float) orderDetail.getJSONObject("meal").getDouble("price"));
                                    basket.setMealQuantity(orderDetail.getInt("quantity"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                basketList.add(basket);
                            }

                            // Update the ListView with Order Details data
                            adapter.notifyDataSetChanged();
                        }

                        // Update Status View
                        statusView.setText(status);

                        // Show Chef and Customer on the map
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

    private void getDriverLocation() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/customer/driver/location/?access_token=" + sharedPref.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DRIVER LOCATION", response.toString());

                        try {
                            String[] location = response.getString("location").split(",");
                            String lat = location[0];
                            String lng = location[1];

                            LatLng driPos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                            try {
                                driverMarker.remove();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            driverMarker = mMap
                                    .addMarker(new MarkerOptions()
                                            .position(driPos)
                                            .title("Driver Location")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car)));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
