package cn.junechiu.junecore.base;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by junzhao on 2018/1/31.
 * 要求框架中的每个 {@link Activity} 都需要实现此类,以满足规范
 */
public interface IActivity {

    void initPresenter();

    /**
     * 初始化 View,如果initView返回0,框架则不会调用{@link Activity#setContentView(int)}
     *
     * @param savedInstanceState
     * @return
     */
    int initView(Bundle savedInstanceState);

    /**
     * 初始化数据
     *
     * @param savedInstanceState
     */
    void initData(Bundle savedInstanceState);
}
