package com.urbanshef.urbanshefapp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentOptionCallback;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.paymentsheet.PaymentSheetResultCallback;
import com.stripe.android.paymentsheet.model.PaymentOption;
import com.urbanshef.urbanshefapp.AppDatabase;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.utils.CommonMethods;
import com.urbanshef.urbanshefapp.utils.ProductFlavor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private String chefId, customer_flat_number, phone, customer_street_address, orderDetails, serviceCharge, deliveryCharge, deliveryInstructions, couponCode, preOrder;
//    private Button buttonPlaceOrder;

    private PaymentSheet.FlowController flowController;
    private Button paymentMethodButton;
    private Button buyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        getSupportActionBar().setTitle("");

        // Get Order Data
        Intent intent = getIntent();
        chefId = intent.getStringExtra("chefId");
        phone = intent.getStringExtra("phone");
        customer_flat_number = intent.getStringExtra("customer_flat_number");
        customer_street_address = intent.getStringExtra("customer_street_address");
        orderDetails = intent.getStringExtra("orderDetails");
        serviceCharge = intent.getStringExtra("serviceCharge");
        deliveryInstructions = intent.getStringExtra("delivery_instructions");
        couponCode = intent.getStringExtra("coupon_code");
        deliveryCharge = intent.getStringExtra("deliveryCharge");
        preOrder = intent.getStringExtra("pre_order");

        paymentMethodButton = (Button) findViewById(R.id.add_payment_method);
        buyButton = (Button) findViewById(R.id.button_place_order);
        PaymentConfiguration.init(
                getApplicationContext(),
                CommonMethods.getTargetStripeKey(this)
        );

        paymentMethodButton.setEnabled(false);
        buyButton.setEnabled(false);

        final PaymentOptionCallback paymentOptionCallback = this::onPaymentOption;

        final PaymentSheetResultCallback paymentSheetResultCallback = this::onPaymentSheetResult;

        flowController = PaymentSheet.FlowController.create(
                this,
                paymentOptionCallback,
                paymentSheetResultCallback
        );

        fetchInitData();

