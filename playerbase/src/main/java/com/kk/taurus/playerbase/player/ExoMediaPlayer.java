package com.kk.taurus.playerbase.player;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.kk.taurus.playerbase.config.AppContextAttach;
import com.kk.taurus.playerbase.config.PlayerConfig;
import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.entity.DecoderPlan;
import com.kk.taurus.playerbase.event.BundlePool;
import com.kk.taurus.playerbase.event.EventKey;
import com.kk.taurus.playerbase.event.OnErrorEventListener;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.lebo.LeCast;
import com.kk.taurus.playerbase.log.PLog;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ExoMediaPlayer extends BaseInternalPlayer {

    private final String TAG = "ExoMediaPlayer";

    public static final int PLAN_ID = 200;

    private Timer mTimer;

    private TimerTask mTask;

    private final Context mAppContext;

    private SimpleExoPlayer mInternalPlayer;

    private int mVideoWidth, mVideoHeight;

    private int mStartPos = -1;

    private boolean isPreparing = true;

    private boolean isBuffering = false;

    private boolean isPendingSeek = false;

    private MediaSource videoSource;

    private final DefaultBandwidthMeter mBandwidthMeter;

    public static void init(Application application) {
        PlayerConfig.addDecoderPlan(new DecoderPlan(
                PLAN_ID,
                ExoMediaPlayer.class.getName(),
                "exoplayer"));
        PlayerConfig.setDefaultPlanId(PLAN_ID);
        PlayerLibrary.init(application);
    }

    public ExoMediaPlayer() {
        mAppContext = AppContextAttach.getApplicationContext();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(mAppContext);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new CustomLoadControl();
        mInternalPlayer = ExoPlayerFactory.newSimpleInstance(mAppContext, renderersFactory, trackSelector, loadControl);
        //ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();
        mInternalPlayer.addListener(mEventListener);
        mInternalPlayer.addVideoListener(mVideoListener);
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        String data = dataSource.getData();
        Map<String, String> headers = dataSource.getExtra();
        Log.e(TAG, "URL Link = " + data);
        //播放本地视频 不需要缓存
        matchUrl(data, data.contains(LeCast.getInstance().getWifiIp()) || data.contains("127.0.0.1"), dataSource.getFrom(), headers);
        // Prepare the player with the source.
        isPreparing = true;
        mInternalPlayer.prepare(videoSource);
        mInternalPlayer.setPlayWhenReady(false);

        Bundle sourceBundle = BundlePool.obtain();
        sourceBundle.putSerializable(EventKey.SERIALIZABLE_DATA, dataSource);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET, sourceBundle);
    }

    private void matchUrl(String url, boolean isLocal, int from, Map<String, String> headers) {
        Uri uri = Uri.parse(url);
        if (url.matches(".*m3u8.*")) { //m3u8
            HttpDataSource.Factory okHttpDataSourceFactory = new OkHttpDataSourceFactory(OkHttpClientFactory.getInstance().getOkHttpClient(), "ua");
            if (isLocal) {
                PlayerLibrary.isLocal = 1;
            } else {
                PlayerLibrary.isLocal = 0;
                //设置headers
                if (headers != null) { //waiju
                    okHttpDataSourceFactory.getDefaultRequestProperties().set(headers);
                }
            }
            if (from == 13) {
                okHttpDataSourceFactory = new CustomOkHttpDataSourceFactory(OkHttpClientFactory.getInstance().getOkHttpClient(), "ua");
            }
            videoSource = new HlsMediaSource.Factory(okHttpDataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(uri);
            videoSource.addEventListener(new Handler(), mediaSourceEventListener);
        } else {
            if (isLocal) {
                HttpDataSource.Factory okHttpDataSourceFactory = new OkHttpDataSourceFactory(OkHttpClientFactory.getInstance().getOkHttpClient(),
                        Util.getUserAgent(mAppContext, "exo"));
                videoSource = new ExtractorMediaSource.Factory(okHttpDataSourceFactory)
                        .createMediaSource(uri);
            } else {
                String ua = from == 0 ? userAgent(mAppContext) : null;
                HttpDataSource.Factory okHttpDataSourceFactory = new OkHttpDataSourceFactory(
                        OkHttpClientFactory.getInstance().getOkHttpClient(), ua);
                //设置headers
                if (headers != null) {
                    okHttpDataSourceFactory.getDefaultRequestProperties().set(headers);
                }
                videoSource = new ExtractorMediaSource.Factory(okHttpDataSourceFactory)
                        .createMediaSource(uri);
            }
        }
    }

    public String userAgent(Context context) {
        StringBuffer sb = new StringBuffer();
        String packageName = context.getPackageName();
        PackageInfo info;
        String versionName;
        try {
            info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "?";
        }
        String referer = "app.51octopus.com";
        sb.append("versionName:").append(versionName).append(" ");
        sb.append("referer:").append(referer);
        return sb.toString();
    }

    private void startBufferingUpdateTask() {
        stopBufferingUpdateTask();
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                final int percent = mInternalPlayer.getBufferedPercentage();
                if (percent < 100) {
                    submitBufferingUpdate(percent, null);
                } else {
                    stopBufferingUpdateTask();
                }
                PLog.e(TAG, "percent: " + percent);
            }
        };
        mTimer.schedule(mTask, 3000, 1000);
    }

    private void stopBufferingUpdateTask() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        mInternalPlayer.setVideoSurfaceHolder(surfaceHolder);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_HOLDER_UPDATE, null);
    }

    @Override
    public void setSurface(Surface surface) {
        mInternalPlayer.setVideoSurface(surface);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SURFACE_UPDATE, null);
    }

    @Override
    public void setVolume(float left, float right) {
        mInternalPlayer.setVolume(left);
    }

    @Override
    public void setSpeed(float speed) {
        PlaybackParameters parameters = new PlaybackParameters(speed, 1f);
        mInternalPlayer.setPlaybackParameters(parameters);
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null)
            return false;
        int state = mInternalPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public int getCurrentPosition() {
        return (int) mInternalPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) mInternalPlayer.getDuration();
    }

    @Override
    public int getAudioSessionId() {
        return mInternalPlayer.getAudioSessionId();
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public void start() {
        mInternalPlayer.setPlayWhenReady(true);
        startBufferingUpdateTask();
    }

    @Override
    public void start(int msc) {
        mStartPos = msc;
        start();
    }

    @Override
    public void pause() {
        int state = getState();
        if (isInPlaybackState()
                && state != STATE_END
                && state != STATE_ERROR
                && state != STATE_IDLE
                && state != STATE_INITIALIZED
                && state != STATE_PAUSED
                && state != STATE_STOPPED)
            mInternalPlayer.setPlayWhenReady(false);
    }

    @Override
    public void resume() {
        if (isInPlaybackState() && getState() == STATE_PAUSED)
            mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            isPendingSeek = true;
        }
        mInternalPlayer.seekTo(msc);
        Bundle bundle = BundlePool.obtain();
        bundle.putInt(EventKey.INT_DATA, msc);
        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO, bundle);
    }

    @Override
    public void stop() {
        isPreparing = true;
        isBuffering = false;
        updateStatus(IPlayer.STATE_STOPPED);
        mInternalPlayer.stop();
    }

    @Override
    public void reset() {
        stop();
    }

    @Override
    public void destroy() {
        stopBufferingUpdateTask();
        isPreparing = true;
        isBuffering = false;
        updateStatus(IPlayer.STATE_END);
        mInternalPlayer.removeListener(mEventListener);
        mInternalPlayer.removeVideoListener(mVideoListener);
        mInternalPlayer.release();
    }

    private boolean isInPlaybackState() {
        int state = getState();
        return state != STATE_END
                && state != STATE_ERROR
                && state != STATE_INITIALIZED
                && state != STATE_STOPPED;
    }

    private VideoListener mVideoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            mVideoWidth = width;
            mVideoHeight = height;
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(EventKey.INT_ARG1, mVideoWidth);
            bundle.putInt(EventKey.INT_ARG2, mVideoHeight);
            bundle.putInt(EventKey.INT_ARG3, 0);
            bundle.putInt(EventKey.INT_ARG4, 0);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_SIZE_CHANGE, bundle);
        }

        @Override
        public void onRenderedFirstFrame() {
            PLog.d(TAG, "onRenderedFirstFrame duration: " + getDuration());
            updateStatus(IPlayer.STATE_STARTED);
            submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START, null);
        }
    };

    private Player.EventListener mEventListener = new Player.EventListener() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            PLog.d(TAG, "onTracksChanged:");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            PLog.d(TAG, "onPlayerStateChanged : playWhenReady = " + playWhenReady
                    + ", playbackState = " + playbackState);

            if (!isPreparing) {
                if (playWhenReady) {
                    updateStatus(IPlayer.STATE_STARTED);
                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESUME, null);
                } else {
                    updateStatus(IPlayer.STATE_PAUSED);
                    submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PAUSE, null);
                }
            }

            if (isPreparing && playbackState == Player.STATE_READY) {
                isPreparing = false;
                Format format = mInternalPlayer.getVideoFormat();
                Bundle bundle = BundlePool.obtain();
                if (format != null) {
                    bundle.putInt(EventKey.INT_ARG1, format.width);
                    bundle.putInt(EventKey.INT_ARG2, format.height);
                }
                updateStatus(IPlayer.STATE_PREPARED);
                submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED, bundle);
                if (mStartPos > 0) {
                    mInternalPlayer.seekTo(mStartPos);
                    mStartPos = -1;
                }
            }

            if (isBuffering) {
                switch (playbackState) {
                    case Player.STATE_READY:
                    case Player.STATE_ENDED:
                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
                        PLog.d(TAG, "buffer_end, BandWidth : " + bitrateEstimate);
                        isBuffering = false;
                        Bundle bundle = BundlePool.obtain();
                        bundle.putLong(EventKey.LONG_DATA, bitrateEstimate);
                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END, bundle);
                        break;
                }
            }

            if (isPendingSeek && playbackState == Player.STATE_READY) {
                isPendingSeek = false;
                submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE, null);

            }

            if (!isPreparing) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
                        PLog.d(TAG, "buffer_start, BandWidth : " + bitrateEstimate);
                        isBuffering = true;
                        Bundle bundle = BundlePool.obtain();
                        bundle.putLong(EventKey.LONG_DATA, bitrateEstimate);
                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START, bundle);
                        break;
                    case Player.STATE_ENDED:
                        updateStatus(IPlayer.STATE_PLAYBACK_COMPLETE);
                        isPreparing = true;
                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE, null);
                        break;
                }
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error == null) {
                submitErrorEvent(OnErrorEventListener.ERROR_EVENT_UNKNOWN, null);
                return;
            }
            PLog.e(TAG, error.getMessage() == null ? "" : error.getMessage());
            int type = error.type;
            switch (type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    submitErrorEvent(OnErrorEventListener.ERROR_EVENT_IO, null);
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    submitErrorEvent(OnErrorEventListener.ERROR_EVENT_COMMON, null);
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    submitErrorEvent(OnErrorEventListener.ERROR_EVENT_UNKNOWN, null);
                    break;
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            PLog.d(TAG, "onPlaybackParametersChanged : " + playbackParameters.toString());
        }
    };

    /**
     * m3u8类型视频,遇到不存在的ts文件,跳过该文件
     */
    private MediaSourceEventListener mediaSourceEventListener = new MediaSourceEventListener() {
        @Override
        public void onMediaPeriodCreated(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
//            PLog.d("onMediaPeriodCreated", "mediaPeriodId " + mediaPeriodId);
        }

        @Override
        public void onMediaPeriodReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
//            PLog.d("onMediaPeriodReleased", "mediaPeriodId " + mediaPeriodId);
        }

        @Override
        public void onLoadStarted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
