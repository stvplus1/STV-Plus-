package com.stvplus.player;

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

// ExoPlayer Imports
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.drm.LocalMediaDrmCallback;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.audio.AudioAttributes;

// VLC Imports
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    
    // گۆڕاوێن VLC
    private LibVLC libVLC;
    private MediaPlayer vlcMediaPlayer;
    private VLCVideoLayout vlcVideoLayout;

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
        vlcVideoLayout = findViewById(R.id.vlc_video_layout);

        // ئامادەکرنا ExoPlayer دگەل چارەسەریا دەنگی
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        exoPlayer = new ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .build();
        playerView.setPlayer(exoPlayer);

        // ئامادەکرنا VLC Player
        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        options.add("-vvv");
        libVLC = new LibVLC(this, options);
        vlcMediaPlayer = new MediaPlayer(libVLC);
        vlcMediaPlayer.attachViews(vlcVideoLayout, null, false, false);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        
        webView.setBackgroundColor(0x00000000); 
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ئەگەر جۆرێ کەناڵی 'vlc' بوو، دێ ب VLC لێدەت بۆ چارەسەریا دەنگی
                    if (type != null && type.toLowerCase().equals("vlc")) {
                        exoPlayer.stop();
                        playerView.setVisibility(View.GONE);
                        vlcVideoLayout.setVisibility(View.VISIBLE);
                        
                        vlcMediaPlayer.stop();
                        Media media = new Media(libVLC, Uri.parse(url));
                        media.setHWDecoderEnabled(true, false);
                        vlcMediaPlayer.setMedia(media);
                        media.release();
                        vlcMediaPlayer.play();
                    } 
                    // ئەگەر نە، دێ ب ExoPlayer لێدەت
                    else {
                        vlcMediaPlayer.stop();
                        vlcVideoLayout.setVisibility(View.GONE);
                        playerView.setVisibility(View.VISIBLE);
                        
                        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory();
                        httpFactory.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                        if (referer != null && !referer.isEmpty()) {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Referer", referer);
                            headers.put("Origin", referer);
                            httpFactory.setDefaultRequestProperties(headers);
                        }
                        
                        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MainActivity.this, httpFactory);
                        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);
                        MediaItem.Builder builder = new MediaItem.Builder().setUri(url);
                        
                        if (url.contains(".mpd") || (type != null && type.toLowerCase().contains("dash"))) {
                            builder.setMimeType(MimeTypes.APPLICATION_MPD);
                        }
                        
                        // سیستەمێ ClearKey DRM
                        if (drmKeyId != null && !drmKeyId.isEmpty() && drmKey != null && !drmKey.isEmpty()) {
                            try {
                                byte[] kidBytes = hexStringToByteArray(drmKeyId);
                                byte[] kBytes = hexStringToByteArray(drmKey);
                                String kidB64 = Base64.encodeToString(kidBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
                                String kB64 = Base64.encodeToString(kBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
                                String clearKeyJson = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"" + kB64 + "\",\"kid\":\"" + kidB64 + "\"}],\"type\":\"temporary\"}";
                                LocalMediaDrmCallback drmCallback = new LocalMediaDrmCallback(clearKeyJson.getBytes());
                                DefaultDrmSessionManager drmSessionManager = new DefaultDrmSessionManager.Builder()
                                        .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                                        .build(drmCallback);
                                mediaSourceFactory.setDrmSessionManagerProvider(mediaItem -> drmSessionManager);
                                builder.setDrmConfiguration(new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID).build());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        MediaSource mediaSource = mediaSourceFactory.createMediaSource(builder.build());
                        exoPlayer.setMediaSource(mediaSource);
                        exoPlayer.prepare();
                        exoPlayer.play();
                    }
                }
            });
        }
        
        @JavascriptInterface
        public void stopStream() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (exoPlayer != null) exoPlayer.stop();
                    if (vlcMediaPlayer != null) vlcMediaPlayer.stop();
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
        if (exoPlayer != null) exoPlayer.release();
        if (vlcMediaPlayer != null) {
            vlcMediaPlayer.release();
            libVLC.release();
        }
    }
}
