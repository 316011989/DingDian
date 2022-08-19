package cn.yumi.daka.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.yumi.daka.R;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import java.util.List;

/**
 * Created by android on 2018/4/25.
 */

public class PlayerCoverCastAdapter extends RecyclerView.Adapter<PlayerCoverCastAdapter.TextViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private List<LelinkServiceInfo> devices;

    public int selected = -1; //选中设备

    public PlayerCoverCastAdapter(Context context, List<LelinkServiceInfo> devices, int selected) {
        this.devices = devices;
        this.selected = selected;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.player_cover_item_cast_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        LelinkServiceInfo device = devices.get(position);
        holder.mTextView.setText(device.getName());
        if (selected == position) {
            holder.mTextView.setTextColor(Color.parseColor("#FFB500"));
        } else {
            holder.mTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClicklistener != null) {
                onItemClicklistener.onItemClick(position, devices.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices == null ? 0 : devices.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        TextViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.cast_text);
        }
    }

    public OnItemClicklistener onItemClicklistener;

    public void setOnItemClicklistener(OnItemClicklistener onItemClicklistener) {
        this.onItemClicklistener = onItemClicklistener;
    }

    public interface OnItemClicklistener {
        void onItemClick(int position, LelinkServiceInfo device);
    }
}
