package com.urbanshef.urbanshefapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationProvider implements LifecycleObserver {
    private MLocationCallback callback;
    private Context context;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private long interval;
    private long fastestInterval;
    private int priority;
    private double Latitude = 0.0, Longitude = 0.0;

    private LocationProvider(final Builder builder)
    {
        context = builder.context;
        callback = builder.callback;
        interval = builder.interval;
        fastestInterval = builder.fastestInterval;
        priority = builder.priority;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdate()
    {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void connectGoogleClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            int REQUEST_GOOGLE_PLAY_SERVICE = 988;
            googleAPI.getErrorDialog((AppCompatActivity) context, resultCode, REQUEST_GOOGLE_PLAY_SERVICE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreateLocationProvider() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onLocationResume() {
        buildGoogleApiClient();
    }

    @SuppressLint("MissingPermission")
    private synchronized void buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        callback.onGoogleAPIClient(mGoogleApiClient, "Connected");

                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(interval);
                        mLocationRequest.setFastestInterval(fastestInterval);
                        mLocationRequest.setPriority(priority);
                        mLocationRequest.setSmallestDisplacement(0);

                        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                        builder.addLocationRequest(mLocationRequest);
                        builder.setAlwaysShow(true);
                        mLocationSettingsRequest = builder.build();

                        mSettingsClient
                                .checkLocationSettings(mLocationSettingsRequest)
                                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                                    @Override
                                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                        requestLocationUpdate();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                int statusCode = ((ApiException) e).getStatusCode();
                                switch (statusCode) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        try {
                                            int REQUEST_CHECK_SETTINGS = 214;
                                            ResolvableApiException rae = (ResolvableApiException) e;
                                            rae.startResolutionForResult((AppCompatActivity) context, REQUEST_CHECK_SETTINGS);
                                        } catch (IntentSender.SendIntentException sie) {
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                }
                            }
                        }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {

                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        connectGoogleClient();
                        callback.onGoogleAPIClient(mGoogleApiClient, "Connection Suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        callback.onGoogleAPIClient(mGoogleApiClient, "" + connectionResult.getErrorCode() + " " + connectionResult.getErrorMessage());
                    }
                })
                .addApi(LocationServices.API)
                .build();

        connectGoogleClient();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Latitude = locationResult.getLastLocation().getLatitude();
                Longitude = locationResult.getLastLocation().getLongitude();

                if (Latitude == 0.0 && Longitude == 0.0) {
                    requestLocationUpdate();
                } else {
                    callback.onLocationUpdated(Latitude, Longitude);
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    public void removeUpdates() {
        try {
            callback.onLocationUpdateRemoved();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public interface MLocationCallback {
        void onGoogleAPIClient(GoogleApiClient googleApiClient, String message);

        void onLocationUpdated(double latitude, double longitude);

        void onLocationUpdateRemoved();
    }

    public static class Builder
    {
        private Context context;
        private MLocationCallback callback;
        private long interval = 10 * 1000;
        private long fastestInterval = 5 * 1000;
        private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        public Builder(Context context) {
            this.context = context;
        }

        public LocationProvider build() {

            return new LocationProvider(this);
        }

        public Builder setListener(MLocationCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setFastestInterval(int fastestInterval) {
            this.fastestInterval = fastestInterval;
            return this;
        }

    }
}