package com.stvplus.player;

import com.yourname.stvplus.R; // پشتڕاست بە ناڤێ پاکێجێ وەکو خۆ یە

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

// کتێبخانەیێن ExoPlayer
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        playerView = findViewById(R.id.player_view);

        // ١. ئامادەکرنا پلەیەرێ ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // ٢. ڕێکخستنێن WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        
        webView.setBackgroundColor(0x00000000); // وێبڤیو شەفاف دکەت
        webView.setWebViewClient(new WebViewClient());

        // ٣. دروستکرنا پردێ ل ناڤبەرا جاڤا و جاڤاسکریپت (HTML)
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");

        // ٤. بەشێ دابەزاندنا ئاپدەیتێ
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // لۆدکرنا شاشەیا سەرەکی
        webView.loadUrl("file:///android_asset/index.html");
    }

    // ئەڤ کلاسە فەرمانان ژ HTML وەردگریت بۆ جاڤا
    public class WebAppInterface {
        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // هەر دەمێ HTML گۆت لێبدە، ExoPlayer دێ ڤی کاری کەت
                    MediaItem mediaItem = MediaItem.fromUri(url);
                    player.setMediaItem(mediaItem);
                    player.prepare();
                    player.play();
                }
            });
        }
        
        @JavascriptInterface
        public void stopStream() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (player != null) {
                        player.stop();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // دەمێ بەرنامە دئێتە داخستن، پلەیەر ژی دهێتە ڕاگرتن دا پاتریێ نەخۆت
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
