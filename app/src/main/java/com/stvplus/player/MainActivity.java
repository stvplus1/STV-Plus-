package com.stvplus.player;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // گازی کرنا کۆدێ فول سکرین کرنێ
        hideSystemUI();

        playerView = findViewById(R.id.player_view);
        webView = findViewById(R.id.webview);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(trackSelector.buildUponParameters()
                .setExceedRendererCapabilitiesIfNecessary(true)
                .setExceedAudioConstraintsIfNecessary(true));

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true);

        player = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .build();
                
        player.setAudioAttributes(audioAttributes, true);
        player.setVolume(1.0f);
        playerView.setPlayer(player);

        // کۆدێ وەرگرتنا کوالێتی و دەنگان و فرێکرنا وان بۆ HTML
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
        });

        webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidPlayer");
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

        // کۆدێ زوومکرنێ
        @JavascriptInterface
        public void setResizeMode(int modeIndex) {
            runOnUiThread(() -> {
                if (playerView != null) {
                    switch (modeIndex) {
                        case 0: playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT); break;
                        case 1: playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL); break;
                        case 2: playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM); break;
                    }
                }
            });
        }

        // کۆدێ وەرگرتنا گۆڕانکاریا کوالێتیێ ژ HTML
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

        // کۆدێ وەرگرتنا گۆڕانکاریا دەنگی ژ HTML
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

    // --- کۆدێن تایبەت ب فول سکرینێ (Immersive Mode) ---
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        ViewNormally I can help with things like this, but I don't seem to have access to that content. You can try again or ask me for something else.
