package cn.junechiu.junecore.app;

import android.content.Context;
import android.os.Handler;
import cn.junechiu.junecore.utils.ACache;
import cn.junechiu.junecore.utils.FileUtil;
import cn.junechiu.junecore.utils.ScreenUtil;
import com.blankj.utilcode.util.Utils;

import java.util.WeakHashMap;

/**
 * Created by junzhao on 2017/12/2.
 */
public final class June {

    //Context context 为application
    public static Configurator init(Context context) {
        getConfigurations().put(ConfigKeys.APPLICATION_CONTEXT.name(), context.getApplicationContext());
        Utils.init(context);
        ScreenUtil.init(context);
        FileUtil.init(context);
        ACache.init(context);
        return Configurator.getInstance();
    }

    public static WeakHashMap<Object, Object> getConfigurations() {
        return Configurator.getInstance().getJuneConfigs();
    }

    public static Configurator getConfigurator() {
        return Configurator.getInstance();
    }

    public static <T> T getConfigration(Object key) {
        return getConfigurator().getConfiguration(key);
    }

    public static Handler getHandler() {
        return getConfigration(ConfigKeys.HANDLER);
    }

    public static Context getApplication() {
        return (Context) getConfigurations().get(ConfigKeys.APPLICATION_CONTEXT.name());
    }

}
