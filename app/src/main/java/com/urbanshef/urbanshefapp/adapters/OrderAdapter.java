package com.urbanshef.urbanshefapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.activities.DriverMainActivity;
import com.urbanshef.urbanshefapp.fragments.DeliveryFragment;
import com.urbanshef.urbanshefapp.objects.Order;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Order> orderList;

    public OrderAdapter(Activity activity, ArrayList<Order> orderList) {
        this.activity = activity;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int i) {
        return orderList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_order, null);
        }

        final Order order = orderList.get(i);

        TextView chefName = (TextView) view.findViewById(R.id.meal_name);
        TextView customerName = (TextView) view.findViewById(R.id.customer_name);
//        TextView customerFlat = (TextView) view.findViewById(R.id.customer_flat_number);
        TextView customerAddress = (TextView) view.findViewById(R.id.customer_street_address);
        TextView customerPhone = (TextView) view.findViewById(R.id.customer_phone);
        ImageView customerImage = (ImageView) view.findViewById(R.id.customer_image);

        chefName.setText(order.getChefName());
        customerName.setText(order.getCustomerName());
//        customerFlat.setText(order.getCustomerFlat());
        customerAddress.setText(order.getCustomerAddress());
        customerPhone.setText(order.getCustomerPhone());
        Picasso.get().load(order.getCustomerImage()).fit().centerCrop().into(customerImage);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show an alert
                AlertDialog.Builder builder = new AlertDialog.Builder((activity));
                builder.setTitle("Pick this order?");
                builder.setMessage("Would you like to take this order?");
                builder.setPositiveButton("Cancel", null);
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(activity.getApplicationContext(), "ORDER PICKED", Toast.LENGTH_SHORT).show();

                        pickOrder(order.getId());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return view;
    }

    private void pickOrder(final String orderId) {
        String url = activity.getString(R.string.API_URL) + "/driver/order/pick/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("ORDER PICKED", response.toString());

                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            if (jsonObj.getString("status").equals("success")) {
                                FragmentTransaction transaction = ((DriverMainActivity) activity).getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.content_frame, new DeliveryFragment()).commit();
                            } else {
                                Toast.makeText(activity, jsonObj.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener () {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(activity, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPref = activity.getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
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

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(postRequest);
    }
}