//        final CardInputWidget mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
//        buttonPlaceOrder = (Button) findViewById(R.id.button_place_order);
//        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("StaticFieldLeak")
//            @Override
//            public void onClick(View view) {
//                final SharedPreferences sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
//                String name = sharedPref.getString("name", "");
//                Card.Builder mBuilder = mCardInputWidget.getCardBuilder().name(name);
//                final Card card = mBuilder.build();
////                final Card card = mCardInputWidget.getCard();
//                if (card == null) {
//                    // Do not continue token creation.
//                    Toast.makeText(getApplicationContext(), "Card cannot be blank", Toast.LENGTH_LONG).show();
//                } else {
//
//                    // Disable the Place Order Button
//                    setButtonPlaceOrder("LOADING...", false);
//
//                    new AsyncTask<Void, Void, Void>() {
//                        @SuppressLint("WrongThread")
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//
//                            Stripe stripe = new Stripe(getApplicationContext(), getString(R.string.Stripe_Key));
//                            stripe.createCardToken(
//                                    card,
//                                    new ApiResultCallback<Token>() {
//                                        public void onSuccess(@NonNull Token token) {
//
//                                            // Make an order
//                                            addOrder(token.getId());
//                                        }
//
//                                        public void onError(Exception error) {
//                                            // Show localized error message
//                                            Toast.makeText(getApplicationContext(),
//                                                    error.getLocalizedMessage(),
//                                                    Toast.LENGTH_LONG
//                                            ).show();
//
//                                            // Enable the Place Order Button
//                                            setButtonPlaceOrder("PLACE ORDER", true);
//                                        }
//                                    }
//                            );
//
//                            return null;
//                        }
//                    }.execute();
//                }
//            }
//        });
    }

    private void addOrder(final String stripeToken) {
        String url = getString(R.string.API_URL) + "/customer/order/add/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("ORDER ADDED", response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("status").equals("success")) {
                                deleteBasket();

                                // Jump to the Order screen
                                Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                                intent.putExtra("screen", "order");
                                startActivity(intent);

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jsonObject.getString("error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Enable the Place Order Button
                        setButtonPlaceOrder("PLACE ORDER", true);

                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the Place Order Button
                        setButtonPlaceOrder("PLACE ORDER", true);

                        Toast.makeText(getApplicationContext(),
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPref.getString("token", ""));
                params.put("chef_id", chefId);
                params.put("stripe_token", stripeToken);
                params.put("delivery_charge", deliveryCharge);
                params.put("service_charge", serviceCharge);
                params.put("coupon", couponCode);
                params.put("customer_street_address", customer_street_address);
                params.put("customer_flat_number", customer_flat_number);
                params.put("phone", phone);
                params.put("order_details", orderDetails);
                params.put("delivery_instructions", deliveryInstructions);
                params.put("pre_order", preOrder);
                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    private void setButtonPlaceOrder(String text, boolean isEnable) {
//        buttonPlaceOrder.setText(text);
//        buttonPlaceOrder.setClickable(isEnable);
//        if (isEnable) {
//            buttonPlaceOrder.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//        } else {
//            buttonPlaceOrder.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
//        }
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteBasket() {
        final AppDatabase db = AppDatabase.getAppDatabase(this);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.basketDao().deleteAll();
                return null;
            }
        }.execute();
    }

    private void fetchInitData() {
        String url = getString(R.string.API_URL) + "/customer/payment/sheet/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            // Execute code
                            final JSONObject responseJson = new JSONObject(response);
                            final String paymentIntentClientSecret = responseJson.getJSONObject("payment_intent").optString("client_secret");
                            final String customerId = responseJson.getJSONObject("customer").optString("id");
                            final String ephemeralKeySecret = responseJson.getJSONObject("ephemeralKey").optString("secret");

                            configureFlowController(
                                    paymentIntentClientSecret,
                                    customerId,
                                    ephemeralKeySecret
                            );
                        } catch (JSONException ex) {
                            Log.e("JSONException", "" + ex.toString());
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the Place Order Button
                        setButtonPlaceOrder("PLACE ORDER", true);

                        Toast.makeText(getApplicationContext(),
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put("amount", "2500");
                parameters.put("currency", "gbp");
                return parameters;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    private void configureFlowController(
            String paymentIntentClientSecret,
            String customerId,
            String ephemeralKeySecret
    ) {

        final PaymentSheet.GooglePayConfiguration googlePayConfiguration =
                new PaymentSheet.GooglePayConfiguration(
                        CommonMethods.getProductFlavor() == ProductFlavor.UAT ?
                                PaymentSheet.GooglePayConfiguration.Environment.Test :
                                PaymentSheet.GooglePayConfiguration.Environment.Production,
                        "US"
                );

        PaymentSheet.Configuration paymentSheetConfigurations = new PaymentSheet.Configuration(
                getString(R.string.merchant_display_name),
                new PaymentSheet.CustomerConfiguration(
                        customerId,
                        ephemeralKeySecret
                )
        );
        paymentSheetConfigurations.setGooglePay(googlePayConfiguration);

        flowController.configureWithPaymentIntent(
                paymentIntentClientSecret, paymentSheetConfigurations,
                (success, error) -> {
                    if (success) {
                        onFlowControllerReady();
                    } else {
                        // handle FlowController configuration failure
                    }
                }
        );
    }

    private void onFlowControllerReady() {
        paymentMethodButton.setOnClickListener(v -> flowController.presentPaymentOptions());
        buyButton.setOnClickListener(v -> onCheckout());
        paymentMethodButton.setEnabled(true);
        onPaymentOption(flowController.getPaymentOption());
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            CommonMethods.showAlertDialog(this, getString(R.string.error), getString(R.string.payment_cancelled), (dialogInterface, i) -> {
            });
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            CommonMethods.showAlertDialog(this, getString(R.string.error), getString(R.string.payment_failed) + ((PaymentSheetResult.Failed) paymentSheetResult).getError(), (dialogInterface, i) -> {
            });
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            CommonMethods.showAlertDialog(this, getString(R.string.success), getString(R.string.payment_completed), (dialogInterface, i) -> {
            });
        }
    }

    private void onPaymentOption(@Nullable PaymentOption paymentOption) {
        if (paymentOption != null) {
            paymentMethodButton.setText(paymentOption.getLabel());
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    paymentOption.getDrawableResourceId(),
                    0,
                    0,
                    0
            );
            buyButton.setEnabled(true);
        } else {
            paymentMethodButton.setText("Select");
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
            );
            buyButton.setEnabled(false);
        }
    }

    private void onCheckout() {
        flowController.confirm();
    }
}
