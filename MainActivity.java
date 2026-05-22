package com.example.tvapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // دروستکرنا دەقەکێ سادە ل سەر شاشێ
        TextView textView = new TextView(this);
        textView.setText("خێر هاتن بۆ پرۆژێ من یێ تیڤیێ!");
        textView.setTextSize(30);
        
        // نیشاندانا دەقی ل سەر شاشێ
        setContentView(textView);
    }
}
