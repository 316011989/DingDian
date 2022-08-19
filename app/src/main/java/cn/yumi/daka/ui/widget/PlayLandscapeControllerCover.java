package cn.yumi.daka.ui.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static cn.yumi.daka.base.DataInter.Key.KEY_CASTDEVICE_INDEX;
import static cn.yumi.daka.base.DataInter.Key.KEY_LOCK_SCREEN;
import static cn.yumi.daka.base.DataInter.Key.KEY_PLAY_CLARITY;
import static cn.yumi.daka.base.DataInter.Key.KEY_PLAY_ESP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.BundlePool;
import com.kk.taurus.playerbase.event.EventKey;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.lebo.LeCast;
import com.kk.taurus.playerbase.log.PLog;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.player.OnTimerUpdateListener;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.receiver.IReceiverGroup;
import com.kk.taurus.playerbase.touch.OnTouchGestureListener;
import com.kk.taurus.playerbase.utils.TimeUtil;
import com.kk.taurus.playerbase.utils.VideoUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.junechiu.junecore.anim.viewanimator.ViewAnimator;
import cn.junechiu.junecore.utils.ScreenUtil;
import cn.yumi.daka.R;
import cn.yumi.daka.base.Api;
import cn.yumi.daka.base.DataInter;
import cn.yumi.daka.data.remote.model.VideoPlay;
import cn.yumi.daka.ui.activity.CastHelperActivity;
import cn.yumi.daka.ui.activity.PlayerHelper;
import cn.yumi.daka.ui.adapter.PlayerCoverCastAdapter;
import cn.yumi.daka.ui.adapter.PlayerCoverClarityAdapter;
import cn.yumi.daka.ui.adapter.PlayerCoverEpisodeAdapter;
import cn.yumi.daka.ui.adapter.PlayerCoverPicRatioAdapter;
import cn.yumi.daka.ui.adapter.PlayerCoverSpeedAdapter;
import cn.yumi.daka.utils.CommonUtil;

/**
 * Created by Taurus on 2018/4/15.
 */

public class PlayLandscapeControllerCover extends BaseCover implements OnTimerUpdateListener, OnTouchGestureListener, View.OnClickListener {

    private final int MSG_CODE_DELAY_HIDDEN_CONTROLLER = 101;

    private int selectPanelLayWidth = 358;

    private final int espNum = 5;

    private View mTopContainer;
    private View mBottomContainer;
    private View mRightContainer;
    private ImageView mBackIcon;
    private TextView mTopTitle;
    private ImageView mStateIcon;
    private TextView mPlayTime;
    private SeekBar mSeekBar;
    private TextView currentTime;
    private ImageView batteryLevel;
    private ImageView nextEsp;
    private TextView speedText, clarityText, espText;//播放倍速,清晰度,选集
    private RelativeLayout selectPanelLay;  //界面右半部分
    private FrameLayout selectPanel; //选集、清晰、缓存列表布局
    private ImageView castBtn, likeBtn, shareBtn, lockBtn, playWindow, pictureRatio;
    private RelativeLayout tipLayout;
    private TextView tipText;

    private PlayerCoverSpeedAdapter speedAdapter;//播放器速率选择浮层的适配器

    private PlayerCoverClarityAdapter clarityAdapter;//视频清晰度选择浮层的适配器

    private PlayerCoverPicRatioAdapter ratioAdapter;

    private List<VideoPlay> episodeList = new ArrayList();//可选集数集合
    private List<String> rateList = new ArrayList();//可选分辨率集合

    private PlayerCoverEpisodeAdapter espAdapter;

    private int mSeekProgress = -1;

    private boolean mTimerUpdateProgressEnable = true;

