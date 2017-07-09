package com.galadar.example.stockxchange;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
This is the launch screen of the app, showing the logo image
 and launching MainActivity behind the scenes until it loads and is ready to show.
 */

public class StockXChange2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startActivity(new Intent(this, MainActivity.class));

        this.finish();
    }

    }
