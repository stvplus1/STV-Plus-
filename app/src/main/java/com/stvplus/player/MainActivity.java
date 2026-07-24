package com.stvplus.player;

import com.yourname.stvplus.R; 

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

// کتێبخانەیێن نوی یێن ExoPlayer بۆ پشتیوانیکرنا MPD و Headers
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // شاردنەڤەیا سەعەت و پاتریێ (Full Screen)
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        playerView = findViewById(R.id.player_view);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        
        webView.setBackgroundColor(0x00000000); 
        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ١. دروستکرنا User-Agent و Referer
                    DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
                    
                    // دانانا User-Agent (وەکو کرۆمێ کۆمپیوتەری خۆ نیشان ددەت دا بلۆک نەبیت)
                    dataSourceFactory.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

                    // دانانا Referer ئەگەر د ناو کۆدێ HTML دا هەبیت
                    if (referer != null && !referer.isEmpty()) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Referer", referer);
                        headers.put("Origin", referer);
                        dataSourceFactory.setDefaultRequestProperties(headers);
                    }

                    // ٢. ئامادەکرنا لینکێ ڤیدیۆیێ و ناسیارکرنا جۆرێ MPD
                    MediaItem.Builder builder = new MediaItem.Builder().setUri(url);
                    
                    if (url.contains(".mpd") || (type != null && type.toLowerCase().contains("mpd"))) {
                        builder.setMimeType(MimeTypes.APPLICATION_MPD);
                    }

                    MediaItem mediaItem = builder.build();

                    // ٣. تێکەلکرنا لینکێ ب هێدەران ڤە و کارپێکرنا پلەیەری
                    MediaSource mediaSource = new DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem);

                    player.setMediaSource(mediaSource);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
