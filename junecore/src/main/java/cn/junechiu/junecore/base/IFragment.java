package cn.junechiu.junecore.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

/**
 * Created by junzhao on 2018/1/31.
 * 要求框架中的每个 {@link Fragment} 都需要实现此类,以满足规范
 */
public interface IFragment {

//    void initPresenter();

    /**
     * 初始化 View
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 初始化数据
     *
     * @param savedInstanceState
     */
    void initData(Bundle savedInstanceState);


    void setData(Object data);
}
