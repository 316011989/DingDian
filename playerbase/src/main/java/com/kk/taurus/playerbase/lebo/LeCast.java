package com.kk.taurus.playerbase.lebo;

import static com.kk.taurus.playerbase.config.AppContextAttach.getApplicationContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.hpplay.sdk.source.api.IBindSdkListener;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.LelinkPlayerInfo;
import com.hpplay.sdk.source.api.LelinkSourceSDK;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import java.util.List;

public class LeCast {

    private static final String TAG = "LeCast";
    private static final String APPKEY = "15694";
    private static final String APPSECRET = "ae45d4bc6e13bf8a53dede5b332f2708";
    private boolean isMirror;

    private Context mContext;
    private LelinkSourceSDK mLelinkServiceManager;
    private List<LelinkServiceInfo> mInfos;
    private UIHandler mUIHandler;

    private IConnectListener mActivityConnectListener;

    public int status = IUIUpdateListener.STATE_STOP;

    public static final class Holder {
        public static final LeCast INSTANCE = new LeCast();
    }

    public static final LeCast getInstance() {
        return Holder.INSTANCE;
    }

    public void initLeCast(Context pContext) {
        this.mContext = pContext;
        mUIHandler = new UIHandler(Looper.getMainLooper());

        mLelinkServiceManager = LelinkSourceSDK.getInstance();
        mLelinkServiceManager.setBindSdkListener(mBindSdkListener)
                .setSdkInitInfo(getApplicationContext(), APPKEY, APPSECRET)
                .bindSdk();
        mLelinkServiceManager.setBrowseResultListener(mBrowseListener);
        mLelinkServiceManager.setConnectListener(mConnectListener);
        mLelinkServiceManager.setPlayListener(mLelinkPlayerListener);
    }

    IBindSdkListener mBindSdkListener = success -> {
        //
        Log.d(TAG, "lecast bind success");
    };


    public void setUIUpdateListener(IUIUpdateListener pUIUpdateListener) {
        mUIHandler.setUIUpdateListener(pUIUpdateListener);
    }

    public List<LelinkServiceInfo> getInfos() {
        return mInfos;
    }


    //开始搜索设备 type: All/Lelink/DLNA
    public void browse() {
        mLelinkServiceManager.startBrowse();
    }

    //停止搜索
    public void stopBrowse() {
        mLelinkServiceManager.stopBrowse();
    }


    public void playLocalMedia(String url, int type, String screenCode) {
        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
        lelinkPlayerInfo.setType(type);
        lelinkPlayerInfo.setLocalPath(url);
        mLelinkServiceManager.startPlayMedia(lelinkPlayerInfo);
    }

    public void playNetMedia(String headerJson, String url, int type) {
        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
        lelinkPlayerInfo.setType(type);
        if (!TextUtils.isEmpty(headerJson)) {
            lelinkPlayerInfo.setHeader(headerJson);
        }
        lelinkPlayerInfo.setUrl(url);
        lelinkPlayerInfo.setLoopMode(LelinkPlayerInfo.LOOP_MODE_SINGLE);
        mLelinkServiceManager.startPlayMedia(lelinkPlayerInfo);
    }

    public void pause() {
        mLelinkServiceManager.pause();
    }

    public void resume() {
        mLelinkServiceManager.resume();
    }

    public void stop() {
        mLelinkServiceManager.stopPlay();
    }

    public void seekTo(int pPosition) {
        mLelinkServiceManager.seekTo(pPosition);
    }

    public void voulumeUp() {
        mLelinkServiceManager.addVolume();
    }

    public void voulumeDown() {
        mLelinkServiceManager.subVolume();
    }

    //链接设备
    public void connect(LelinkServiceInfo pInfo) {
        mLelinkServiceManager.connect(pInfo);
    }

    //断开链接
    public void disConnect(LelinkServiceInfo pInfo) {
        mLelinkServiceManager.disConnect(pInfo);
    }


    public void startMirror(LelinkServiceInfo lelinkServiceInfo,
                            int resolutionLevel, int bitrateLevel, boolean isAudioEnnable, String screenCode) {
        if (mLelinkServiceManager != null) {
            isMirror = true;
            LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
            lelinkPlayerInfo.setType(LelinkPlayerInfo.TYPE_MIRROR);
//            lelinkPlayerInfo.setActivity(pActivity);
//            lelinkPlayerInfo.setOption(IAPI.OPTION_6, screenCode);
            lelinkPlayerInfo.setLelinkServiceInfo(lelinkServiceInfo);
            lelinkPlayerInfo.setMirrorAudioEnable(isAudioEnnable);
            lelinkPlayerInfo.setResolutionLevel(resolutionLevel);
            lelinkPlayerInfo.setBitRateLevel(bitrateLevel);
            mLelinkServiceManager.startMirror(lelinkPlayerInfo);
        }
    }

