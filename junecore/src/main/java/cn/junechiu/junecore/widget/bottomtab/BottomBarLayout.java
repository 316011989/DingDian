package cn.junechiu.junecore.widget.bottomtab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import cn.junechiu.junecore.R;

public class BottomBarLayout extends LinearLayout implements View.OnClickListener {

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private int normalTextColor;

    private int selectTextColor;

    private LinearLayout mLinearLayout;

    private List<TabEntity> tabEntities = new ArrayList<>();

    public BottomBarLayout(Context context) {
        super(context);
        init(context);
    }

    public BottomBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mLinearLayout = (LinearLayout) View.inflate(context, R.layout.container_layout, null);
        addView(mLinearLayout);
    }

    public void setNormalTextColor(int color) {
        this.normalTextColor = color;
    }

    public void setSelectTextColor(int color) {
        this.selectTextColor = color;
    }

    public void setTabList(List<TabEntity> tabs) {
        if (tabs == null || tabs.size() == 0) {
            return;
        }
        this.tabEntities = tabs;
        for (int i = 0; i < tabs.size(); i++) {
            View itemView = View.inflate(getContext(), R.layout.item_tab_layout, null);
            LayoutParams params = new LayoutParams(
                    ScreenUtils.getScreenWidth() / tabs.size(), LayoutParams.MATCH_PARENT);
            itemView.setLayoutParams(params);
            itemView.setId(i);
            TextView text = itemView.findViewById(R.id.tv_title);
            ImageView icon = itemView.findViewById(R.id.iv_icon);
            itemView.setOnClickListener(this);

            TabEntity itemEntity = tabEntities.get(i);
            text.setText(itemEntity.text);
            text.setTextColor(normalTextColor);
            icon.setImageResource(itemEntity.normalIconId);
            mLinearLayout.addView(itemView);
            if (i == 0) {
                showTab(0, itemView);
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (mOnItemClickListener == null) {
            return;
        } else {
            mOnItemClickListener.onItemClick(view.getId());
        }
        showTab(view.getId(), view);
    }

    public void showTab(int position, View view) {
        clearStatus();
        TextView text = view.findViewById(R.id.tv_title);
        ImageView icon = view.findViewById(R.id.iv_icon);
        text.setTextColor(selectTextColor);
        icon.setImageResource(tabEntities.get(position).selectIconId);
    }

    private void clearStatus() {
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View itemView = mLinearLayout.getChildAt(i);
            ImageView icon = itemView.findViewById(R.id.iv_icon);
            TextView text = itemView.findViewById(R.id.tv_title);
            text.setTextColor(normalTextColor);
            icon.setImageResource(tabEntities.get(i).normalIconId);
        }
    }
}
