package com.urbanshef.urbanshefapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.urbanshef.urbanshefapp.OnDeleteCallBack;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.objects.Basket;

import java.util.ArrayList;
import java.util.Locale;

public  class CartListAdapter extends RecyclerView.Adapter {




    Context context;
    private ArrayList<Basket> basketList;

    OnDeleteCallBack onDeleteCallBack;

    public CartListAdapter(Context context,ArrayList<Basket> basketList, OnDeleteCallBack onDeleteCallBack)
    {
        this.context=context;
        this.basketList=basketList;
        this.onDeleteCallBack=onDeleteCallBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ViewHolder viewHolder = (ViewHolder)holder;
        Basket basket = basketList.get(position);
        viewHolder.mealName.setText(basket.getMealName());
        viewHolder.mealQuantity.setText(basket.getMealQuantity() + "");
        viewHolder.mealSubTotal.setText(String.format(Locale.getDefault(), "£%.2f", (basket.getMealPrice() * basket.getMealQuantity())));
    }

    @Override
    public int getItemCount() {
        return basketList.size();
    }





    public Context getContext()
    {

        return context;
    }

    public void deleteTask(int position)
    {

        onDeleteCallBack.onDelete(position);

    }

       }


 class ViewHolder extends RecyclerView.ViewHolder {

     TextView mealName ;
     TextView mealQuantity ;
     TextView mealSubTotal ;

     public ViewHolder(ViewGroup parent) {
         super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_basket, parent, false));

         mealName = itemView.findViewById(R.id.basket_meal_name);
         mealQuantity =  itemView.findViewById(R.id.basket_meal_quantity);
         mealSubTotal = itemView.findViewById(R.id.basket_meal_subtotal);

     }
}