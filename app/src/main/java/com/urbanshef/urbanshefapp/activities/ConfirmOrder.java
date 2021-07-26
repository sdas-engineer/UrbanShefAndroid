package com.urbanshef.urbanshefapp.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.urbanshef.urbanshefapp.AppDatabase;
import com.urbanshef.urbanshefapp.R;
import com.urbanshef.urbanshefapp.utils.CommonMethods;
import com.urbanshef.urbanshefapp.utils.Constants;
import com.urbanshef.urbanshefapp.utils.ProductFlavor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmOrder extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private GoogleMap mMap;

    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;

    private EditText customer_flat_number;
    private EditText customer_street_address;
    private EditText delivery_instructions;
    private EditText phone;
    private EditText preOrderDate;
    private EditText preOrderTime;
    Button buyButton;
    Context context;

    private PaymentSheet paymentSheet;

    private String paymentIntentClientSecret;
    private String customerId;
    private String paymentId;
    private String ephemeralKeySecret;

    private Calendar calendar;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        phone = findViewById(R.id.phone);

        // Address EditText
        customer_flat_number = findViewById(R.id.customer_flat_number);
        customer_street_address = findViewById(R.id.customer_street_address);
        delivery_instructions = findViewById(R.id.delivery_instructions);
        buyButton = findViewById(R.id.button_checkout);
        preOrderDate = findViewById(R.id.pre_order_date);
        preOrderTime = findViewById(R.id.pre_order_time);
        context = this;

        calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
