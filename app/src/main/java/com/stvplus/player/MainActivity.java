package com.stvplus.player;

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

        // ڕێکخستنێن WebView بۆ کارپێکرنا جاڤاسکریپت
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

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

        // لۆدکرنا فایلا index.html
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
