package cn.video.star.ui.widget;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import cn.video.star.R;
import cn.video.star.base.DataInter;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.receiver.IReceiverGroup;
import com.kk.taurus.playerbase.receiver.PlayerStateGetter;

import java.text.DecimalFormat;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Taurus on 2018/4/15.
 */

public class PlayLoadingCover extends BaseCover {

    private final int MSG_CODE_NET_SPEED = 101;

    private Long lastTotalRxBytes = 0L, lastTimeStamp = 0L;

    //网速
    public TextView loadingSpeedText;

    private ScheduledExecutorService timer;

    private TimerTask task;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CODE_NET_SPEED:
                    Log.d("loadingSpeedText", "--" + getNetSpeed());
                    loadingSpeedText.setText(getNetSpeed());
                    break;
            }
        }
    };

    public PlayLoadingCover(Context context) {
        super(context);
        loadingSpeedText = findViewById(R.id.loading_speed_text);
        showLoadSpeed();
    }

    @Override
    protected void onCoverAttachedToWindow() {
        super.onCoverAttachedToWindow();
        PlayerStateGetter playerStateGetter = getPlayerStateGetter();
        if (playerStateGetter != null && isInPlaybackState(playerStateGetter)) {
            setLoadingState(playerStateGetter.isBuffering());
        }
    }

    @Override
    public void onReceiverBind() {
        getGroupValue().registerOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    @Override
    public void onReceiverUnBind() {
        getGroupValue().unregisterOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    //监听是否显示loading状态
    private IReceiverGroup.OnGroupValueUpdateListener mOnGroupValueUpdateListener =
            new IReceiverGroup.OnGroupValueUpdateListener() {
                @Override
                public String[] filterKeys() {
                    return new String[]{
                            DataInter.Key.KEY_SHOW_LOADING};
                }

                @Override
                public void onValueUpdate(String key, Object value) {
                    if (key.equals(DataInter.Key.KEY_SHOW_LOADING)) {
                        boolean show = (boolean) value;
                        setLoadingState(show);
                    }
                }
            };

    private boolean isInPlaybackState(PlayerStateGetter playerStateGetter) {
        int state = playerStateGetter.getState();
        return state != IPlayer.STATE_END
                && state != IPlayer.STATE_ERROR
                && state != IPlayer.STATE_IDLE
                && state != IPlayer.STATE_INITIALIZED
                && state != IPlayer.STATE_STOPPED;
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START:
            case OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET:
            case OnPlayerEventListener.PLAYER_EVENT_ON_PROVIDER_DATA_START:
            case OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO:
                setLoadingState(true);
                break;

            case OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START:
            case OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END:
            case OnPlayerEventListener.PLAYER_EVENT_ON_STOP:
            case OnPlayerEventListener.PLAYER_EVENT_ON_PROVIDER_DATA_ERROR:
            case OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE:
                setLoadingState(false);
                break;
        }
    }

    @Override
    public void onErrorEvent(int eventCode, Bundle bundle) {
        setLoadingState(false);
    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle) {
    }

    @Override
    protected void onCoverDetachedToWindow() {
        super.onCoverDetachedToWindow();
        mHandler.removeMessages(MSG_CODE_NET_SPEED);
        stopLoadSpeed();
    }

    private void setLoadingState(boolean show) {
        if (show) {
            showLoadSpeed();
        } else {
            stopLoadSpeed();
        }
        setCoverVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateCoverView(Context context) {
        return View.inflate(context, R.layout.layout_loading_cover, null);
    }

    @Override
    public int getCoverLevel() {
        return levelMedium(1);
    }

    private String getNetSpeed() {
        String netSpeed;
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(getContext().getApplicationInfo().uid) ==
                TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            netSpeed = String.valueOf(1) + " kb/s";
            return netSpeed;
        }
        //毫秒转换
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        if (speed > 1024) {
            DecimalFormat df = new DecimalFormat("######0.0");
            netSpeed = String.valueOf(df.format(getM(speed))) + " MB/s";
        } else {
            netSpeed = String.valueOf(speed) + " kb/s";
        }
        return netSpeed;
    }

    public double getM(long k) {
        double m;
        m = k / 1024.0;
        return m;
    }

    private void showLoadSpeed() {
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(MSG_CODE_NET_SPEED);
                }
            };
        }
        if (null == timer) {
            timer = Executors.newScheduledThreadPool(2);
            if (task != null) {
                timer.scheduleWithFixedDelay(task, 0, 800, TimeUnit.MILLISECONDS);
            }
        }
    }

    //activity stop
    public void stopLoadSpeed() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            timer = null;
        }
    }
}
