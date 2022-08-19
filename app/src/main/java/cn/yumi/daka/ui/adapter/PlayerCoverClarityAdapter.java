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
public class PlayerCoverClarityAdapter extends RecyclerView.Adapter<PlayerCoverClarityAdapter.TextViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private List<String> clarties;

    public String selected; //选中分辨率

    public PlayerCoverClarityAdapter(Context context, List<String> clarties, String selected) {
        this.clarties = clarties;
        this.selected = selected;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.player_cover_item_speed_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        String clarity = clarties.get(position);
        String arr[] = clarity.split("&");
        holder.mTextView.setText(arr[1]);
        if (selected.equals(arr[0])) {
            holder.mTextView.setTextColor(Color.parseColor("#FF4E4D"));
        } else {
            holder.mTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClicklistener != null) {
                onItemClicklistener.onItemClick(arr[0]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clarties == null ? 0 : clarties.size();
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
        void onItemClick(String position);
    }
}
