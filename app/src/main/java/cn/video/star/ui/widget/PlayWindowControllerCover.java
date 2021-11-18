package cn.video.star.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import cn.video.star.R;
import cn.video.star.base.DataInter;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.BundlePool;
import com.kk.taurus.playerbase.event.EventKey;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.player.OnTimerUpdateListener;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.touch.OnTouchGestureListener;

public class PlayWindowControllerCover extends BaseCover implements View.OnClickListener,
        OnTouchGestureListener, OnTimerUpdateListener {

    private final int MSG_CODE_DELAY_HIDDEN_CONTROLLER = 101;

    View mBottomContainer;

    ImageView mStateIcon, closeWindow, returnPlay, rewindImg, forwardImg;

    ProgressBar bottomBar;

    private int mSeekProgress = -1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CODE_DELAY_HIDDEN_CONTROLLER:
                    setControllerState(false);
                    break;
            }
        }
    };

    private ObjectAnimator mBottomAnimator;
    private ObjectAnimator mRightAnimator;

    public PlayWindowControllerCover(Context context) {
        super(context);
        mBottomContainer = findViewById(R.id.cover_player_controller_bottom_container);
        mStateIcon = findViewById(R.id.cover_player_controller_image_view_play_state);
        returnPlay = findViewById(R.id.return_play);
        closeWindow = findViewById(R.id.close_window);
        bottomBar = findViewById(R.id.bottom_progress);
        rewindImg = findViewById(R.id.rewind_img);
        forwardImg = findViewById(R.id.forward_img);
        mStateIcon.setOnClickListener(this);
        closeWindow.setOnClickListener(this);
        returnPlay.setOnClickListener(this);
        rewindImg.setOnClickListener(this);
        forwardImg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cover_player_controller_image_view_play_state:
                boolean selected = mStateIcon.isSelected();
                if (selected) {
                    requestResume(null); //恢复播放
                } else {
                    requestPause(null);  //暂停播放
                }
                mStateIcon.setSelected(!selected);
                break;
            case R.id.close_window:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_CLOSE_WINDOW, null); //关闭小窗
                break;
            case R.id.return_play:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_RETURN_PLAY, null); //返回全屏
                break;
            case R.id.rewind_img://后退5秒
                sendSeekEvent(bottomBar.getProgress() - 5000);
                break;
            case R.id.forward_img: //快进5秒
                sendSeekEvent(bottomBar.getProgress() + 5000);
                break;
        }
    }

    @Override
    public void onReceiverBind() {
        super.onReceiverBind();
    }

    @Override
    protected void onCoverAttachedToWindow() {
        super.onCoverAttachedToWindow();
        DataSource dataSource = getGroupValue().get(DataInter.Key.KEY_DATA_SOURCE);
        setTitle(dataSource);
    }

    @Override
    protected void onCoverDetachedToWindow() {
        super.onCoverDetachedToWindow();
        mBottomContainer.setVisibility(View.GONE);
        removeDelayHiddenMessage();
    }

    @Override
    public void onReceiverUnBind() {
        super.onReceiverUnBind();
        cancelBottomAnimation();
        cancelRightAnimation();
        removeDelayHiddenMessage();
        mHandler.removeCallbacks(mSeekEventRunnable);
    }

    private void setTitle(DataSource dataSource) {
        if (dataSource != null) {
            String title = dataSource.getTitle();
            if (!TextUtils.isEmpty(title)) {
                return;
            }
            String data = dataSource.getData();
            if (!TextUtils.isEmpty(data)) {
            }
        }
    }

    private void cancelBottomAnimation() {
        if (mBottomAnimator != null) {
            mBottomAnimator.cancel();
            mBottomAnimator.removeAllListeners();
            mBottomAnimator.removeAllUpdateListeners();
        }
    }

    private void cancelRightAnimation() {
        if (mRightAnimator != null) {
            mRightAnimator.cancel();
            mRightAnimator.removeAllListeners();
            mRightAnimator.removeAllUpdateListeners();
        }
    }

    private void setBottomContainerState(final boolean state) {
        mBottomContainer.clearAnimation();
        cancelBottomAnimation();
        mBottomAnimator = ObjectAnimator.ofFloat(mBottomContainer,
                "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
        mBottomAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (state) {
                    mBottomContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!state) {
                    mBottomContainer.setVisibility(View.GONE);
                }
            }
        });
        mBottomAnimator.start();
        if (state) {
            requestNotifyTimer();
        } else {
            requestStopTimer();
        }
    }

    private void setControllerState(boolean state) {
        if (state) {
            sendDelayHiddenMessage();
        } else {
            removeDelayHiddenMessage();
        }
        setBottomContainerState(state);
    }

    private boolean isControllerShow() {
        return mBottomContainer.getVisibility() == View.VISIBLE;
    }

    //是否显示控制器
    private void toggleController() {
        if (isControllerShow()) {
            setControllerState(false);
        } else {
            setControllerState(true);
        }
    }

    private void sendDelayHiddenMessage() {
        removeDelayHiddenMessage();
        mHandler.sendEmptyMessageDelayed(MSG_CODE_DELAY_HIDDEN_CONTROLLER, 5000);
    }

    private void removeDelayHiddenMessage() {
        mHandler.removeMessages(MSG_CODE_DELAY_HIDDEN_CONTROLLER);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET:
                DataSource data = (DataSource) bundle.getSerializable(EventKey.SERIALIZABLE_DATA);
                getGroupValue().putObject(DataInter.Key.KEY_DATA_SOURCE, data);
                setTitle(data);
                break;
            case OnPlayerEventListener.PLAYER_EVENT_ON_STATUS_CHANGE:
                int status = bundle.getInt(EventKey.INT_DATA);
                if (status == IPlayer.STATE_PAUSED) {
                    mStateIcon.setSelected(true);
                } else if (status == IPlayer.STATE_STARTED) {
                    mStateIcon.setSelected(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorEvent(int eventCode, Bundle bundle) {
    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle) {
    }

    @Override
    public Bundle onPrivateEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case DataInter.PrivateEvent.EVENT_CODE_UPDATE_SEEK:
                if (bundle != null) {
                    int curr = bundle.getInt(EventKey.INT_ARG1);
                    int duration = bundle.getInt(EventKey.INT_ARG2);
                    updateUI(curr, duration);
                }
                break;
        }
        return null;
    }

    @Override
    public View onCreateCoverView(Context context) {
        return View.inflate(context, R.layout.layout_window_controller_cover, null);
    }

    @Override
    public int getCoverLevel() {
        return levelLow(11);
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        updateUI(progress, seekBar.getMax());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendSeekEvent(seekBar.getProgress());
                }
            };

    private void sendSeekEvent(int progress) {
        mSeekProgress = progress;
        mHandler.removeCallbacks(mSeekEventRunnable);
        mHandler.postDelayed(mSeekEventRunnable, 300);
    }

    private Runnable mSeekEventRunnable = () -> {
        if (mSeekProgress < 0)
            return;
        Bundle bundle = BundlePool.obtain();
        bundle.putInt(EventKey.INT_DATA, mSeekProgress);
        requestSeek(bundle);  //发送seek事件到播放器
    };

    @Override
    public void onTimerUpdate(int curr, int duration, int bufferPercentage) {
        updateUI(curr, duration);
        Log.d("onTimerUpdate", "bufferPercentage: " + bufferPercentage);
    }

    private void updateUI(int curr, int duration) {
        bottomBar.setMax(duration);
        bottomBar.setProgress(curr);
    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        toggleController();
    }

    @Override
    public void onDoubleTap(MotionEvent event) {
        //双击暂停播放
        boolean selected = mStateIcon.isSelected();
        if (selected) {
            requestResume(null); //恢复播放
        } else {
            requestPause(null);  //暂停播放
        }
        mStateIcon.setSelected(!selected);
    }

    @Override
    public void onDown(MotionEvent event) {
    }

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    }

    @Override
    public void onEndGesture() {
    }
}
