package com.stvplus.player;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.AspectRatioFrameLayout; // ئەڤە بۆ زوومکرنا شاشەیێ هاتە زێدەکرن

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

        // ئامادەکرنا تایبەتمەندیێن دەنگی بۆ چارەسەرکرنا کێشەیا دەنگی
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        // ڕێکخستنا نەرمەکاڵای بۆ خوێندنا هەر جۆرە دەنگەکێ
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true);

        // نەچارکرنا پلەیەرێ کو دەنگی کارپێبکەت
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(trackSelector.buildUponParameters()
                .setExceedRendererCapabilitiesIfNecessary(true)
                .setExceedAudioConstraintsIfNecessary(true));

        // باشترکرنا شیکارکرنا فایلێن TS یێن IPTV
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true);

        // دروستکرنا پلەیەرێ ب هەمی هێز و تایبەتمەندیێن نویڤە
        player = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .build();
                
        player.setAudioAttributes(audioAttributes, true);
        player.setVolume(1.0f);
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

        // ئەڤ فەنکشنە نویە بۆ گۆڕینا قەبارێ شاشەیێ (نۆڕماڵ، درێژکرن، زووم)
        @JavascriptInterface
        public void setResizeMode(int modeIndex) {
            runOnUiThread(() -> {
                if (playerView != null) {
                    switch (modeIndex) {
                        case 0: // Normal
                            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                            break;
                        case 1: // Stretch (درێژکرن)
                            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                            break;
                        case 2: // Zoom
                            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                            break;
                    }
                }
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
