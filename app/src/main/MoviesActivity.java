package com.stvplus.player;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
// ئەگەر کتێبخانەیێن ExoPlayer تە هەبن ل ڤێرە زێدە بکە وەکو د MainActivity دا هەنە

public class MoviesActivity extends AppCompatActivity {

    private WebView webView;
    // ل ڤێرە گۆڕاوێن ExoPlayer یێن خۆ زێدە بکە (وەکو playerView, player)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // دڤێت دیزاینا تە یا xml (activity_movies) هەم PlayerView و هەم WebView تێدا بن
        setContentView(R.layout.activity_movies); 

        // 1. ڕێکخستنا WebView
        webView = findViewById(R.id.webView);
        
        // 2. گرنگترین خاڵ: شەفافکرنا باکگراوەندێ WebView دا ڤیدیۆ ل پشتەڤە دیار بیت
        webView.setBackgroundColor(0x00000000); 

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // 3. دروستکرنا پردێ پەیوەندیێ دناڤبەرا HTML و جاڤا دا ب ناڤێ "AndroidPlayer"
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");

        webView.setWebViewClient(new WebViewClient());
        
        // 4. لۆدکرنا فایلا فلیمان ژ ناو فۆلدەرێ assets (پشتڕاست بە ناڤێ فایلێ یێ دروستە)
        webView.loadUrl("file:///android_asset/movies.html");

        // 5. ل ڤێرە ExoPlayer یێ خۆ ئامادە بکە وەکو چەوا تە د MainActivity دا چێکریە
        // setupExoPlayer(); 
    }

    // ئەڤ کلاسە فەرمانان ژ کۆدێ HTML وەردگریت
    public class WebAppInterface {

        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(() -> {
                if (url != null && url.equals("about:blank")) {
                    // ئەگەر فەرمانا "about:blank" هات، ئانکو بەکارهێنەری فلیم داخست، ڤیدیۆیێ ڕابگرە
                    stopExoPlayer();
                } else {
                    // ل ڤێرە فەرمانێ بدە ExoPlayer دا ڤیدیۆیا نوی لێبدەت
                    startExoPlayer(url, type);
                }
            });
        }
    }

    private void startExoPlayer(String url, String type) {
        // کۆدێ کارپێکرنا ExoPlayer ل ڤێرە دابنێ (کۆپی بکە ژ MainActivity)
    }

    private void stopExoPlayer() {
        // کۆدێ ڕاگرتنا ExoPlayer ل ڤێرە دابنێ
    }
}