//        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if (getIntent().getStringExtra("deliveryTime").equalsIgnoreCase(Constants.PRE_ORDER)) {
            findViewById(R.id.pre_order_container).setVisibility(View.VISIBLE);
            preOrderDate.setOnClickListener(this);
            preOrderTime.setOnClickListener(this);
        } else {
            findViewById(R.id.pre_order_container).setVisibility(View.GONE);
        }


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.basket_map);
        mapFragment.getMapAsync(this);

        // Phone EditText

        // Handle Map Address
        handleMapAddress();

        // instantiate view and buyButton

        buyButton.setEnabled(false);

        PaymentConfiguration.init(
                getApplicationContext(),
                CommonMethods.getTargetStripeKey(this)
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        buyButton.setOnClickListener(v -> handlePaymentButtonClick());

        fetchInitData();
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
                            paymentIntentClientSecret = responseJson.getJSONObject("payment_intent").optString("client_secret");
                            customerId = responseJson.getJSONObject("customer").optString("id");
                            ephemeralKeySecret = responseJson.getJSONObject("ephemeralKey").optString("secret");
                            paymentId = responseJson.getJSONObject("payment_intent").optString("id");
                            runOnUiThread(() -> buyButton.setEnabled(true));
                        } catch (JSONException ex) {
                            Log.e("JSONException", "" + ex.toString());
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put("amount", String.format(Locale.getDefault(), "%d", (int)(Double.parseDouble(getIntent().getStringExtra("totalCharge")) * 100)));
                parameters.put("currency", "gbp");
                parameters.put("access_token", sharedPref.getString("token", ""));
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

    private void handlePaymentButtonClick() {
        if (phone.getText().toString().equals("")) {
            phone.setError("Phone number cannot be blank");
        } else if (customer_flat_number.getText().toString().equals("")) {
            customer_flat_number.setError("Flat or building number cannot be blank");
        } else if (customer_street_address.getText().toString().equals("")) {
            customer_street_address.setError("Full address number cannot be blank");
        } else if (getIntent().getStringExtra("deliveryTime").equalsIgnoreCase(Constants.PRE_ORDER)) {
            if (preOrderDate.getText().toString().equals("")) {
                preOrderDate.setError("Pre order date can't be empty");
            } else if (preOrderTime.getText().toString().equals("")) {
                preOrderTime.setError("Pre order time can't be empty");
            } else {
                presentPaymentSheet();
            }
        } else {
            presentPaymentSheet();
        }
    }

    private void presentPaymentSheet() {
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

        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                paymentSheetConfigurations);
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            CommonMethods.showAlertDialog(this, getString(R.string.error), getString(R.string.payment_cancelled), (dialogInterface, i) -> {
            });
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            CommonMethods.showAlertDialog(this, getString(R.string.error), getString(R.string.payment_failed) + ((PaymentSheetResult.Failed) paymentSheetResult).getError(), (dialogInterface, i) -> {
            });
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            addOrder();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    // Get the last-know location of the device and set the position of the map.
                    getDeviceLocation();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Do other setup activities here too, as described elsewhere in this tutorial.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getLocationPermission();
            getDeviceLocation();
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                mMap.addMarker(new MarkerOptions().position(latLng));

                // Set address field from the position on the map
                Geocoder coder = new Geocoder(ConfirmOrder.this);
                try {
                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocation(
                            latLng.latitude, latLng.longitude, 1
                    );

                    if (!addresses.isEmpty()) {
                        customer_street_address.setText(addresses.get(0).getAddressLine(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                                ));

                                // Set address field from the position on the map
                                Geocoder coder = new Geocoder(ConfirmOrder.this);
                                try {
                                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocation(
                                            mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1
                                    );

                                    if (!addresses.isEmpty()) {
                                        customer_street_address.setText(addresses.get(0).getAddressLine(0));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void handleMapAddress() {
        customer_street_address.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_DONE) {
                    Geocoder coder = new Geocoder(getActivity());

                    try {
                        ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(textView.getText().toString(), 1);
                        if (!addresses.isEmpty()) {
                            double lat = addresses.get(0).getLatitude();
                            double lng = addresses.get(0).getLongitude();

                            LatLng pos = new LatLng(lat, lng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM));
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(pos));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
    }

    private Context getActivity() {
        return this;
    }

//    private void proceedToPayment() {
//        Intent intent = new Intent(ConfirmOrder.this, PaymentActivity.class);
//        intent.putExtras(getIntent().getExtras());
////                    intent.putExtra("delivery_info", delivery_instructions.getText().toString());
//        intent.putExtra("phone", phone.getText().toString());
//        intent.putExtra("customer_flat_number", customer_flat_number.getText().toString());
//        intent.putExtra("customer_street_address", customer_street_address.getText().toString());
//        intent.putExtra("delivery_instructions", delivery_instructions.getText().toString());
//        String preOrder = getIntent().getStringExtra("deliveryTime").equalsIgnoreCase(Constants.PRE_ORDER) ? String.format(Locale.getDefault(), "%1$s %2$s", preOrderDate.getText().toString(), preOrderTime.getText().toString()) : getIntent().getStringExtra("deliveryTime");
//        intent.putExtra("pre_order", preOrder);
//        startActivity(intent);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pre_order_date:
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        if (calendar.getTime().getTime() > new Date().getTime()) {
                            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
                            preOrderDate.setText(sdf.format(calendar.getTime()));
                        } else {
                            Toast.makeText(ConfirmOrder.this, getString(R.string.msg_invalid_pre_order_date), Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(new Date().getTime() + (24 * 60 * 60 * 1000));
                datePickerDialog.show();
                break;
            case R.id.pre_order_time:
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        calendar.set(Calendar.HOUR_OF_DAY, i);
                        calendar.set(Calendar.MINUTE, i1);

                        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault());
                        preOrderTime.setText(sdf.format(calendar.getTime()));
                    }
                };
                new TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                break;
        }
    }

    private void addOrder() {
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
                                CommonMethods.showAlertDialog(ConfirmOrder.this, getString(R.string.success), getString(R.string.payment_completed), (dialogInterface, i) -> {
                                    Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                                    intent.putExtra("screen", "order");
                                    startActivity(intent);
                                });

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jsonObject.getString("error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Intent intent = getIntent();
                final SharedPreferences sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPref.getString("token", ""));
                params.put("chef_id", intent.getStringExtra("chefId"));
                params.put("delivery_charge", intent.getStringExtra("deliveryCharge"));
                params.put("service_charge", intent.getStringExtra("serviceCharge"));
                params.put("coupon", intent.getStringExtra("coupon_code"));
                params.put("customer_street_address", customer_street_address.getText().toString());
                params.put("customer_flat_number", customer_flat_number.getText().toString());
                params.put("phone", phone.getText().toString());
                params.put("order_details", intent.getStringExtra("orderDetails"));
                params.put("delivery_instructions", delivery_instructions.getText().toString());
                params.put("pre_order", getIntent().getStringExtra("deliveryTime").equalsIgnoreCase(Constants.PRE_ORDER) ? String.format(Locale.getDefault(), "%1$s %2$s", preOrderDate.getText().toString(), preOrderTime.getText().toString()) : getIntent().getStringExtra("deliveryTime"));
                params.put("payment_id", paymentId);
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
}