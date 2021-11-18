package cn.junechiu.junecore.widget.alertdialog;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by android on 2018/10/22.
 */
public class CustomClickUrlSpan extends ClickableSpan {

    private String url;

    private OnLinkClickListener mListener;

    public CustomClickUrlSpan(String url, OnLinkClickListener listener) {
        this.url = url;
        this.mListener = listener;
    }

    @Override
    public void onClick(View widget) {
        if (mListener != null) {
            mListener.onLinkClick(widget);
        }
    }

    /**
     * 跳转链接接口
     */
    public interface OnLinkClickListener {
        void onLinkClick(View view);
    }
}
