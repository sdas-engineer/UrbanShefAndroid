package com.urbanshef.urbanshefapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.adapters.MealAdapter;
import com.urbanshef.urbanshefapp.objects.Meal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MealListActivity extends AppCompatActivity {

    private ArrayList<Meal> mealArrayList;
    private MealAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_list);

        Intent intent = getIntent();
        String chefId = intent.getStringExtra("chefId");
        String chefName = intent.getStringExtra("chefName");

        getSupportActionBar().setTitle(chefName);

        mealArrayList = new ArrayList<Meal>();
        adapter = new MealAdapter(this, mealArrayList, chefId);

        ListView listView = (ListView) findViewById(R.id.meal_list);
       // listView.setAdapter(adapter);

        // Get Meals list
        getMeals(chefId);
    }

    private void getMeals(String chefId) {
        String url = String.format("%s/customer/meals/%s/", getString(R.string.API_URL), chefId);

        System.out.println("URL : "+url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MEAL LIST", response.toString());

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

                        // Refresh ListView with up-to-date data
                        mealArrayList.clear();
                        mealArrayList.addAll(new ArrayList<Meal>(Arrays.asList(meals)));
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERRRROR :::::"+error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
