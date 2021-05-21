package com.urbanshef.urbanshefapp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.objects.Basket;

import java.util.ArrayList;
import java.util.Locale;

public class BasketAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Basket> basketList;

    public BasketAdapter(Activity activity, ArrayList<Basket> basketList) {
        this.activity = activity;
        this.basketList = basketList;
    }

    @Override
    public int getCount() {
        return basketList.size();
    }

    @Override
    public Object getItem(int i) {
        return basketList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_basket, null);
        }

        TextView mealName = (TextView) view.findViewById(R.id.basket_meal_name);
        TextView mealQuantity = (TextView) view.findViewById(R.id.basket_meal_quantity);
        TextView mealSubTotal = (TextView) view.findViewById(R.id.basket_meal_subtotal);

        Basket basket = basketList.get(i);
        mealName.setText(basket.getMealName());
        mealQuantity.setText(basket.getMealQuantity() + "");
        mealSubTotal.setText(String.format(Locale.getDefault(), "£%.2f", (basket.getMealPrice() * basket.getMealQuantity())));


        return view;
    }

}
