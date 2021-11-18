package  cn.video.star.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import  cn.video.star.R;

import java.util.List;

/**
 * Created by android on 2018/4/25.
 */
public class PlayerCoverPicRatioAdapter extends RecyclerView.Adapter<PlayerCoverPicRatioAdapter.TextViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private List<String> ratios;

    public int selected = 0; //选中设备 1.0X

    public PlayerCoverPicRatioAdapter(Context context, List<String> ratios, int selected) {
        this.ratios = ratios;
        this.selected = selected;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.player_cover_item_speed_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        String ratio = ratios.get(position);
        holder.mTextView.setText(ratio);
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
        return ratios == null ? 0 : ratios.size();
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
