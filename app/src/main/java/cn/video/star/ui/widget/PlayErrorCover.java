package cn.video.star.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.kk.taurus.playerbase.config.PConst;
import com.kk.taurus.playerbase.event.BundlePool;
import com.kk.taurus.playerbase.event.EventKey;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.receiver.BaseCover;
import com.kk.taurus.playerbase.receiver.IReceiverGroup;
import com.kk.taurus.playerbase.utils.NetworkUtils;

import cn.video.star.R;
import cn.video.star.base.App;
import cn.video.star.base.DataInter;

/**
 * Created junechiu
 */
public class PlayErrorCover extends BaseCover implements View.OnClickListener {

    TextView mInfo, mRetry, netText, netTryText;
    RelativeLayout errorLayout;
    LinearLayout wifiTipLayout;

    private boolean mErrorShow;
    private int mCurrPosition;
    private int VIDEO_ERROR = 0;
    private int NET_ERROR = 1;

    final int STATUS_ERROR = -1;
    final int STATUS_UNDEFINE = 0;
    final int STATUS_MOBILE = 1;
    final int STATUS_NETWORK_ERROR = 2;

    int mStatus = STATUS_UNDEFINE;
    boolean ignoreMobileData = false;

    public PlayErrorCover(Context context) {
        super(context);
        errorLayout = findViewById(R.id.error_layout);
        wifiTipLayout = findViewById(R.id.wifi_tip_layout);
        mInfo = findViewById(R.id.error_text);
        mRetry = findViewById(R.id.retry_text);
        netTryText = findViewById(R.id.go_play);
        netText = findViewById(R.id.net_text);

        mRetry.setOnClickListener(this);
        netTryText.setOnClickListener(this);
    }

    @Override
    public void onReceiverBind() {
        super.onReceiverBind();
        getGroupValue().registerOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    @Override
    protected void onCoverAttachedToWindow() {
        super.onCoverAttachedToWindow();
    }

    @Override
    public void onReceiverUnBind() {
        super.onReceiverUnBind();
        getGroupValue().unregisterOnGroupValueUpdateListener(mOnGroupValueUpdateListener);
    }

    //处理操作
    private void handleStatus() {
        Bundle bundle = BundlePool.obtain();
        bundle.putInt(EventKey.INT_DATA, mCurrPosition);
        switch (mStatus) {
            case STATUS_ERROR:
            case STATUS_NETWORK_ERROR:
                setErrorState(false);
                requestRetry(bundle);
                break;
            case STATUS_MOBILE:
                ignoreMobileData = true; //忽略流量播放
                setErrorState(false);
                requestResume(bundle);
                break;
        }
    }

    //接受组件传过来的信息
    private IReceiverGroup.OnGroupValueUpdateListener mOnGroupValueUpdateListener =
            new IReceiverGroup.OnGroupValueUpdateListener() {
                @Override
                public String[] filterKeys() {
                    return new String[]{
                            DataInter.Key.KEY_NETWORK_RESUME};
                }

                @Override
                public void onValueUpdate(String key, Object value) {
                    //网络恢复继续播放
                    if (key.equals(DataInter.Key.KEY_NETWORK_RESUME)) {
                        Bundle bundle = BundlePool.obtain();
                        bundle.putInt(EventKey.INT_DATA, mCurrPosition);
                        setErrorState(false);
                        requestRetry(bundle);
                    }
                }
            };

    @Override
    public void onProducerData(String key, Object data) {
        super.onProducerData(key, data);
        if (DataInter.Key.KEY_NETWORK_STATE.equals(key)) {
            int networkState = (int) data;
            handleNetStatusUI(networkState);
        }
    }

    //处理网络状态错误
    private void handleNetStatusUI(int networkState) {//是否是网络资源
        if (!getGroupValue().getBoolean(DataInter.Key.KEY_NETWORK_RESOURCE, true))
            return;
        if (networkState < 0) {
            mStatus = STATUS_NETWORK_ERROR;
            switchErrorUI(NET_ERROR, getContext().getString(R.string.net_error),
                    getContext().getString(R.string.click_to_restart));
        } else {
            if (networkState == PConst.NETWORK_STATE_WIFI ||
                    SPUtils.getInstance().getBoolean("OpenMobileData") ||
                    ignoreMobileData) {//wifi不提示,流量播放下载开启状态不提示,本次播放不提示
                if (mErrorShow) {
                    setErrorState(false);
                }
            } else {
                mStatus = STATUS_MOBILE;
                switchErrorUI(NET_ERROR, getContext().getString(R.string.mobile_play_tip),
                        getContext().getString(R.string.mobile_play));
            }
        }
    }

    //切换error ui
    private void switchErrorUI(int flag, String textInfo, String textHandle) {
        setErrorState(true);
        if (flag == VIDEO_ERROR) {
            errorLayout.setVisibility(View.VISIBLE);
            wifiTipLayout.setVisibility(View.GONE);
            mInfo.setText(textInfo);
            mRetry.setText(textHandle);
        } else {
            errorLayout.setVisibility(View.GONE);
            wifiTipLayout.setVisibility(View.VISIBLE);
            netText.setText(textInfo);
            netTryText.setText(textHandle);
        }
    }

    //设置是否显示
    private void setErrorState(boolean state) {
        mErrorShow = state;
        setCoverVisibility(state ? View.VISIBLE : View.GONE);
        if (!state) {
            mStatus = STATUS_UNDEFINE;
        } else {
            notifyReceiverEvent(DataInter.Event.EVENT_CODE_ERROR_SHOW, null);
        }
        getGroupValue().putBoolean(DataInter.Key.KEY_ERROR_SHOW, state);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {
        switch (eventCode) {
            case OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET: //设置完资源判断网络
                mCurrPosition = 0;
                handleNetStatusUI(NetworkUtils.getNetworkState(getContext()));
                break;
            case OnPlayerEventListener.PLAYER_EVENT_ON_TIMER_UPDATE:
                mCurrPosition = bundle.getInt(EventKey.INT_ARG1);
                break;
        }
    }

    @Override
    public void onErrorEvent(int eventCode, Bundle bundle) {
        mStatus = STATUS_ERROR;
        if (!mErrorShow) {
            switchErrorUI(VIDEO_ERROR, getContext().getString(R.string.video_loading_faild), getContext().getString(R.string.click_to_restart));
        }
    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle) {
    }

    @Override
    public View onCreateCoverView(Context context) {
        return View.inflate(context, R.layout.layout_error_cover, null);
    }

    @Override
    public int getCoverLevel() {
        return levelHigh(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry_text:
            case R.id.go_play:
                handleStatus();
                break;
        }
    }

}
