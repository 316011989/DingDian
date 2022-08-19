package cn.yumi.daka.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.yumi.daka.R;

import java.util.List;

/**
 * Created by android on 2018/4/25.
 */
public class PlayerCoverSpeedAdapter extends RecyclerView.Adapter<PlayerCoverSpeedAdapter.TextViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private List<String> speeds;

    public int selected = 1; //选中设备 1.0X

    public PlayerCoverSpeedAdapter(Context context, List<String> speeds, int selected) {
        this.speeds = speeds;
        this.selected = selected;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.player_cover_item_speed_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        String speed = speeds.get(position);
        holder.mTextView.setText(speed.split(" ")[0]);
        if (selected == position) {
            holder.mTextView.setTextColor(Color.parseColor("#FFB500"));
        } else {
            holder.mTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClicklistener != null) {
                onItemClicklistener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return speeds == null ? 0 : speeds.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        TextViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.speed_text);
        }
    }

    public OnItemClicklistener onItemClicklistener;

    public void setOnItemClicklistener(OnItemClicklistener onItemClicklistener) {
        this.onItemClicklistener = onItemClicklistener;
    }

    public interface OnItemClicklistener {
        void onItemClick(int position);
    }
}
