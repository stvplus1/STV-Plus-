package com.stvplus.player;

// پشتڕاست بە ناڤێ پاکێجا تە ل ڤێرە یێ دروستە
import com.yourname.stvplus.R; 

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
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
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        
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

    // کۆنڤێرتکرنا کلیلێن DRM بۆ فۆرماتێ دروست
    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory();
                    httpFactory.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                    
                    if (referer != null && !referer.isEmpty()) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Referer", referer);
                        headers.put("Origin", referer);
                        httpFactory.setDefaultRequestProperties(headers);
                    }
                    
                    // بەکارهێنانا DefaultDataSource دا کو پشتیوانیا فایلێن ناوخۆیی و داتا بکەت
                    DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MainActivity.this, httpFactory);
                    
                    MediaItem.Builder builder = new MediaItem.Builder().setUri(url);
                    
                    // دەستنیشانکرنا جۆرێ MPD بۆ کەناڵێن پاراستی
                    if (url.contains(".mpd") || (type != null && type.toLowerCase().contains("dash"))) {
                        builder.setMimeType(MimeTypes.APPLICATION_MPD);
                    }
                    
                    // کاراکرنا سیستەمێ ClearKey DRM ڕاستەوخۆ د ناو ExoPlayer دا
                    if (drmKeyId != null && !drmKeyId.isEmpty() && drmKey != null && !drmKey.isEmpty()) {
                        try {
                            byte[] kidBytes = hexStringToByteArray(drmKeyId);
                            byte[] kBytes = hexStringToByteArray(drmKey);
                            
                            String kidB64 = Base64.encodeToString(kidBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
                            String kB64 = Base64.encodeToString(kBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
                            
                            // دروستکرنا فایلا مۆڵەتا DRM
                            String clearKeyJson = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"" + kB64 + "\",\"kid\":\"" + kidB64 + "\"}],\"type\":\"temporary\"}";
                            String licenseUrl = "data:application/json;base64," + Base64.encodeToString(clearKeyJson.getBytes(), Base64.NO_WRAP);
                            
                            MediaItem.DrmConfiguration drmConfig = new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                                    .setLicenseUri(licenseUrl)
                                    .build();
                            builder.setDrmConfiguration(drmConfig);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    MediaSource mediaSource = new DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(builder.build());
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
