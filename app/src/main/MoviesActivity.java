package com.yourname.stvplus; // ناڤێ پاکێجا خۆ لێرە بنڤیسە

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // شاردنەوەی شریتی سەرەوە بۆ ئەوەی شاشەکە پڕ بێت
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        myWebView = new WebView(this);
        setContentView(myWebView);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        // زۆر گرنگە بۆ تیڤی بۆکسێ
        webSettings.setMediaPlaybackRequiresUserGesture(false); 
        
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);

        myWebView.setWebViewClient(new WebViewClient());

        // فایلێ خۆ یێ HTML لێرە ڤەکە
        myWebView.loadUrl("file:///android_asset/index.html"); 
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
