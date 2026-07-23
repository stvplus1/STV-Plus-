package com.stvplus.player;

// گرێدانا دیزاینێ ب ناڤێ دروست یێ پاکێجێ
import com.yourname.stvplus.R; 

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // گرێدانا جاڤا ب دیزاینا شاشەیێ ڤە
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // ڕێکخستنێن WebView بۆ کارپێکرنا جاڤاسکریپت و فایلان
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

        // بۆ هندێ کو فایلێن HTML د ناو خودی بەرنامەی دا ڤەبن
        webView.setWebViewClient(new WebViewClient());

        // بەشێ دابەزاندنا ئاپدەیتێ (APK) ب ڕێکا وێبگەرێ مۆبایلێ
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // لۆدکرنا فایلا سەرەکی (شاشەیا چالاککرن و ئاپدەیتێ)
        webView.loadUrl("file:///android_asset/index.html");
    }
    
    // کۆنترۆلا دوگمەیا ڤەگەڕانێ (Back Button)
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
