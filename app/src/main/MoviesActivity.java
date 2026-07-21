package com.example.stvplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MoviesActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies); // دڵنیاببە ناڤێ XML یێ تە ڕاستە

        webView = findViewById(R.id.webView);

        // ڕێكخستنێن WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // گرێدانا Javascript Interface
        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidPlayer");

        webView.setWebViewClient(new WebViewClient());
        
        // لۆدکرنا پەڕگەیێ movies.html ژ فۆڵدەرێ assets
        webView.loadUrl("file:///android_asset/movies.html");
    }

    // کلاسێ Interface بۆ وەرگرتنا لینکێ ژ HTML
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void openMedia(String videoUrl) {
            // شاندنا لینکێ بۆ هەمان ئەکتیڤیتیا ExoPlayer
            Intent intent = new Intent(mContext, PlayerActivity.class); // PlayerActivity ناڤێ پلەیەرێ تە یە
            intent.putExtra("VIDEO_URL", videoUrl);
            mContext.startActivity(intent);
        }
    }
}
