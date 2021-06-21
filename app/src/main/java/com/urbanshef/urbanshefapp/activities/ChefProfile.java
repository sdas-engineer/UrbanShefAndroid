package com.urbanshef.urbanshefapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.ImageUrlValidationListener;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.adapters.MealAdapter;
import com.urbanshef.urbanshefapp.objects.Chef;
import com.urbanshef.urbanshefapp.objects.Meal;
import com.urbanshef.urbanshefapp.utils.CircleTransform;
import com.urbanshef.urbanshefapp.utils.CommonMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ChefProfile extends AppCompatActivity implements OnMapReadyCallback {

    RecyclerView recyclerView;
    private ArrayList<Meal> mealArrayList;
    private MealAdapter adapter;
    Chef chef;
    ImageView chefImage;
    ImageView imgChef;
    TextView txtChefBio;
    TextView txtWhereToFind;
    TextView txtAddress;

    private GoogleMap mMap;

    TextView txtReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_profile);
        initViews();
        setVals();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.basket_map);
        mapFragment.getMapAsync(this);

    }

    private void setVals() {
        chef = (Chef) getIntent().getExtras().getSerializable("Chef");
        //getSupportActionBar().setTitle(chef.getName());
        mealArrayList = new ArrayList<Meal>();
        adapter = new MealAdapter(this, mealArrayList, chef);
        recyclerView.setAdapter(adapter);


        txtAddress.setText(chef.getChefStreetAddress().getPlace());
        txtChefBio.setText(chef.getBio());
        txtWhereToFind.setText("Where To Find " + chef.getName());

        CommonMethods.loadImageFromPath(chef.getPicture(), new ImageUrlValidationListener() {
            @Override
            public void imageUrlValidationSuccess(String imageUrl) {
                Picasso.get().load(imageUrl).fit().transform(new CircleTransform()).into(chefImage);
            }

            @Override
            public void imageUrlValidationFailure(String imageUrl) {

            }
        });

        getMeals(String.valueOf(chef.getId()));
        // getReview(String.valueOf(chef.getId()));
    }

    private void initViews() {
        txtAddress = findViewById(R.id.txtAddress);
        imgChef = findViewById(R.id.imgChef);
        chefImage = findViewById(R.id.chefImage);
        txtChefBio = findViewById(R.id.txtBio);
        txtWhereToFind = findViewById(R.id.txtwhereToFind);
        txtReview = findViewById(R.id.txtReview);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }


    private void getReview(String chefId) {
        String url = String.format("%s/chef/review/%s/", getString(R.string.API_URL), chefId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MEAL LIST", response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtReview.setText("No Review Found");
                        System.out.println("ERRRROR :::::" + error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }


    private void getMeals(String chefId) {
        String url = String.format("%s/customer/meals/%s/", getString(R.string.API_URL), chefId);

        System.out.println("URL : " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        String s = response.toString();

                        // Convert JSON data to JSON Array
                        JSONArray mealsJSONArray = null;

                        try {
                            mealsJSONArray = response.getJSONArray("meals");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Convert Json Array to Chef Array
                        Gson gson = new Gson();
                        Meal[] meals = gson.fromJson(mealsJSONArray.toString(), Meal[].class);


                        CommonMethods.loadImageFromPath(meals[new Random().nextInt(meals.length)].getImage(), new ImageUrlValidationListener() {
                            @Override
                            public void imageUrlValidationSuccess(String imageUrl) {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_loading).error(R.drawable.ic_default).fit().centerCrop().into(imgChef);
                            }

                            @Override
                            public void imageUrlValidationFailure(String imageUrl) {

                            }
                        });

                        // Refresh ListView with up-to-date data
                        mealArrayList.clear();
                        mealArrayList.addAll(new ArrayList<Meal>(Arrays.asList(meals)));
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERRRROR :::::" + error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng latLng = new LatLng(Double.parseDouble(chef.getChefStreetAddress().getLatitude()), Double.parseDouble(chef.getChefStreetAddress().getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.addMarker(new MarkerOptions().position(latLng).title(chef.getName()));
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}