    public void stopMirror() {
        if (mLelinkServiceManager != null) {
            isMirror = false;
            mLelinkServiceManager.stopPlay();
        }
    }

    //释放资源
    public void release() {
        if (isMirror && mLelinkServiceManager != null) {
            mLelinkServiceManager.stopPlay();
            mLelinkServiceManager.unBindSdk();
        }
    }

    private Message buildTextMessage(String text) {
        Message message = Message.obtain();
        message.what = UIHandler.MSG_TEXT;
        message.obj = text;
        return message;
    }

    private Message buildStateMessage(int state) {
        return buildStateMessage(state, null);
    }

    private Message buildStateMessage(int state, Object object) {
        Message message = Message.obtain();
        message.what = UIHandler.MSG_STATE;
        message.arg1 = state;
        if (null != object) {
            message.obj = object;
        }
        return message;
    }

    private IBrowseListener mBrowseListener = new IBrowseListener() {
        @Override
        public void onBrowse(int resultCode, List<LelinkServiceInfo> list) {
            Log.d(TAG, "onSuccess size:" + (list == null ? 0 : list.size()));
            mInfos = list;
            if (resultCode == IBrowseListener.BROWSE_SUCCESS) {
                Log.d(TAG, "browse success");
                StringBuffer buffer = new StringBuffer();
                if (null != mInfos) {
                    for (LelinkServiceInfo info : mInfos) {
                        buffer.append("name：").append(info.getName())
                                .append(" uid: ").append(info.getUid())
                                .append(" type:").append(info.getTypes()).append("\n");
                    }
                    buffer.append("---------------------------\n");
                    if (null != mUIHandler) { // 发送文本信息
                        mUIHandler.sendMessage(buildTextMessage(buffer.toString()));
                        if (mInfos.isEmpty()) {
                            mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_SEARCH_NO_RESULT));
                        } else {
                            mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_SEARCH_SUCCESS));
                        }
                    }
                }
            } else {
                if (null != mUIHandler) { // 发送文本信息
                    Log.d(TAG, "browse error:Auth error");
                    mUIHandler.sendMessage(buildTextMessage("搜索错误：Auth错误"));
                    mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_SEARCH_ERROR));
                }
            }
        }
    };

    private IConnectListener mConnectListener = new IConnectListener() {
        @Override
        public void onConnect(LelinkServiceInfo serviceInfo, int extra) {
            Log.d(TAG, "onConnect:" + serviceInfo.getName());
            if (null != mUIHandler) {
                String type = extra == TYPE_LELINK ? "Lelink" : extra == TYPE_DLNA ? "DLNA" : extra == TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                String text;
                if (TextUtils.isEmpty(serviceInfo.getName())) {
                    text = "pin码连接" + type + "成功";
                } else {
                    text = serviceInfo.getName() + "连接" + type + "成功";
                }
                mUIHandler.sendMessage(buildTextMessage(text));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_CONNECT_SUCCESS, text));
            }
            if (mActivityConnectListener != null) {
                mActivityConnectListener.onConnect(serviceInfo, extra);
            }
        }

        @Override
        public void onDisconnect(LelinkServiceInfo serviceInfo, int what, int extra) {
            Log.d(TAG, "onDisconnect:" + serviceInfo.getName() + " disConnectType:" + what + " extra:" + extra);
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {
                if (null != mUIHandler) {
                    String text;
                    if (TextUtils.isEmpty(serviceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = serviceInfo.getName() + "连接断开";
                    }
                    mUIHandler.sendMessage(buildTextMessage(text));
                    mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_DISCONNECT, text));
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                String text = null;
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = serviceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_WAITTING) {
                    text = serviceInfo.getName() + "等待确认";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_REJECT) {
                    text = serviceInfo.getName() + "连接拒绝";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_TIMEOUT) {
                    text = serviceInfo.getName() + "连接超时";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_BLACKLIST) {
                    text = serviceInfo.getName() + "连接黑名单";
                }
                if (null != mUIHandler) {
                    mUIHandler.sendMessage(buildTextMessage(text));
                    mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_CONNECT_FAILURE, text));
                }
            }
            if (mActivityConnectListener != null) {
                mActivityConnectListener.onDisconnect(serviceInfo, what, extra);
            }
        }
    };

    private ILelinkPlayerListener mLelinkPlayerListener = new ILelinkPlayerListener() {
        @Override
        public void onLoading() {
            status = IUIUpdateListener.STATE_LOADING;
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildTextMessage("开始加载"));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_LOADING));
            }
        }

        @Override
        public void onStart() {
            Log.d(TAG, "onStart:");
            status = IUIUpdateListener.STATE_PLAY;
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildTextMessage("开始播放"));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_PLAY));
            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause");
            status = IUIUpdateListener.STATE_PAUSE;
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildTextMessage("暂停播放"));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_PAUSE));
            }
        }

        @Override
        public void onCompletion() {
            Log.d(TAG, "onCompletion");
            status = IUIUpdateListener.STATE_COMPLETION;
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildTextMessage("播放完成"));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_COMPLETION));
            }
        }

        @Override
        public void onStop() {
            Log.d(TAG, "onStop");
            status = IUIUpdateListener.STATE_STOP;
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildTextMessage("播放结束"));
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_STOP));
            }
        }

        @Override
        public void onSeekComplete(int pPosition) {
            Log.d(TAG, "onSeekComplete position:" + pPosition);
            status = IUIUpdateListener.STATE_SEEK;
            mUIHandler.sendMessage(buildTextMessage("设置进度"));
            mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_SEEK));
        }

        @Override
        public void onInfo(int i, int i1) {

        }

        @Override
        public void onInfo(int i, String s) {

        }

