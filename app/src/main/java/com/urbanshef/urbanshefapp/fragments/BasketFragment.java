package com.urbanshef.urbanshefapp.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.urbanshef.urbanshefapp.AppDatabase;
import com.urbanshef.urbanshefapp.OnDeleteCallBack;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.SwipeToDeleteCallback;
import com.urbanshef.urbanshefapp.activities.ConfirmOrder;
import com.urbanshef.urbanshefapp.adapters.CartListAdapter;
import com.urbanshef.urbanshefapp.objects.Basket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class BasketFragment extends Fragment implements OnDeleteCallBack {


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

        recyclerView = view.findViewById(R.id.basket_list);

        linearLayout = view.findViewById(R.id.basket_layout);
        delivery_charge = view.findViewById(R.id.delivery_charge);
        service_fee = view.findViewById(R.id.service_fee);
        totalView = view.findViewById(R.id.basket_total);
        buttonAddPayment = view.findViewById(R.id.button_add_payment);
        context = getContext();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialise DB
        db = AppDatabase.getAppDatabase(getContext());
        listBasket();
        adapter = new CartListAdapter(context, basketList, this::onDelete);
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
            // Calculate the total
            float total = 0;
            float serviceCharges = 0;


            for (Basket basket : baskets) {
                total += (basket.getMealQuantity() * basket.getMealPrice());

            }
            total = total + 3;
            serviceCharges = (float) (0.10 * total);
            service_fee.setText(String.format(Locale.getDefault(), "£%.2f", serviceCharges));
            delivery_charge.setText(String.format(Locale.getDefault(), "£%.2f", 3.0));
            total = total + serviceCharges;
            totalView.setText(String.format(Locale.getDefault(), "£%.2f", total));
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

                String totalMealsCost = totalView.getText().toString().replace("£", "");
                if (Float.parseFloat(totalMealsCost) < 10) {
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

                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDelete(int position) {
        confirmDialog(position);
    }
}