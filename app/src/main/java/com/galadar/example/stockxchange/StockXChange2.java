package com.galadar.example.stockxchange;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class StockXChange2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_splash);

        startActivity(new Intent(this, MainActivity.class));

        super.onCreate(savedInstanceState);

        this.finish();
    }

    }