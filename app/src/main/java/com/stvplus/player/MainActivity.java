package com.stvplus.player;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.MediaItem.DrmConfiguration;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private PlayerView playerView;
    private ExoPlayer player;
    private boolean isFullScreen = true;
    
    // یوزەر-ئێجێنتێ کۆمپیوتەری تەنێ دێ بۆ کەنالێن ExoPlayer هێتە بکارئینان
    private final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
        }

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            isFullScreen = true;
        }

        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.player_view);
        webView = findViewById(R.id.webview);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build();

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(trackSelector.buildUponParameters()
                .setExceedRendererCapabilitiesIfNecessary(true)
                .setExceedAudioConstraintsIfNecessary(true));

        player = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, new DefaultExtractorsFactory()))
                .build();
                
        player.setAudioAttributes(audioAttributes, true);
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onTracksChanged(Tracks tracks) {
                try {
                    JSONArray videoQualities = new JSONArray();
                    JSONArray audioTracks = new JSONArray();

                    for (int g = 0; g < tracks.getGroups().size(); g++) {
                        Tracks.Group group = tracks.getGroups().get(g);
                        if (group.getType() == C.TRACK_TYPE_VIDEO) {
                            for (int i = 0; i < group.length; i++) {
                                Format format = group.getTrackFormat(i);
                                JSONObject q = new JSONObject();
                                q.put("groupIndex", g);
                                q.put("trackIndex", i);
                                q.put("height", format.height != Format.NO_VALUE ? format.height : 0);
                                videoQualities.put(q);
                            }
                        } else if (group.getType() == C.TRACK_TYPE_AUDIO) {
                            for (int i = 0; i < group.length; i++) {
                                Format format = group.getTrackFormat(i);
                                JSONObject a = new JSONObject();
                                a.put("groupIndex", g);
                                a.put("trackIndex", i);
                                a.put("name", format.language != null ? format.language : "Track " + (i + 1));
                                audioTracks.put(a);
                            }
                        }
                    }
                    
                    JSONObject result = new JSONObject();
                    result.put("video", videoQualities);
                    result.put("audio", audioTracks);
                    
                    String json = result.toString();
                    runOnUiThread(() -> {
                        webView.evaluateJavascript("javascript:if(window.updateAndroidTracks) window.updateAndroidTracks('" + json + "');", null);
                    });
                } catch(Exception e) {}
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                if (playerView != null) {
                    playerView.postDelayed(() -> {
                        if (player != null) {
                            player.prepare(); 
                            player.play();    
                        }
                    }, 3000); 
                }
            }
        });

        webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        
        // تێبینی: من User-Agent یێ کۆمپیوتەری ل ڤێرە لادا دا کو Shaka Player بێ کێشە کار بکەت!
        
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");
        webView.loadUrl("file:///android_asset/index.html");

        updatePlayerViewLayout(getResources().getConfiguration().orientation);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePlayerViewLayout(newConfig.orientation);
    }

    private void updatePlayerViewLayout(int orientation) {
        if (playerView != null) {
            ViewGroup.LayoutParams params = playerView.getLayoutParams();
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.35);
            } else {
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            playerView.setLayoutParams(params);
        }
    }

    private class WebAppInterface {
        
        @JavascriptInterface
        public void playStream(String url, String type, String referer, String drmKeyId, String drmKey) {
            runOnUiThread(() -> {
                DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
                httpDataSourceFactory.setAllowCrossProtocolRedirects(true);
                
                // User-Agent یێ کۆمپیوتەری تەنێ بۆ ڤێرە (ExoPlayer) دهێتە بکارئینان
                httpDataSourceFactory.setUserAgent(CHROME_USER_AGENT);
                
                if (referer != null && !referer.trim().isEmpty()) {
                    httpDataSourceFactory.setDefaultRequestProperties(java.util.Collections.singletonMap("Referer", referer.trim()));
                }

                androidx.media3.datasource.DefaultDataSource.Factory dataSourceFactory = 
                        new androidx.media3.datasource.DefaultDataSource.Factory(MainActivity.this, httpDataSourceFactory);

                MediaItem.Builder mediaItemBuilder = new MediaItem.Builder().setUri(url);

                if (drmKeyId != null && !drmKeyId.trim().isEmpty() && drmKey != null && !drmKey.trim().isEmpty()) {
                    String cleanKeyId = drmKeyId.trim();
                    String cleanKey = drmKey.trim();
                    
                    String clearKeyJson = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"" + hexToBase64Url(cleanKey) + "\",\"kid\":\"" + hexToBase64Url(cleanKeyId) + "\"}],\"type\":\"temporary\"}";
                    String licenseUri = "data:application/json;base64," + android.util.Base64.encodeToString(clearKeyJson.getBytes(), android.util.Base64.NO_WRAP);
                    
                    DrmConfiguration drmConfig = new DrmConfiguration.Builder(C.CLEARKEY_UUID)
                            .setLicenseUri(licenseUri)
                            .build();
                    mediaItemBuilder.setDrmConfiguration(drmConfig);
                }

                MediaItem mediaItem = mediaItemBuilder.build();
                androidx.media3.exoplayer.source.MediaSource mediaSource;

                if (url.contains(".mpd") || "dash".equalsIgnoreCase(type)) {
                    mediaSource = new DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
                } else {
                    mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
                }

                player.setMediaSource(mediaSource);
                player.prepare();
                player.play();
            });
        }
        
        private String hexToBase64Url(String hex) {
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
            }
            return android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP);
        }

        @JavascriptInterface
        public void setResizeMode(int modeIndex) {
            runOnUiThread(() -> {
                if (playerView != null) {
                    playerView.setResizeMode(modeIndex == 1 ? AspectRatioFrameLayout.RESIZE_MODE_FILL : 
                                             modeIndex == 2 ? AspectRatioFrameLayout.RESIZE_MODE_ZOOM : 
                                             AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
            });
        }

        @JavascriptInterface
        public void toggleNativeFullScreen() {
            runOnUiThread(() -> {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                if (controller != null) {
                    controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    
                    if (isFullScreen) {
                        controller.show(WindowInsetsCompat.Type.systemBars());
                        isFullScreen = false;
                    } else {
                        controller.hide(WindowInsetsCompat.Type.systemBars());
                        isFullScreen = true;
                    }
                }
            });
        }

        @JavascriptInterface
        public void setVideoQuality(int groupIndex, int trackIndex) {
            runOnUiThread(() -> {
                if (groupIndex == -1) {
                    player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                            .buildUpon().clearOverridesOfType(C.TRACK_TYPE_VIDEO).build());
                } else {
                    try {
                        player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                                .buildUpon().setOverrideForType(new TrackSelectionOverride(
                                        player.getCurrentTracks().getGroups().get(groupIndex).getMediaTrackGroup(),
                                        java.util.Collections.singletonList(trackIndex)
                                )).build());
                    } catch (Exception e){}
                }
            });
        }

        @JavascriptInterface
        public void setAudioTrack(int groupIndex, int trackIndex) {
            runOnUiThread(() -> {
                if (groupIndex == -1) {
                    player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                            .buildUpon().clearOverridesOfType(C.TRACK_TYPE_AUDIO).build());
                } else {
                    try {
                        player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                                .buildUpon().setOverrideForType(new TrackSelectionOverride(
                                        player.getCurrentTracks().getGroups().get(groupIndex).getMediaTrackGroup(),
                                        java.util.Collections.singletonList(trackIndex)
                                )).build());
                    } catch(Exception e){}
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
