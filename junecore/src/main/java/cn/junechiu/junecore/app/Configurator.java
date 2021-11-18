package cn.junechiu.junecore.app;

import android.app.Activity;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import okhttp3.Interceptor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * 静态内部类单利模式
 */
public class Configurator {

    private static final WeakHashMap<Object, Object> JUNE_CONFIGS = new WeakHashMap<>();

    private static final Handler HANDLER = new Handler();

    private static final ArrayList<Interceptor> INTERCEPTORS = new ArrayList<>();

    private Configurator() {
        JUNE_CONFIGS.put(ConfigKeys.CONFIG_READY, false);//未初始化完成
    }

    public static Configurator getInstance() {
        return Holder.INSTANCE;
    }

    public static class Holder {
        public static final Configurator INSTANCE = new Configurator();
    }

    public static WeakHashMap<Object, Object> getJuneConfigs() {
        return JUNE_CONFIGS;
    }

    public final void configure() {
        Logger.addLogAdapter(new AndroidLogAdapter());
        JUNE_CONFIGS.put(ConfigKeys.HANDLER, HANDLER);//添加handler
        JUNE_CONFIGS.put(ConfigKeys.CONFIG_READY, true);//初始化完成
    }

    public final Configurator withApiHost(String host) {
        JUNE_CONFIGS.put(ConfigKeys.API_HOST, host);
        return this;
    }

    public final Configurator withInterceptor(Interceptor interceptor) {
        INTERCEPTORS.add(interceptor);
        JUNE_CONFIGS.put(ConfigKeys.INTERCEPTOR, INTERCEPTORS);
        return this;
    }

    public final Configurator withDNSs(List<InetAddress> dnSs) {
        JUNE_CONFIGS.put(ConfigKeys.DNSS, dnSs);
        return this;
    }

    public final Configurator withInterceptors(ArrayList<Interceptor> interceptors) {
        INTERCEPTORS.addAll(interceptors);
        JUNE_CONFIGS.put(ConfigKeys.INTERCEPTOR, INTERCEPTORS);
        return this;
    }

    public final Configurator withWeChatAppId(String appId) {
        JUNE_CONFIGS.put(ConfigKeys.WE_CHAT_APP_ID, appId);
        return this;
    }

    public final Configurator withWeChatAppSecret(String appSecret) {
        JUNE_CONFIGS.put(ConfigKeys.WE_CHAT_APP_SECRET, appSecret);
        return this;
    }

    public final Configurator withActivity(Activity activity) {
        JUNE_CONFIGS.put(ConfigKeys.ACTIVITY, activity);
        return this;
    }

    public Configurator withJavascriptInterface(@NonNull String name) {
        JUNE_CONFIGS.put(ConfigKeys.JAVASCRIPT_INTERFACE, name);
        return this;
    }

    //检查是否配置完成
    private void checkConfigurations() {
        final boolean isReady = (boolean) JUNE_CONFIGS.get(ConfigKeys.CONFIG_READY);
        if (!isReady) {
            throw new RuntimeException("Configuration is not ready,call configure method");
        }
    }

    //获取配置项
    final <T> T getConfiguration(Object key) {
        checkConfigurations();
        return (T) JUNE_CONFIGS.get(key);
    }
}