//            PLog.d("onLoadStarted", "mediaPeriodId " + mediaPeriodId + "  loadEventInfo " + loadEventInfo + "  mediaLoadData " + mediaLoadData);
        }

        @Override
        public void onLoadCompleted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
//            PLog.d("onLoadCompleted", "mediaPeriodId " + mediaPeriodId + "  loadEventInfo " + loadEventInfo + "  mediaLoadData " + mediaLoadData);
        }

        @Override
        public void onLoadCanceled(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
//            PLog.d("onLoadCanceled", "mediaPeriodId " + mediaPeriodId + "  loadEventInfo " + loadEventInfo + "  mediaLoadData " + mediaLoadData);
        }

        @Override
        public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
            if (error instanceof HttpDataSource.InvalidResponseCodeException) {
                if (((HttpDataSource.InvalidResponseCodeException) error).responseCode == 404) {
//                    seekTo((int) (mediaLoadData.mediaEndTimeMs + 10));
                    Bundle bundle = new Bundle();
                    bundle.putString("message", error.getMessage() == null ? "" : error.getMessage());
                    submitErrorEvent(OnErrorEventListener.ERROR_EVENT_TS_NOTFOUND, bundle);
                }
                PLog.d("onLoadError---1 ", " " + error.getCause());
            } else if (error.getCause() != null && error.getCause().toString().contains("connect to")) {
                PLog.d("onLoadError---2 ", " " + error.getCause());
                Bundle bundle = new Bundle();
                bundle.putString("message", error.getMessage() == null ? "" : error.getMessage());
                submitErrorEvent(OnErrorEventListener.ERROR_EVENT_TIME_OUT, bundle);
            }
        }

        @Override
        public void onReadingStarted(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
//            PLog.d("onReadingStarted", "mediaPeriodId " + mediaPeriodId);
        }

        @Override
        public void onUpstreamDiscarded(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
//            PLog.d("onUpstreamDiscarded", "mediaPeriodId " + mediaPeriodId);
        }

        @Override
        public void onDownstreamFormatChanged(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
//            PLog.d("onDownstreamForma", "mediaPeriodId " + mediaPeriodId);
        }
    };
}
