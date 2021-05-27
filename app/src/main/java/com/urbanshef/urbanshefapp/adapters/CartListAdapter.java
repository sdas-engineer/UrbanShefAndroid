package com.urbanshef.urbanshefapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.urbanshef.urbanshefapp.OnDeleteCallBack;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.objects.Basket;
import com.urbanshef.urbanshefapp.BasketItemClickListener;

import java.util.ArrayList;
import java.util.Locale;

public  class CartListAdapter extends RecyclerView.Adapter<BasketViewHolder> {
    Context context;
    private ArrayList<Basket> basketList;
    OnDeleteCallBack onDeleteCallBack;
    BasketItemClickListener onItemClickListener;

    public CartListAdapter(Context context, ArrayList<Basket> basketList, OnDeleteCallBack onDeleteCallBack, BasketItemClickListener onItemClickListener) {
        this.context = context;
        this.basketList = basketList;
        this.onDeleteCallBack = onDeleteCallBack;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public BasketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BasketViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(BasketViewHolder viewHolder, int position) {
        Basket basket = basketList.get(position);
        viewHolder.mealName.setText(basket.getMealName());
        viewHolder.mealQuantity.setText(basket.getMealQuantity() + "");
        viewHolder.mealSubTotal.setText(String.format(Locale.getDefault(), "£%.2f", (basket.getMealPrice() * basket.getMealQuantity())));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onBasketItemClick(basket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketList.size();
    }

    public Context getContext() {
        return context;
    }

    public void deleteTask(int position) {
        onDeleteCallBack.onDelete(position);
    }
}


class BasketViewHolder extends RecyclerView.ViewHolder {
    TextView mealName;
    TextView mealQuantity;
    TextView mealSubTotal;

    public BasketViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_basket, parent, false));
        mealName = itemView.findViewById(R.id.basket_meal_name);
        mealQuantity = itemView.findViewById(R.id.basket_meal_quantity);
        mealSubTotal = itemView.findViewById(R.id.basket_meal_subtotal);
    }
}