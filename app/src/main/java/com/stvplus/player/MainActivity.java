package com.yourname.stvplus; // لێرە ناڤێ پاکێجا خۆ بنڤیسە

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
        
        // شاردنەوەی شریتی سەرەوە بۆ ئەوەی شاشەکە پڕ بێت (Full Screen)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // دروستکرنا WebView ڕاستەوخۆ
        myWebView = new WebView(this);
        setContentView(myWebView);

        // ڕێکخستنێن گرنگ بۆ WebView (بۆ خێرایی و تیڤی بۆکس)
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // کارپێکرنا جاڤاسکریپتێ
        webSettings.setDomStorageEnabled(true); // ڕێگەدان ب خەزنکرنا زانیاریان د کۆدێ HTML دا
        
        // ئەڤە زۆر گرنگە بۆ تیڤی بۆکسێ، دا کو ڤیدیۆ بێی دەستلێدان (Touch) ڕاستەوخۆ کار بکەت
        webSettings.setMediaPlaybackRequiresUserGesture(false); 
        
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);

        // ڕێگری دکەت ل ڤەکرنا وێبگەڕێ دەرەکی (Chrome)، هەمی تشت د ناڤ ئەپێ دا دەمێنێت
        myWebView.setWebViewClient(new WebViewClient());

        // فایلێ خۆ یێ HTML لێرە ڤەکە (ئەگەر د ناڤ فۆلدەرێ assets بیت)
        myWebView.loadUrl("file:///android_asset/index.html"); 
        
        // یان ئەگەر فایلێ تە ل سەر ئینتەرنێتێ یە ئەڤێ هێڵێ بکار بینە و هێڵا سەرێ بسڕە:
        // myWebView.loadUrl("https://your-website-link.com/index.html");
    }

    // کۆنترۆلکرنا پێلۆکا ڤەگەڕیانێ (Back Button) یێ کۆنترۆلێ
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
