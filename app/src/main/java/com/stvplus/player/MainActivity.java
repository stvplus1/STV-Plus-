package com.stvplus.player; // تێبینی: ئەگەر ناڤێ پاکێجا تە جودایە، تەنێ ڤێ ڕێزا ئێکێ بکە ناڤێ پاکێجا خۆ

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
        // پەیوەستکرن ب دیزاینا شاشەیێ ڤە (کو پێدڤییە WebView تێدا هەبیت)
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // ڕێکخستنێن WebView بۆ کارپێکرنا جاڤاسکریپت و داتایان
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

        // بۆ هندێ کو فایل د ناو خودی بەرنامەی دا ڤەبن نەک بڕنە دەرڤە
        webView.setWebViewClient(new WebViewClient());

        // ==========================================================
        // ئەڤەیە پێنگاڤا دووێ (گرنگترین بەش بۆ دابەزاندنا ئاپدەیتێ)
        // ==========================================================
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // دەمێ فەرمانا دابەزاندنێ دهێت (وەکو لینکا ئاپدەیتێ ل index.html)
                // دێ ڕاستەوخۆ کرۆم یان وێبگەرێ مۆبایلێ ڤەکەت دا فایلێ APK دابەزینیت
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // لۆدکرنا فایلا سەرەکی یا بەرنامەیێ تە ژ ناو فۆلدەرێ assets
        webView.loadUrl("file:///android_asset/index.html");
    }
    
    // ئەگەر تە دڤێت دوگمەیا 'Back' ب دروستی کار بکەت د ناو وێبڤیوی دا
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
