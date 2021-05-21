package com.urbanshef.urbanshefapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.OnDeleteCallBack;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.activities.MealDetailActivity;
import com.urbanshef.urbanshefapp.objects.Meal;

import java.util.ArrayList;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter {


    Context context;

    OnDeleteCallBack onDeleteCallBack;

    private final Activity activity;
    private final ArrayList<Meal> mealList;
    private final String chefId;

    public MealAdapter(Activity activity, ArrayList<Meal> mealList, String chefId) {
        this.activity = activity;
        this.mealList = mealList;
        this.chefId = chefId;
        this.context=activity;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ViewHolder viewHolder = (ViewHolder)holder;
        Meal meal = mealList.get(position);


        viewHolder.mealName.setText(meal.getName());
        viewHolder.mealPrice.setText(String.format(Locale.getDefault(), "£%.2f", meal.getPrice()));
        Picasso.get().load(meal.getImage()).placeholder(R.drawable.ic_loading).fit().centerCrop().into(viewHolder.mealImage);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MealDetailActivity.class);
                intent.putExtra("chefId", chefId);
                intent.putExtra("mealId", meal.getId());
                intent.putExtra("mealName", meal.getName());
                intent.putExtra("mealDescription", meal.getShort_description());
                intent.putExtra("mealPrice", meal.getPrice());
                intent.putExtra("mealImage", meal.getImage());
                activity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }





    public Context getContext()
    {

        return context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealName ;
        TextView mealPrice;
        ImageView mealImage;


        public ViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_meal, parent, false));
             mealName =itemView.findViewById(R.id.meal_name);
             mealPrice =itemView.findViewById(R.id.meal_price);
             mealImage =itemView.findViewById(R.id.meal_picture);

        }
    }


}



