package com.urbanshef.urbanshefapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.urbanshef.urbanshefapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private AppUpdateManager appUpdateManager;
    private static final Integer DAYS_FOR_FLEXIBLE_UPDATE = 1;
    private static final Integer IN_APP_UPDATE_REQUEST_CODE = 0x34;
    private Button customerButton, driverButton;
    private CallbackManager callbackManager;
    private SharedPreferences sharedPref;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        customerButton = (Button) findViewById(R.id.button_customer);
        driverButton = (Button) findViewById(R.id.button_driver);

        // Handle Customer Button
        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                customerButton.setTextColor(getResources().getColor(android.R.color.white));

                driverButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                driverButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        // Handle Driver Button
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                driverButton.setTextColor(getResources().getColor(android.R.color.white));

                customerButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                customerButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        // Handle Login Button
        buttonLogin = (Button) findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AccessToken.getCurrentAccessToken() == null) {
                    LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("public_profile", "email"));
                } else {
                    ColorDrawable customerbuttonColor = (ColorDrawable) customerButton.getBackground();

                    if (customerbuttonColor.getColor() == getResources().getColor(R.color.colorAccent)) {
                        loginToServer(AccessToken.getCurrentAccessToken().getToken(), "customer");
                    } else {
                        loginToServer(AccessToken.getCurrentAccessToken().getToken(), "driver");
                    }
                }
            }
        });


        callbackManager = CallbackManager.Factory.create();
        sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        final Button buttonLogout = (Button) findViewById(R.id.button_logout);
        //   buttonLogout.setVisibility(View.GONE);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("FACEBOOK TOKEN", loginResult.getAccessToken().getToken());

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.d("FACEBOOK DETAILS", object.toString());

                                        SharedPreferences.Editor editor = sharedPref.edit();

                                        try {
                                            editor.putString("name", object.getString("name"));
                                            editor.putString("email", object.getString("email"));
                                            editor.putString("avatar", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        editor.commit();
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,picture");
                        request.setParameters(parameters);
                        request.executeAsync();

                        ColorDrawable customerbuttonColor = (ColorDrawable) customerButton.getBackground();

                        if (customerbuttonColor.getColor() == getResources().getColor(R.color.colorAccent)) {
                            loginToServer(AccessToken.getCurrentAccessToken().getToken(), "customer");
                        } else {
                            loginToServer(AccessToken.getCurrentAccessToken().getToken(), "driver");
                        }
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        if (AccessToken.getCurrentAccessToken() != null) {
            Log.d("USER", sharedPref.getAll().toString());
            buttonLogin.setText("Continue as " + sharedPref.getString("email", ""));
            //  buttonLogout.setVisibility(View.VISIBLE);
        }

        // Handle Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                buttonLogin.setText("Login with Facebook");
                // buttonLogout.setVisibility(View.GONE);
            }
        });

        checkForAppUpdate();
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            IN_APP_UPDATE_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    private void loginToServer(String facebookAccessToken, final String userType) {
        buttonLogin.setText("LOADING...");
        buttonLogin.setClickable(false);
        buttonLogin.setBackgroundColor(getResources().getColor(R.color.colorLightGray));

        String url = getString(R.string.API_URL) + "/social/convert-token";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("grant_type", "convert_token");
            jsonBody.put("client_id", getString(R.string.CLIENT_ID));
            jsonBody.put("client_secret", getString(R.string.CLIENT_SECRET));
            jsonBody.put("backend", "facebook");
            jsonBody.put("token", facebookAccessToken);
            jsonBody.put("user_type", userType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Execute code
                        Log.d("LOGIN TO SERVER", response.toString());

                        // Shave server token to local database
                        SharedPreferences.Editor editor = sharedPref.edit();

                        try {
                            editor.putString("token", response.getString("access_token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        editor.commit();

                        // Start main activity
                        if (userType.equals("customer")) {
                            Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
                            startActivity(intent);
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle errors
                        System.out.println("ERROR :" + error.getMessage());
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    public void checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks whether the platform allows the specified type of update,
        // and current version staleness.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    /* && appUpdateInfo.clientVersionStalenessDays() != null
                    && appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_FLEXIBLE_UPDATE */
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            IN_APP_UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
