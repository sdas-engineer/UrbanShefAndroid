package com.urbanshef.urbanshefapp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.urbanshef.urbanshefapp.AppDatabase;
import com.urbanshef.urbanshefapp.BasketItemClickListener;
import com.urbanshef.urbanshefapp.OnDeleteCallBack;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.SwipeToDeleteCallback;
import com.urbanshef.urbanshefapp.activities.ConfirmOrder;
import com.urbanshef.urbanshefapp.activities.MealDetailActivity;
import com.urbanshef.urbanshefapp.adapters.CartListAdapter;
import com.urbanshef.urbanshefapp.objects.Basket;
import com.urbanshef.urbanshefapp.utils.CommonMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class BasketFragment extends BaseFragment implements OnDeleteCallBack, View.OnClickListener, BasketItemClickListener {
    private AppDatabase db;
    private ArrayList<Basket> basketList = new ArrayList<>();
    private CartListAdapter adapter;

    TextView totalView;
    TextView delivery_charge;
    TextView service_fee;
    RecyclerView recyclerView;
    Button buttonAddPayment;
    Context context;
    LinearLayout linearLayout;
    EditText couponTextField;
    ViewGroup discountContainer;
    TextView discountTitle;
    TextView discountAmountLabel;

    public BasketFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        recyclerView = view.findViewById(R.id.basket_list);
        linearLayout = view.findViewById(R.id.basket_layout);
        delivery_charge = view.findViewById(R.id.delivery_charge);
        service_fee = view.findViewById(R.id.service_fee);
        totalView = view.findViewById(R.id.basket_total);
        buttonAddPayment = view.findViewById(R.id.button_add_payment);
        couponTextField = view.findViewById(R.id.et_coupon);
        discountContainer = view.findViewById(R.id.ll_discount_amount);
        discountTitle = view.findViewById(R.id.discount_title);
        discountAmountLabel = view.findViewById(R.id.discount);
        view.findViewById(R.id.btn_apply_coupon).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialise DB
        db = AppDatabase.getAppDatabase(getContext());
        listBasket();
        adapter = new CartListAdapter(context, basketList, this::onDelete, this::onBasketItemClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        handleAddPayment();
    }

    @SuppressLint("StaticFieldLeak")
    private void listBasket() {
        List<Basket> baskets = db.basketDao().getAll();
        if (!baskets.isEmpty()) {
            // Refresh our listview
            basketList.clear();
            basketList.addAll(baskets);
            applyDiscountAndRecalculate(false, 0);
        } else {
            // Display a message
            TextView alertText = new TextView(getActivity());
            alertText.setText("Your basket is empty. Please order a meal");
            alertText.setTextSize(17);
            alertText.setGravity(Gravity.CENTER);
            alertText.setLayoutParams(
                    new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    ));
            linearLayout.removeAllViews();
            linearLayout.addView(alertText);
        }
    }


    private void confirmDialog(int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.lyt_dialog_delete, null);
        alertDialog.setView(inflate);
        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog1.show();
        Button btnLogout = inflate.findViewById(R.id.btnRemove);
        Button btnNo = inflate.findViewById(R.id.btnNo);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.dismiss();
                Basket basket = basketList.get(position);
                AppDatabase.getAppDatabase(context).basketDao().delete(basket);
                basketList.remove(position);
                adapter.notifyDataSetChanged();
                updateUi();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.dismiss();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateUi() {
        listBasket();
    }


    private void handleAddPayment() {
        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double totalMealsCost = Double.parseDouble(totalView.getText().toString().replace("£", ""));
                if (discountContainer.getVisibility() == View.VISIBLE) {
                    totalMealsCost += Double.parseDouble(discountAmountLabel.getText().toString().replace("£", ""));
                }
                if (totalMealsCost < 10) {
                    Toast.makeText(context, getString(R.string.msg_min_meal_price), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getContext(), ConfirmOrder.class);
                    intent.putExtra("chefId", basketList.get(0).getChefId());
                    ArrayList<HashMap<String, Integer>> orderDetails = new ArrayList<HashMap<String, Integer>>();
                    for (Basket basket : basketList) {
                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put("meal_id", Integer.parseInt(basket.getMealId()));
                        map.put("quantity", basket.getMealQuantity());
                        orderDetails.add(map);
                    }
                    intent.putExtra("orderDetails", new Gson().toJson(orderDetails));

                    String serviceCharges = service_fee.getText().toString().replace("£", "");
                    intent.putExtra("serviceCharge", serviceCharges);

                    String deliveryCharges = delivery_charge.getText().toString().replace("£", "");
                    intent.putExtra("deliveryCharge", deliveryCharges);

                    intent.putExtra("coupon_code", couponTextField.getText().toString());

                    intent.putExtra("deliveryTime", basketList.get(0).getDeliveryTime());

                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDelete(int position) {
        confirmDialog(position);
    }

    private void getCouponDetails(final String couponCode) {
        showProgressDialog(getString(R.string.please_wait), getString(R.string.applying_coupon));

        String url = getString(R.string.API_URL) + "/customer/order/coupon/";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("code", couponCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonRequest,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                hideProgressDialog();
                                try {
                                    boolean isActive = response.getBoolean("active");
                                    if (isActive) {
                                        String validFrom = response.getString("valid_from");
                                        String validTo = response.getString("valid_to");
                                        applyDiscountAndRecalculate(true, response.getInt("discount"));
                                    }
                                } catch (JSONException ex) {
                                    Log.e("JSONException", "" + ex.toString());
                                }
                            }
                        }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        // TODO: Handle error
                        Toast.makeText(context, getString(R.string.msg_invalid_coupon_code), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }

    private void applyDiscountAndRecalculate(boolean applyDiscount, int discountPercent) {
        View rootView = getView();
        if (rootView != null) {
            // Calculate the total
            float subTotal = 0;
            float serviceCharges = 0;

            for (Basket basket : basketList) {
                subTotal += (basket.getMealQuantity() * basket.getMealPrice());
            }
            subTotal = subTotal + 3;
            serviceCharges = (float) (0.10 * subTotal);
            service_fee.setText(String.format(Locale.getDefault(), "£%.2f", serviceCharges));
            delivery_charge.setText(String.format(Locale.getDefault(), "£%.2f", 3.0));
            subTotal = subTotal + serviceCharges;
            totalView.setText(String.format(Locale.getDefault(), "£%.2f", subTotal));

            if (applyDiscount) {
                rootView.findViewById(R.id.ll_discount_amount).setVisibility(View.VISIBLE);
                double discountAmount = subTotal * (discountPercent / 100.0f);
                double updatedSubTotal = subTotal - discountAmount;
                discountTitle.setText(String.format(Locale.getDefault(), "Discount (%d%%)", discountPercent));
                discountAmountLabel.setText(String.format(Locale.getDefault(), "£%.2f", discountAmount));
                totalView.setText(String.format(Locale.getDefault(), "£%.2f", updatedSubTotal));
            } else {
                rootView.findViewById(R.id.ll_discount_amount).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_apply_coupon:
                CommonMethods.hideKeyboard(getActivity());
                if (couponTextField.getText().toString().isEmpty()) {
                    Toast.makeText(context, getString(R.string.msg_coupon_can_not_be_empty), Toast.LENGTH_SHORT).show();
                } else {
                    getCouponDetails(couponTextField.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onBasketItemClick(Basket basket) {
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            Intent intent = new Intent(parentActivity, MealDetailActivity.class);
            intent.putExtra("chefId", basket.getChefId());
            intent.putExtra("mealId", basket.getMealId());
            intent.putExtra("mealName", basket.getMealName());
            intent.putExtra("mealDescription", basket.getMealDescription());
            intent.putExtra("mealPrice", basket.getMealPrice());
            intent.putExtra("mealImage", basket.getMealImage());
            parentActivity.startActivity(intent);
        }
    }
}