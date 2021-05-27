package com.urbanshef.urbanshefapp.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.urbanshef.urbanshefapp.AppDatabase;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.objects.Basket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MealDetailActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        Intent intent = getIntent();
        final String chefId = intent.getStringExtra("chefId");
        final String mealId = intent.getStringExtra("mealId");
        final String mealName = intent.getStringExtra("mealName");
        String mealDescription = intent.getStringExtra("mealDescription");
        final Float mealPrice = intent.getFloatExtra("mealPrice", 0);
        String mealImage = intent.getStringExtra("mealImage");

        getSupportActionBar().setTitle(mealName);

        TextView name = (TextView) findViewById(R.id.meal_name);
        final Button price = findViewById(R.id.meal_price);
        ImageView image = (ImageView) findViewById(R.id.meal_picture);

        name.setText(mealName);
//        desc.setText(mealDescription);
        price.setText(String.format(Locale.getDefault(), "£%.2f", mealPrice));
        if (mealImage != null && !mealImage.isEmpty()) {
            Picasso.get().load(mealImage).fit().centerCrop().into(image);
        }

        // Declare buttons
        final TextView labelQuantity = (TextView) findViewById(R.id.label_quantity);
        Button buttonIncrease = (Button) findViewById(R.id.button_increase);
        Button buttonDecrease = (Button) findViewById(R.id.button_decrease);
        Button buttonBasket = (Button) findViewById(R.id.button_add_basket);

        // Handle Button Increase Click
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());

                qty = qty + 1;
                labelQuantity.setText(qty + "");
                price.setText(String.format(Locale.getDefault(), "£%.2f", qty * mealPrice));

            }
        });

        // Handle Button Decrease Click
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                if (qty > 1) {
                    qty = qty - 1;
                    labelQuantity.setText(qty + "");
                    price.setText(String.format(Locale.getDefault(), "£%.2f", qty * mealPrice));
                }
            }
        });

        // Initialize DB
        db = AppDatabase.getAppDatabase(this);

        // Handle Button Add To Basket Click
        buttonBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                validateBasket(mealId, mealName, mealPrice, qty, chefId, mealDescription, mealImage);
            }
        });

        getAllergensDetails(mealId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("StaticFieldLeak")
    private void insertBasket(final String mealId, final String mealName, final float mealPrice, final int mealQty, final String chefId, final String mealDescription, final String mealImage) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Basket basket = new Basket();
                basket.setMealId(mealId);
                basket.setMealName(mealName);
                basket.setMealPrice(mealPrice);
                basket.setMealQuantity(mealQty);
                basket.setChefId(chefId);
                basket.setMealDescription(mealDescription);
                basket.setMealImage(mealImage);

                db.basketDao().insertAll(basket);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "MEAL ADDED", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (R.id.basket_button == id) {
            Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
            intent.putExtra("screen", "basket");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteBasket() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.basketDao().deleteAll();
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateBasket(final int basketId, final int mealQty) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.basketDao().updateBasket(basketId, mealQty);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "BASKET UPDATED", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void validateBasket(final String mealId, final String mealName, final float mealPrice, final int mealQuantity, final String chefId, final String mealDescription, final String mealImage) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                List<Basket> allBasket = db.basketDao().getAll();

                if (allBasket.isEmpty() || allBasket.get(0).getChefId().equals(chefId)) {
                    Basket basket = db.basketDao().getBasket(mealId);

                    if (basket == null) {
                        // Meal doesn't exist
                        return "NOT_EXIST";
                    } else {
                        // Meal exist in current basket
                        return basket.getId() + "";
                    }
                } else {
                    // Order meal from other chef
                    return "DIFFERENT_CHEF";
                }
            }

            @Override
            protected void onPostExecute(final String result) {
                super.onPostExecute(result);

                if (result.equals("DIFFERENT_CHEF")) {
                    // Show an alert
                    AlertDialog.Builder builder = new AlertDialog.Builder((MealDetailActivity.this));
                    builder.setTitle("Start New Basket?");
                    builder.setMessage("You're ordering meal from another chef. Would you like to clear the current basket?");
                    builder.setPositiveButton("Cancel", null);
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteBasket();
                            insertBasket(mealId, mealName, mealPrice, mealQuantity, chefId, mealDescription, mealImage);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else if (result.equals("NOT_EXIST")) {
                    insertBasket(mealId, mealName, mealPrice, mealQuantity, chefId, mealDescription, mealImage);

                } else {
                    // Show an alert
                    AlertDialog.Builder builder = new AlertDialog.Builder((MealDetailActivity.this));
                    builder.setTitle("Add More?");
                    builder.setMessage("Your basket already has this meal. Do you want to add more?");
                    builder.setPositiveButton("No", null);
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            updateBasket(Integer.parseInt(result), mealQuantity);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        }.execute();
    }

    private void getAllergensDetails(String mealId) {
        String url = String.format("%s/customer/allergens/%s/", getString(R.string.API_URL), mealId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray allergens = response.getJSONArray("allergens");
                            if (allergens.length() > 0) {
                                StringBuilder allergensContents = new StringBuilder();
                                for (int i = 0; i < allergens.length(); i++) {
                                    allergensContents.append(allergens.getString(i) + (i == allergens.length() -1 ? "" : ", "));
                                }
                                TextView description = findViewById(R.id.description);
                                String resultString = String.format(Locale.getDefault(), "%s\n\nContains : %s", description.getText().toString(), allergensContents);
                                ((TextView) findViewById(R.id.description)).setText(resultString);
                            }
                        } catch (JSONException ex) {
                            Log.e("JSONException", "" + ex.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "" + error.toString());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
