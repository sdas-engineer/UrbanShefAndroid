package com.urbanshef.urbanshefapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.activities.CustomerMainActivity;
import com.urbanshef.urbanshefapp.adapters.ChefAdapter;
import com.urbanshef.urbanshefapp.objects.Chef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChefListFragment extends Fragment {


    private ArrayList<Chef> chefArrayList;
    private ArrayList<Chef> OrigArrayList=new ArrayList<>();
    private ChefAdapter adapter;

    ListView chefListView;
    TextView txtEmpty;

    public ChefListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chef_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chefArrayList = new ArrayList<Chef>();
        adapter = new ChefAdapter(this.getActivity(), chefArrayList);

        chefListView = (ListView) getActivity().findViewById(R.id.chef_list);
        txtEmpty=getActivity().findViewById(R.id.txtViewEmpty);
        chefListView.setAdapter(adapter);

        // Get list of chefs
        getChefs();

        // Add the Search function
        addSearchFunction();
    }

    private void getChefs() {
        String url = getString(R.string.API_URL) + "/customer/chefs/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CHEFS LIST", response.toString());

                        // Convert JSON data to JSON Array
                        JSONArray chefsJSONArray = null;

                        try
                        {
                            chefsJSONArray = response.getJSONArray("chefs");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Convert Json Array to Chef Array
                        Gson gson = new Gson();
                        Chef[]  chefs = gson.fromJson(chefsJSONArray.toString(), Chef[].class);

                        // Refresh ListView with up-to-date data
                        chefArrayList.clear();
                        OrigArrayList.clear();

                        Location locationA=new Location("");
                        locationA.setLatitude(CustomerMainActivity.UserLatLng.latitude);
                        locationA.setLongitude(CustomerMainActivity.UserLatLng.longitude);
                        for(Chef chef:chefs)
                        {
                            try
                            {
                                Location locationB=new Location("");

                                double lat=Double.parseDouble(chef.getChefStreetAddress().getLatitude());
                                double lng=Double.parseDouble(chef.getChefStreetAddress().getLongitude());

                                locationB.setLatitude(lat);
                                locationB.setLongitude(lng);

                                if(shouldAdd(locationA,locationB))
                                {
                                    chefArrayList.add(chef);
                                    OrigArrayList.add(chef);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        txtEmpty.setVisibility(chefArrayList.isEmpty()?View.VISIBLE:View.GONE);
                        chefListView.setVisibility(chefArrayList.isEmpty()?View.GONE:View.VISIBLE);

                        //chefArrayList.addAll(new ArrayList<Chef>(Arrays.asList(chefs)));
                        adapter.notifyDataSetChanged();
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




    public boolean shouldAdd(Location A,Location B)   //This Function Returns Full Address In Text Format If We Provide it Lat And Long Of Some Palce
    {
        float km = A.distanceTo(B)/1000;
        System.out.println("DISTANCE : "+km);
        return km * 0.621371 <= 6;
    }

    private void addSearchFunction() {
        EditText searchInput = (EditText) getActivity().findViewById(R.id.chef_search);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("SEARCH", charSequence.toString());

                // Update the Chef List
                chefArrayList.clear();
                for (Chef chef : OrigArrayList) {
                    if (chef.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        chefArrayList.add(chef);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
