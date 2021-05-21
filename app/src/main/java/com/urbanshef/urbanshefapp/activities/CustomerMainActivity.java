package com.urbanshef.urbanshefapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.fragments.BasketFragment;
import com.urbanshef.urbanshefapp.fragments.ChefListFragment;
import com.urbanshef.urbanshefapp.fragments.OrderFragment;
import com.urbanshef.urbanshefapp.utils.CircleTransform;
import com.urbanshef.urbanshefapp.utils.LocationProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomerMainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private SharedPreferences sharedPref;
    TextView txtMyLocation;
    LocationProvider locationProvider;
    public  static LatLng UserLatLng;
    static  boolean needsToUpdateLocation=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        needsToUpdateLocation=true;
        loadLocation();
    }

    private void intialize ()
    {

        Toolbar toolbar = findViewById(R.id.toolbar);
        txtMyLocation=findViewById(R.id.txtMyLocation);

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        int id = menuItem.getItemId();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        if (id == R.id.nav_chef) {
                            transaction.replace(R.id.content_frame, new ChefListFragment()).commit();
                        } else if (id == R.id.nav_basket) {
                            transaction.replace(R.id.content_frame, new BasketFragment()).commit();
                        } else if (id == R.id.nav_order) {
                            transaction.replace(R.id.content_frame, new OrderFragment()).commit();
                        } else if (id == R.id.nav_logout) {
                            logoutToServer(sharedPref.getString("token", ""));
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.remove("token");
                            editor.apply();

                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            startActivity(intent);
                        }

                        return true;
                    }
                });

        Intent intent = getIntent();
        String screen = intent.getStringExtra("screen");

        if (Objects.equals(screen, "basket")) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new BasketFragment()).commit();
        } else if (Objects.equals(screen, "order")) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new OrderFragment()).commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new ChefListFragment()).commit();
        }

        // Get the User's info
        sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        ImageView customer_avatar = (ImageView) header.findViewById(R.id.customer_avatar);
        TextView customer_name = (TextView) header.findViewById(R.id.customer_name);

        customer_name.setText(sharedPref.getString("name", ""));
        Picasso.get().load(sharedPref.getString("avatar", "")).transform(new CircleTransform()).into(customer_avatar);

    }

    private void loadLocation()
    {
        ProgressDialog pb=new ProgressDialog(this);
        pb.setCancelable(false);
        pb.show();
        locationProvider = new LocationProvider.Builder(this)
                .setInterval(5000)
                .setFastestInterval(2000)
                .setListener(new LocationProvider.MLocationCallback()
                {

                    @Override
                    public void onGoogleAPIClient(GoogleApiClient googleApiClient, String message)
                    {

                    }

                    @Override
                    public void onLocationUpdated(double latitude, double longitude)
                    {

                        if(!needsToUpdateLocation)
                            return;


                        if(pb.isShowing())
                        {
                            pb.dismiss();
                            intialize();
                        }
                        UserLatLng=new LatLng(latitude,longitude);
                        Geocoder coder = new Geocoder(CustomerMainActivity.this);
                        try {
                            ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocation(
                                    latitude, longitude, 1
                            );

                            if (!addresses.isEmpty())
                            {
                                txtMyLocation.setText(addresses.get(0).getAddressLine(0));
                                txtMyLocation.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                txtMyLocation.setSelected(true);
                                txtMyLocation.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        startActivityForResult(new Intent(CustomerMainActivity.this,MapsActivity.class).putExtra("Lat",latitude).putExtra("Long",longitude),001);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLocationUpdateRemoved()
                    {

                    }

                }).build();


        getLifecycle().addObserver(locationProvider);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==001)
        {
            if(resultCode==RESULT_OK)
            {
                locationProvider.removeUpdates();
                needsToUpdateLocation=false;
                txtMyLocation.setText(data.getExtras().getString("NewLocation"));
            }
        }


    }

    @Override
    public void onBackPressed() {

    }

    private void logoutToServer(final String token) {
        String url = getString(R.string.API_URL) + "/social/revoke-token";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("RESPONSE FROM SERVER", response.toString());
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", token);
                params.put("client_id", getString(R.string.CLIENT_ID));
                params.put("client_secret", getString(R.string.CLIENT_SECRET));

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }
}