//        @Override
//        public void onInfo(int i, String s) {
//
//        }


        @Override
        public void onError(int what, int extra) {
            Log.d(TAG, "onError what:" + what + " extra:" + extra);
            status = IUIUpdateListener.STATE_PLAY_ERROR;
            String text = null;
            if (what == ILelinkPlayerListener.PUSH_ERROR_INIT) {
                if (extra == ILelinkPlayerListener.PUSH_ERRROR_FILE_NOT_EXISTED) {
                    text = "文件不存在";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_OFFLINE) {
                    text = "IM TV不在线";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IMAGE) {

                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_UNSUPPORTED_MIMETYPE) {
                    text = "IM不支持的媒体类型";
                } else {
                    text = "未知";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_INIT) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_UNSUPPORTED) {
                    text = "不支持镜像";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_REJECT_PERMISSION) {
                    text = "镜像权限拒绝";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_DEVICE_UNSUPPORTED) {
                    text = "设备不支持镜像";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_PREPARE) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_INFO) {
                    text = "获取镜像信息出错";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_PORT) {
                    text = "获取镜像端口出错";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PLAY) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "播放无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_STOP) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "退出播放无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PAUSE) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "暂停无响应";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_RESUME) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "恢复无响应";
                }
            }
            mUIHandler.sendMessage(buildTextMessage(text));
            mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_PLAY_ERROR, text));
        }

        /**
         * 音量变化回调
         *
         * @param percent 当前音量
         */
        @Override
        public void onVolumeChanged(float percent) {
            Log.d(TAG, "onVolumeChanged percent:" + percent);
        }

        /**
         * 进度更新回调
         *
         * @param duration 媒体资源总长度
         * @param position 当前进度
         */
        @Override
        public void onPositionUpdate(long duration, long position) {
            Log.d(TAG, "onPositionUpdate duration:" + duration + " position:" + position);
            long[] arr = new long[]{duration, position};
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildStateMessage(IUIUpdateListener.STATE_POSITION_UPDATE, arr));
            }
        }

    };

    private static class UIHandler extends Handler {
        private static final int MSG_TEXT = 1;
        private static final int MSG_STATE = 2;
        private IUIUpdateListener mUIUpdateListener;

        private UIHandler(Looper pMainLooper) {
            super(pMainLooper);
        }

        private void setUIUpdateListener(IUIUpdateListener pUIUpdateListener) {
            mUIUpdateListener = pUIUpdateListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TEXT:
                    String text = (String) msg.obj;
                    if (null != mUIUpdateListener) {
                        mUIUpdateListener.onUpdateText(text);
                    }
                    break;
                case MSG_STATE:
                    int state = msg.arg1;
                    Object obj = msg.obj;
                    if (null != mUIUpdateListener) {
                        mUIUpdateListener.onUpdateState(state, obj);
                    }
                    break;
            }
        }
    }


    public String getWifiIp() {
        if (isWifiConnected()) {
            try {
                NetworkInfo info = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo infowifi = wifiManager.getConnectionInfo();
                    String ip = intIP2StringIP(infowifi.getIpAddress());
                    return ip;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "127.0.0.1";
            }
        } else {
            return "127.0.0.1";
        }
        return "127.0.0.1";
    }

    public String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
