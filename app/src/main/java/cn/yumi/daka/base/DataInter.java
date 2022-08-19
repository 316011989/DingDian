package cn.yumi.daka.base;

import com.kk.taurus.playerbase.assist.InterEvent;
import com.kk.taurus.playerbase.assist.InterKey;

public interface DataInter {

    interface Event extends InterEvent {
        int EVENT_CODE_REQUEST_CLIP_BACK = -99;

        int EVENT_CODE_REQUEST_BACK = -100;

        int EVENT_CODE_REQUEST_CLOSE = -101;

        int EVENT_CODE_REQUEST_TOGGLE_SCREEN = -104;

        int EVENT_CODE_REQUEST_CLIP_TOGGLE_SCREEN = -105;

        int EVENT_CODE_ERROR_SHOW = -111;

        int EVENT_CODE_NEXT_ESP = -112;

        int EVENT_CODE_CHANGE_SPEED = -113;

        int EVENT_CODE_CHANGE_ESP = -117;

        int EVENT_CODE_SHARE_WX = -118;

        int EVENT_CODE_SHARE_CIRCLE = -119;

        int EVENT_CODE_LIKE_VIDEO = -123;

        int EVENT_CODE_CAST_PROTRAIT = -127;//竖屏投屏事件

        int EVENT_SELECT_CAST_DEVICE = -128;//横屏投屏事件

        int EVENT_SELECT_REMOVE_CAST = -129;//移除横屏投屏事件

        int EVENT_SELECT_REMOVE_CAST_PROTRAIT = -130;//移除竖屏投屏事件

        int EVENT_SELECT_REMOVE_CAST_PROTRAIT_LIST = -131;

        int EVENT_SELECT_LOCK_SCREEN = -138; //锁屏事件

        int EVENT_CODE_PLAY_WINDOW = -139; //小窗播放

        int EVENT_CODE_CLOSE_WINDOW = -169; //关闭小窗

        int EVENT_CODE_RETURN_PLAY = -179; //全屏播放

        int EVENT_CODE_CHANGE_PIC_RATIO = -189;//画面比例

        int KEY_CHANGE_UI = -199; //改变系统ui

        int EVENT_CODE_CHANGE_CLARITY = -209;//分辨率
    }

    interface Key extends InterKey {

        String KEY_IS_LANDSCAPE = "isLandscape";

        String KEY_DATA_SOURCE = "data_source";

        String KEY_ERROR_SHOW = "error_show";

        String KEY_PLAY_SPEED = "play_speed";

        String KEY_BATTERY_PERCENT = "battery_percent";

        String KEY_PICTURE_RATIO = "picture_ratio";

        String KEY_PLAY_ESP = "play_esp";

        String KEY_VIDEO_LIKE = "video_like";

        String KEY_CASTDEVICE_INDEX = "device_index";

        String KEY_CAST_ACTION = "cast_action";

        String KEY_CHANGE_CAST = "change_cast";

        String KEY_HIDE_CAST = "hide_cast";

        String KEY_LOCK_SCREEN = "lock_screen";

        String KEY_SHOW_HIDE_UI = "change_ui";

        String KEY_PLAY_CLARITY = "play_rate";//修改分辨率

        String KEY_NETWORK_RESUME = "network_resume";

        String KEY_SHOW_LOADING = "show_loading";

        String KEY_COMPLETE_SHOW = "complete_show";

        String KEY_CONTROLLER_TOP_ENABLE = "controller_top_enable";

        String KEY_CONTROLLER_SCREEN_SWITCH_ENABLE = "screen_switch_enable";

        String KEY_TIMER_UPDATE_ENABLE = "timer_update_enable";

        String KEY_NETWORK_RESOURCE = "network_resource";

        String KEY_NAVIGATIONBARHEIGHT = "navigation_bar_height";
    }

    interface ReceiverKey {
        String KEY_LOADING_COVER = "loading_cover";
        String KEY_HALF_CONTROLLER_COVER = "half_controller_cover";
        String KEY_CONTROLLER_COVER = "controller_cover";
        String KEY_GESTURE_COVER = "gesture_cover";
        String KEY_CAST_COVER = "cast_cover";
        String KEY_CAST_COVER_PORTRAIT = "cast_cover_portrait";
        String KEY_COMPLETE_COVER = "complete_cover";
        String KEY_ERROR_COVER = "error_cover";
        String KEY_CLOSE_COVER = "close_cover";

        String KEY_WINDOW_CONTROLLER_COVER = "window_controller_cover";
    }

    interface PrivateEvent {
        int EVENT_CODE_UPDATE_SEEK = -201;
    }

}
