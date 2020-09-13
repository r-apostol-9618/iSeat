package com.example.apost.myapplication;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toolbar;

import javax.security.auth.login.LoginException;

public class SplashActivity extends AppCompatActivity {
    Intent intent;
    //private static final int SEND_SMS_CODE = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //requestSmsSendPermission();
        ColorDrawable cd = new ColorDrawable(0xFFFF6666);
        getSupportActionBar().setBackgroundDrawable(cd);
        getSupportActionBar().setTitle("iSeat Application");
                //(Color.parseColor("#FFA2C13E"));
        //50 183 222
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(140,3,0));
            window.setNavigationBarColor(Color.rgb(140,3,0));
        }

        intent = new Intent(this, MainActivity.class);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 3000);


    }
    /*private void requestSmsSendPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SEND_SMS },
                SEND_SMS_CODE);
    }*/
}
