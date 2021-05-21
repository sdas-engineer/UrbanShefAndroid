package com.urbanshef.urbanshefapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.adapters.OrderAdapter;
import com.urbanshef.urbanshefapp.objects.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OrderListFragment extends Fragment {

    private OrderAdapter adapter;
    private ArrayList<Order> orderList;

    public OrderListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        orderList = new ArrayList<Order>();
        adapter = new OrderAdapter(this.getActivity(), orderList);

        ListView orderListView = (ListView) getActivity().findViewById(R.id.order_list);
        orderListView.setAdapter(adapter);

        // Get list of ready orders to be delivered
        getReadyOrders();
    }

    private void getReadyOrders() {
        String url = getString(R.string.API_URL) + "/driver/orders/ready/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("READY ORDERS LIST", response.toString());

                        // Get orders in JSONArray type
                        try {
                            JSONArray ordersJSONArray = response.getJSONArray("orders");
                            for (int i = 0;i < ordersJSONArray.length(); i++) {
                                JSONObject orderObject = ordersJSONArray.getJSONObject(i);

                                Order order = new Order(
                                        orderObject.getString("id"),
                                        orderObject.getJSONObject("chef").getString("name"),
                                        orderObject.getJSONObject("customer").getString("name"),
//                                        orderObject.getString("customer_flat_number"),
                                        orderObject.getString("customer_street_address"),
                                        orderObject.getString("phone"),
                                        orderObject.getJSONObject("customer").getString("avatar")
                                );
                                orderList.add(order);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Update the ListView with fresh data
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

}