    public int isLock = 0; //是否锁屏

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CODE_DELAY_HIDDEN_CONTROLLER:
                    PLog.d(getTag().toString(), "msg_delay_hidden...");
                    setControllerState(false);
                    setLockState(false);
                    setTipLayoutState(false);
                    break;
            }
        }
    };

    private boolean mGestureEnable = true;
    private boolean mControllerTopEnable;
    private ObjectAnimator mBottomAnimator;
    private ObjectAnimator mTopAnimator;
    private ObjectAnimator mLockAnimator;
    private ObjectAnimator mRightAnimator;
    private ObjectAnimator mTipAnimator;
    private ObjectAnimator selectPanelAnimator;

    public PlayLandscapeControllerCover(Context context) {
        super(context);
        mTopContainer = findViewById(R.id.cover_player_controller_top_container);
        mBottomContainer = findViewById(R.id.cover_player_controller_bottom_container);
        mRightContainer = findViewById(R.id.right_lay);
        mBackIcon = findViewById(R.id.cover_player_controller_image_view_back_icon);
        mTopTitle = findViewById(R.id.cover_player_controller_text_view_video_title);
        mStateIcon = findViewById(R.id.cover_player_controller_image_view_play_state);
        mPlayTime = findViewById(R.id.cover_player_controller_text_view_play_time);
        mSeekBar = findViewById(R.id.cover_player_controller_seek_bar);
        currentTime = findViewById(R.id.video_current_time);
        batteryLevel = findViewById(R.id.battery_level);
        nextEsp = findViewById(R.id.cover_player_controller_image_view_next_esp);
        speedText = findViewById(R.id.cover_player_controller_text_view_speed);
        clarityText = findViewById(R.id.cover_player_controller_text_view_clarity);
        selectPanel = findViewById(R.id.select_panel);
        selectPanelLay = findViewById(R.id.select_panel_lay);
        espText = findViewById(R.id.cover_player_controller_text_view_episode);
        castBtn = findViewById(R.id.cast_btn);
        likeBtn = findViewById(R.id.like_btn);
        shareBtn = findViewById(R.id.share_btn);
        lockBtn = findViewById(R.id.cover_player_controller_image_view_lock_state);
        playWindow = findViewById(R.id.play_window);
        pictureRatio = findViewById(R.id.picture_ratio);
        tipLayout = findViewById(R.id.tip_layout);
        tipText = findViewById(R.id.tip_text);

        mBackIcon.setOnClickListener(this);
        mStateIcon.setOnClickListener(this);
        nextEsp.setOnClickListener(this);
        speedText.setOnClickListener(this);
        clarityText.setOnClickListener(this);
        espText.setOnClickListener(this);
        castBtn.setOnClickListener(this);
        likeBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        lockBtn.setOnClickListener(this);
        playWindow.setOnClickListener(this);
        pictureRatio.setOnClickListener(this);
        tipText.setOnClickListener(this);

        selectPanelLay.setOnClickListener(v -> popAnim());

        speedAdapter = new PlayerCoverSpeedAdapter(getContext(), CommonUtil.Companion.getSpeeds(), PlayerHelper.Companion.getPLAY_SPEED());
        ratioAdapter = new PlayerCoverPicRatioAdapter(getContext(), CommonUtil.Companion.getRatios(), PlayerHelper.Companion.getPIC_RATIO());
        int itemWidth = (VideoUtil.dp2px(selectPanelLayWidth) - VideoUtil.dp2px(7f) * (espNum - 1) - VideoUtil.dp2px(80)) / espNum;
        espAdapter = new PlayerCoverEpisodeAdapter(getContext(), episodeList, PlayerHelper.Companion.getCHOOSEN_EPISODE_INDEX(), itemWidth);
        clarityAdapter = new PlayerCoverClarityAdapter(getContext(), rateList, PlayerHelper.Companion.getPLAY_CLARITY());
        setSpeedText();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cover_player_controller_image_view_back_icon:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_REQUEST_BACK, null);
                break;
            case R.id.cover_player_controller_image_view_play_state:
                boolean selected = mStateIcon.isSelected();
                if (selected) {
                    requestResume(null); //恢复播放
                } else {
                    requestPause(null);  //暂停播放
                }
                mStateIcon.setSelected(!selected);
                break;
            case R.id.cover_player_controller_image_view_switch_screen:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_REQUEST_TOGGLE_SCREEN, null);
                break;
            case R.id.cover_player_controller_image_view_next_esp:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_NEXT_ESP, null);
                break;
            case R.id.cover_player_controller_text_view_speed:
                speedView();
                break;
            case R.id.cover_player_controller_text_view_episode:
                changeEpisodeView();
                break;
            case R.id.cast_btn:
                castListView();
                break;
            case R.id.share_btn:
                shareView();
                break;
            case R.id.like_btn:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_LIKE_VIDEO, null); //取消喜欢
                break;
            case R.id.cover_player_controller_image_view_lock_state:
                boolean locked = lockBtn.isSelected();
                Bundle bundle = BundlePool.obtain();
                if (locked) {
                    isLock = 0; //解锁
                    setControllerState(true);
                } else {
                    isLock = 1; //锁屏
                    setControllerState(false);
                }
                PlayerHelper.Companion.setLOCK_SCREEN(isLock);
                bundle.putInt(KEY_LOCK_SCREEN, isLock);
                notifyReceiverEvent(DataInter.Event.EVENT_SELECT_LOCK_SCREEN, bundle);
                lockBtn.setSelected(!locked);
                break;
            case R.id.play_window:
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_PLAY_WINDOW, null); //小窗播放
                break;
            case R.id.picture_ratio:
                pictureRatioView();
                break;
            case R.id.cover_player_controller_text_view_clarity:
                changeClarityView();
                break;
            case R.id.tip_text:
                setTipLayoutState(false);
                break;
            case R.id.castlist_refresh:
                LeCast.getInstance().browse();//搜索
                if (selectPanelLay.getVisibility() == View.VISIBLE && selectPanel.getChildCount() > 0) {
                    View castView = selectPanel.getChildAt(0);
                    RecyclerView listView = castView.findViewById(R.id.recycler_view);
                    listView.getAdapter().notifyDataSetChanged();
                }
                break;
            default:
        }
    }

    @Override
    public void onReceiverBind() {
        super.onReceiverBind();
        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        getGroupValue().registerOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    @Override
    protected void onCoverAttachedToWindow() {
        super.onCoverAttachedToWindow();
        DataSource dataSource = getGroupValue().get(DataInter.Key.KEY_DATA_SOURCE);
        setTitle(dataSource);

        boolean topEnable = getGroupValue().getBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, false);
        mControllerTopEnable = topEnable;
        if (!topEnable) {
            setTopContainerState(false);
        }
        toggleController();
    }

    @Override
    protected void onCoverDetachedToWindow() {
        super.onCoverDetachedToWindow();
        mTopContainer.setVisibility(GONE);
        mBottomContainer.setVisibility(GONE);
        lockBtn.setVisibility(GONE);
        mRightContainer.setVisibility(GONE);
        removeDelayHiddenMessage();
    }

    @Override
    public void onReceiverUnBind() {
        super.onReceiverUnBind();

        cancelTopAnimation();
        cancelBottomAnimation();
        cancelRightAnimation();
        cancelLockAnimation();

        getGroupValue().unregisterOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
        removeDelayHiddenMessage();
        mHandler.removeCallbacks(mSeekEventRunnable);
    }

    //接受组件传过来的信息
    private IReceiverGroup.OnGroupValueUpdateListener mOnGroupValueUpdateListener =
            new IReceiverGroup.OnGroupValueUpdateListener() {
                @Override
                public String[] filterKeys() {
                    return new String[]{
                            DataInter.Key.KEY_COMPLETE_SHOW,
                            DataInter.Key.KEY_TIMER_UPDATE_ENABLE,
                            DataInter.Key.KEY_DATA_SOURCE,
                            DataInter.Key.KEY_IS_LANDSCAPE,
                            DataInter.Key.KEY_CONTROLLER_TOP_ENABLE,
                            DataInter.Key.KEY_VIDEO_LIKE,
                            DataInter.Key.KEY_CHANGE_CAST,
                            DataInter.Key.KEY_BATTERY_PERCENT,
                            DataInter.Key.KEY_NAVIGATIONBARHEIGHT};
                }

                @Override
                public void onValueUpdate(String key, Object value) {
                    if (key.equals(DataInter.Key.KEY_COMPLETE_SHOW)) {
                        boolean show = (boolean) value;
                        if (show) {
                            setControllerState(false);
                        }
                        setGestureEnable(!show);
                    } else if (key.equals(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE)) {
                        mControllerTopEnable = (boolean) value;
                        if (!mControllerTopEnable) {
                            setTopContainerState(false);
                        }
                    } else if (key.equals(DataInter.Key.KEY_TIMER_UPDATE_ENABLE)) {
                        mTimerUpdateProgressEnable = (boolean) value;
                    } else if (key.equals(DataInter.Key.KEY_DATA_SOURCE)) {
                        DataSource dataSource = (DataSource) value;
                        setTitle(dataSource);
                        setEspData(dataSource);
                        setRateData(dataSource);
                    } else if (key.equals(DataInter.Key.KEY_VIDEO_LIKE)) {//接受喜欢回传事件
                        if ((boolean) value) {
                            likeBtn.setSelected(true);
                        } else {
                            likeBtn.setSelected(false);
                        }
                    } else if (key.equals(DataInter.Key.KEY_CHANGE_CAST)) { //切换投屏设备
                        castListView();
                    } else if (key.equals(DataInter.Key.KEY_BATTERY_PERCENT)) {
                        setBatteryLevel((int) value);
                    } else if (key.equals(DataInter.Key.KEY_NAVIGATIONBARHEIGHT)) {
                        mTopContainer.setPadding(0, 0, (int) value, 0);
                        mBottomContainer.setPadding(0, 0, (int) value, 0);
                        mRightContainer.setPadding(0, 0, (int) value + ScreenUtil.dp2px(10), 0);
                        selectPanel.setPadding(0, 0, (int) value, 0);
                    }
                }
            };

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
        mTimerUpdateProgressEnable = false;
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

    private void setTitle(DataSource dataSource) {
        if (dataSource != null) {
            String title = dataSource.getTitle();
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
                return;
            }
            String data = dataSource.getData();
            if (!TextUtils.isEmpty(data)) {
                setTitle(data);
            }
        }
    }

    private void setEspData(DataSource data) {
        if (data.getType() == Api.TYPE_MOVIE) {
            espText.setVisibility(GONE);
        } else {
            espText.setVisibility(VISIBLE);
        }
        episodeList.clear();
        if (data.getEspList() != null && data.getEspList().size() > 0) {
            for (Object videoPlay : data.getEspList()) {
                episodeList.add((VideoPlay) videoPlay);
            }
        }
        espAdapter.notifyDataSetChanged();
    }

    //分辨率修改
    private void changeClarityView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        View rateView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_speed, null);
        TextView cover_title = rateView.findViewById(R.id.landscape_cover_title);//倍速,清晰度选择列表标题
        cover_title.setText(R.string.play_clarity);
        RecyclerView listView = rateView.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        listView.setAdapter(clarityAdapter);
        selectPanel.addView(rateView);
        pushAnim();
        if (clarityAdapter != null) {
            clarityAdapter.selected = PlayerHelper.Companion.getPLAY_CLARITY();
            clarityAdapter.notifyDataSetChanged();
        }
        if (clarityAdapter != null) {
            clarityAdapter.setOnItemClicklistener(selectedId -> {
                PlayerHelper.Companion.setPLAY_CLARITY(selectedId);
                clarityAdapter.selected = selectedId;
                clarityAdapter.notifyDataSetChanged();
                setRateText(1);
                Bundle bundle = BundlePool.obtain();
                bundle.putString(KEY_PLAY_CLARITY, PlayerHelper.Companion.getPLAY_CLARITY());
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_CHANGE_CLARITY, bundle);
                popAnim();
            });
        }
        goneAllView();
    }

    /**
     * 分辨率修改
     *
     * @param data
     */
    private void setRateData(DataSource data) {
        rateList.clear();
        if (data.getClarities() != null && data.getClarities().size() > 0) {
            rateList.addAll(data.getClarities());
            clarityText.setVisibility(View.VISIBLE);
        } else {
            clarityText.setVisibility(View.GONE);
        }
        clarityAdapter.notifyDataSetChanged();
        setRateText(0);
    }

    //分辨率修改完成后设置文字
    public void setRateText(int flag) {
        if (rateList != null && rateList.size() > 0) {
            for (String rate : rateList) {
                String arr[] = rate.split("&");
                if (arr[0].equals(PlayerHelper.Companion.getPLAY_CLARITY())) {
                    clarityText.setText(arr[1]);
                    if (flag == 1) {
                        setTipText("", arr[1], "切换中,请稍后…");
                    }
                }
            }
        }
    }

    //当前时间
    public void setCurrentTime() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        if (currentTime != null)
            currentTime.setText(dateFormater.format(date));
    }

    //设置电池等级
    public void setBatteryLevel(int percent) {
        if (percent < 15) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_10);
        } else if (percent >= 15 && percent < 40) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_30);
        } else if (percent >= 40 && percent < 60) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_50);
        } else if (percent >= 60 && percent < 80) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_70);
        } else if (percent >= 80 && percent < 95) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_90);
        } else if (percent >= 95 && percent <= 100) {
            batteryLevel.setBackgroundResource(R.mipmap.player_cover_battery_level_100);
        }
    }

    private void setTitle(String text) {
        mTopTitle.setText(text);
    }

    private void setGestureEnable(boolean gestureEnable) {
        this.mGestureEnable = gestureEnable;
    }

    //////////////////////////////////////////动画////////////////////////////////////////////////////
    private void cancelTopAnimation() {
        if (mTopAnimator != null) {
            mTopAnimator.cancel();
            mTopAnimator.removeAllListeners();
            mTopAnimator.removeAllUpdateListeners();
        }
    }

    private void cancelBottomAnimation() {
        if (mBottomAnimator != null) {
            mBottomAnimator.cancel();
            mBottomAnimator.removeAllListeners();
            mBottomAnimator.removeAllUpdateListeners();
        }
    }

    private void cancelLockAnimation() {
        if (mLockAnimator != null) {
            mLockAnimator.cancel();
            mLockAnimator.removeAllListeners();
            mLockAnimator.removeAllUpdateListeners();
        }
    }

    private void cancelRightAnimation() {
        if (mRightAnimator != null) {
            mRightAnimator.cancel();
            mRightAnimator.removeAllListeners();
            mRightAnimator.removeAllUpdateListeners();
        }
    }

    private void cancelTipAnimation() {
        if (mTipAnimator != null) {
            mTipAnimator.cancel();
            mTipAnimator.removeAllListeners();
            mTipAnimator.removeAllUpdateListeners();
        }
    }

    private void setTopContainerState(final boolean state) {
        if (mControllerTopEnable) {
            mTopContainer.clearAnimation();
            cancelTopAnimation();
            mTopAnimator = ObjectAnimator.ofFloat(mTopContainer,
                    "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
            mTopAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (state) {
                        if (isLock == 0) { //没有锁屏
                            mTopContainer.setVisibility(VISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (!state) {
                        mTopContainer.setVisibility(GONE);
                    }
                }
            });
            mTopAnimator.start();
        } else {
            mTopContainer.setVisibility(GONE);
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
                    if (isLock == 0) { //如果没有锁屏
                        mBottomContainer.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!state) {
                    mBottomContainer.setVisibility(GONE);
                }
            }
        });
        mBottomAnimator.start();
        if (state) {
            PLog.d(getTag().toString(), "requestNotifyTimer...");
            requestNotifyTimer();
        } else {
            PLog.d(getTag().toString(), "requestStopTimer...");
            requestStopTimer();
        }
    }

    private void setRightState(final boolean state) {
        mRightContainer.clearAnimation();
        cancelRightAnimation();
        mRightAnimator = ObjectAnimator.ofFloat(mRightContainer,
                "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
        mRightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (state) {
                    if (isLock == 0) { //没有锁屏
                        mRightContainer.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!state) {
                    mRightContainer.setVisibility(GONE);
                }
            }
        });
        mRightAnimator.start();
    }

    private void setLockState(final boolean state) {
        lockBtn.clearAnimation();
        cancelLockAnimation();
        mLockAnimator = ObjectAnimator.ofFloat(lockBtn,
                "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
        mLockAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (state) {
                    lockBtn.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!state) {
                    lockBtn.setVisibility(GONE);
                }
            }
        });
        mLockAnimator.start();
    }

    private void setTipLayoutState(final boolean state) {
        if (state) {
            sendDelayHiddenMessage();
        } else {
            removeDelayHiddenMessage();
        }
        tipLayout.clearAnimation();
        cancelTipAnimation();
        mTipAnimator = ObjectAnimator.ofFloat(tipLayout,
                "alpha", state ? 0 : 1, state ? 1 : 0).setDuration(300);
        mTipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (state) {
                    tipLayout.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!state) {
                    tipLayout.setVisibility(GONE);
                }
            }
        });
        mTipAnimator.start();
    }

    //上下控制面板显示隐藏
    private void setControllerState(boolean state) {
        if (state) {
            setTipLayoutState(false);
            sendDelayHiddenMessage();
        } else {
            removeDelayHiddenMessage();
        }
        Bundle bundle = BundlePool.obtain();
        bundle.putBoolean(DataInter.Key.KEY_SHOW_HIDE_UI, state);
        notifyReceiverEvent(DataInter.Event.KEY_CHANGE_UI, bundle); //显示隐藏系统ui
        setTopContainerState(state);
        setBottomContainerState(state);
        setLockState(state);
        setRightState(state);
    }

    private boolean isControllerShow() {
        return mBottomContainer.getVisibility() == VISIBLE;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    //隐藏所有按钮
    public void goneAllView() {
        setControllerState(false);
        setLockState(false);
        setTipLayoutState(false);
    }

    //点击屏幕显示隐藏控制面板
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

    private void setPlayTime(int curr, int duration) {
        String playTime = TimeUtil.stringForTime(curr) + "/" + TimeUtil.stringForTime(duration);
        mPlayTime.setText(playTime);
    }

    @Override
    public void onTimerUpdate(int curr, int duration, int bufferPercentage) {
        if (!mTimerUpdateProgressEnable)
            return;
        updateUI(curr, duration);
        setCurrentTime(); //更新当前时间
        Log.d("onTimerUpdate", "bufferPercentage: " + bufferPercentage);
        mSeekBar.setSecondaryProgress(bufferPercentage * duration / 100);
    }

    private void updateUI(int curr, int duration) {
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(curr);
        setPlayTime(curr, duration);
    }

    //设置提示信息
    private void setTipText(String startText, String colorText, String endText) {
        String start = CommonUtil.Companion.getColorText("#ffffff", startText);
        String color = CommonUtil.Companion.getColorText("#FFB500", colorText);
        String end = CommonUtil.Companion.getColorText("#ffffff", endText);
        tipText.setText(Html.fromHtml(start + color + end));
        setTipLayoutState(true);
    }

    //画面比例
    private void pictureRatioView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        View picratioView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_picture_ratio, null);
        RecyclerView listView = picratioView.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        listView.setAdapter(ratioAdapter);
        ratioAdapter.notifyDataSetChanged();
        ratioAdapter.setOnItemClicklistener((position) -> {
            ratioAdapter.selected = position;
            ratioAdapter.notifyDataSetChanged();
            PlayerHelper.Companion.setPIC_RATIO(position);
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(DataInter.Key.KEY_PICTURE_RATIO, position);
            notifyReceiverEvent(DataInter.Event.EVENT_CODE_CHANGE_PIC_RATIO, bundle);
            popAnim();
        });
        selectPanel.addView(picratioView);
        pushAnim();
        goneAllView();
    }

    //倍数
    private void speedView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        View speedView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_speed, null);
        TextView cover_title = speedView.findViewById(R.id.landscape_cover_title);
        cover_title.setText(R.string.play_speed);
        RecyclerView listView = speedView.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        listView.setAdapter(speedAdapter);
        speedAdapter.notifyDataSetChanged();
        speedAdapter.setOnItemClicklistener((position) -> {
            speedAdapter.selected = position;
            speedAdapter.notifyDataSetChanged();
            PlayerHelper.Companion.setPLAY_SPEED(position);
            PlayerHelper.Companion.setSHOW_SPEED(1);
            setSpeedText();
            String speed = CommonUtil.Companion.getSpeeds().get(PlayerHelper.Companion.getPLAY_SPEED());
            if (position == 1) {
                setTipText("已恢复", "正常速度", "播放");
            } else {
                setTipText("已为您开启", speed.split(" ")[1] + "倍速", "播放");
            }
            popAnim();
        });
        selectPanel.addView(speedView);
        pushAnim();
        goneAllView();
    }

    public void setSpeedText() {
        String speed = CommonUtil.Companion.getSpeeds().get(PlayerHelper.Companion.getPLAY_SPEED());
        if (PlayerHelper.Companion.getSHOW_SPEED() == 0) {
            speedText.setText(R.string.double_speed);
        } else {
            if (!TextUtils.isEmpty(speed) && speed.contains(" ")) {
                speedText.setText(speed.split(" ")[0]);
            }
        }
        Bundle bundle = BundlePool.obtain();
        bundle.putFloat(DataInter.Key.KEY_PLAY_SPEED, Float.valueOf(speed.split(" ")[1]));
        notifyReceiverEvent(DataInter.Event.EVENT_CODE_CHANGE_SPEED, bundle);
    }

    //剧集
    public void changeEpisodeView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        View episodeView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_episode, null);
        RecyclerView listView = episodeView.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new GridLayoutManager(getContext(), espNum));
        listView.scrollToPosition(PlayerHelper.Companion.getCHOOSEN_EPISODE_INDEX());
        int spacing = VideoUtil.dp2px(7f);
        listView.addItemDecoration(new GridSpacingItemDecoration2(espNum, spacing, true));
        listView.setAdapter(espAdapter);
        selectPanel.addView(episodeView);
        pushAnim();
        if (espAdapter != null) {
            espAdapter.selected = PlayerHelper.Companion.getCHOOSEN_EPISODE_INDEX();
            espAdapter.notifyDataSetChanged();
        }
        espAdapter.setOnItemClicklistener(position -> {
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(KEY_PLAY_ESP, position);
            notifyReceiverEvent(DataInter.Event.EVENT_CODE_CHANGE_ESP, bundle);
            espAdapter.selected = position;
            espAdapter.notifyDataSetChanged();
            popAnim();
        });
        goneAllView();
    }

    //分享
    public void shareView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        View shareView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_share, null);
        LinearLayout wxFriend = shareView.findViewById(R.id.wx_friend);
        LinearLayout wxCircle = shareView.findViewById(R.id.wx_circle);
        wxFriend.setOnClickListener(v -> {
            notifyReceiverEvent(DataInter.Event.EVENT_CODE_SHARE_WX, null);
            popAnim();
        });
        wxCircle.setOnClickListener(v -> {
            notifyReceiverEvent(DataInter.Event.EVENT_CODE_SHARE_CIRCLE, null);
            popAnim();
        });
        selectPanel.addView(shareView);
        pushAnim();
        goneAllView();
    }

    //投屏设备列表
    private void castListView() {
        selectPanelLay.setVisibility(selectPanelLay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        selectPanel.removeAllViews();
        final View castView = LayoutInflater.from(getContext()).inflate(R.layout.player_cover_layout_cast_list, null);
        TextView refreshCastBtn = castView.findViewById(R.id.castlist_refresh);
        RecyclerView listView = castView.findViewById(R.id.recycler_view);
        TextView refreshingTip = castView.findViewById(R.id.cast_refreshing_tip);
        TextView cast_helper = castView.findViewById(R.id.cast_helper);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        PlayerCoverCastAdapter castAdapter = new PlayerCoverCastAdapter(getContext(),
                LeCast.getInstance().getInfos(), -1);
        listView.setAdapter(castAdapter);
        castAdapter.setOnItemClicklistener((position, device) -> {
            castAdapter.selected = position;
            castAdapter.notifyDataSetChanged();
            setSelectPanelAnim(false);
            Bundle bundle = BundlePool.obtain();
            bundle.putInt(KEY_CASTDEVICE_INDEX, position);
            notifyReceiverEvent(DataInter.Event.EVENT_SELECT_CAST_DEVICE, bundle);
        });
        refreshCastBtn.setOnClickListener(v -> {
            refreshingTip.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(() -> {
                LeCast.getInstance().browse();//搜索
                refreshingTip.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                PlayerCoverCastAdapter castAdapter1 = new PlayerCoverCastAdapter(getContext(),
                        LeCast.getInstance().getInfos(), -1);
                listView.setAdapter(castAdapter1);
                castAdapter1.setOnItemClicklistener((position, device) -> {
                    castAdapter1.selected = position;
                    castAdapter1.notifyDataSetChanged();
                    setSelectPanelAnim(false);
                    Bundle bundle = BundlePool.obtain();
                    bundle.putInt(KEY_CASTDEVICE_INDEX, position);
                    notifyReceiverEvent(DataInter.Event.EVENT_SELECT_CAST_DEVICE, bundle);
                });
            }, 4_000);
        });
        selectPanel.addView(castView);
        setSelectPanelAnim(true);
        goneAllView();
        cast_helper.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getContext(), CastHelperActivity.class));
        });
    }

    private void setSelectPanelAnim(final boolean push) {
        selectPanel.clearAnimation();
        cancelSelectPanelAnimator();
        if (push) {
            float startP = VideoUtil.dp2px(selectPanelLayWidth);
            selectPanelAnimator = ObjectAnimator.ofFloat(selectPanel,
                    "translationX", startP, 0).setDuration(250);
        } else {
            float endP = VideoUtil.dp2px(selectPanelLayWidth);
            selectPanelAnimator = ObjectAnimator.ofFloat(selectPanel,
                    "translationX", 0, endP).setDuration(250);
        }
        selectPanelAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!push) {
                    selectPanelLay.setVisibility(GONE);
                }
            }
        });
        selectPanelAnimator.start();
    }

    private void cancelSelectPanelAnimator() {
        if (selectPanelAnimator != null) {
            selectPanelAnimator.cancel();
            selectPanelAnimator.removeAllListeners();
            selectPanelAnimator.removeAllUpdateListeners();
        }
    }

    public void pushAnim() {
        //228dp ---> 0dp
        float startP = VideoUtil.dp2px(selectPanelLayWidth);
        ViewAnimator
                .animate(selectPanel)
                .translationX(startP, 0f)
                .duration(250)
                .start();
    }

    public void popAnim() {
        //0dp ---> 228dp
        float endP = VideoUtil.dp2px(selectPanelLayWidth);
        ViewAnimator
                .animate(selectPanel)
                .translationX(0f, endP)
                .duration(250)
                .onStop(() -> selectPanelLay.setVisibility(GONE)).start();
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET:
                setSpeedText();
                updateUI(0, 0);
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
            case OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START:
            case OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE:
                mTimerUpdateProgressEnable = true;
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
        return View.inflate(context, R.layout.layout_landscape_controller_cover, null);
    }

    @Override
    public int getCoverLevel() {
        return levelLow(1);
    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if (!mGestureEnable)
            return;
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
        if (!mGestureEnable)
            return;
    }

    @Override
    public void onEndGesture() {
    }
}
