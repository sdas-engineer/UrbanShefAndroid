package com.urbanshef.urbanshefapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.urbanshef.urbanshefapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        App.handleSSLHandshake();
//        App.trustEveryone();
        hasPermissions();

    }
    public  void navigateToMain()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                finish();
                startActivity(new Intent(SplashActivity.this, SignInActivity.class));
            }
        }, 3000);
    }
    private void hasPermissions()
    {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            navigateToMain();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 11);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    //this method calls when user gives permisson or deny
    {
        switch (requestCode)
        {
            case 11: //case 11 is our location request code
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                   navigateToMain();
                }
                else
                {
                    Toast.makeText(SplashActivity.this,"Permission Required ..",Toast.LENGTH_LONG).show();
                    hasPermissions();
                }

            }
        }
    }
}