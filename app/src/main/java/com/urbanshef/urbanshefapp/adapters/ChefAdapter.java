package com.urbanshef.urbanshefapp.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.activities.ChefProfile;
import com.urbanshef.urbanshefapp.objects.Chef;
import com.urbanshef.urbanshefapp.objects.Meal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class ChefAdapter extends BaseAdapter {

    private final Activity activity;
    private ArrayList<Chef> chefList;

    public ChefAdapter(Activity activity, ArrayList<Chef> chefList) {
        this.activity = activity;
        this.chefList = chefList;
    }

    @Override
    public int getCount() {
        return chefList.size();
    }

    @Override
    public Object getItem(int i) {
        return chefList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_chef, null);
        }

        final Chef chef = chefList.get(i);

        TextView cheName = (TextView) view.findViewById(R.id.meal_name);
        TextView chePlace = (TextView) view.findViewById(R.id.meal_price);
        ImageView chePicture = (ImageView) view.findViewById(R.id.meal_picture);

        cheName.setText(chef.getName());
            if(chef.getChefStreetAddress()!=null)
        chePlace.setText(chef.getChefStreetAddress().getPlace());
            loadMealImage(chef.getId(),chePicture);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ChefProfile.class);
                intent.putExtra("Chef",chef);
                activity.startActivity(intent);
            }
        });

        return view;
    }

    private void loadMealImage (Integer id , ImageView chePicture)
    {

        String url = String.format("%s/customer/meals/%s/",  activity.getString(R.string.API_URL), String.valueOf(id));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray mealsJSONArray = null;

                        try
                        {
                            mealsJSONArray = response.getJSONArray("meals");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Convert Json Array to Chef Array
                        Gson gson = new Gson();
                        Meal[] meals = gson.fromJson(mealsJSONArray.toString(), Meal[].class);


                        Picasso.get().load(meals[new Random().nextInt(meals.length)].getImage()).placeholder(R.drawable.ic_loading).error(R.drawable.blank_image)
                                .fit().centerCrop().into(chePicture);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Picasso.get().load(R.drawable.blank_image).fit().into(chePicture);
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(jsonObjectRequest);

    }
}

