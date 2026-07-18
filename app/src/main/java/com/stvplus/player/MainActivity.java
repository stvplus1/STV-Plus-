package com.stvplus.player;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.player_view);
        webView = findViewById(R.id.webview);

        // ئامادەکرنا ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // ئامادەکرنا WebView ب شەفافی
        webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // دروستکرنا پردەکێ دناڤبەرا HTML و ئەندرۆید دا
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");
        
        // لۆدکرنا فایلێ HTML
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void playStream(String url, String type) {
            runOnUiThread(() -> {
                MediaItem mediaItem = MediaItem.fromUri(url);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
