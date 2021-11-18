package cn.video.star.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.video.star.R;
import cn.video.star.data.remote.model.VideoPlay;

import java.util.List;

/**
 * Created by android on 2018/4/25.
 */
public class PlayerCoverEpisodeAdapter extends RecyclerView.Adapter<PlayerCoverEpisodeAdapter.TextViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private List<VideoPlay> videoPlays;

    public int selected = 0; //选中集

    public int itemWidth = 0;

    public PlayerCoverEpisodeAdapter(Context context, List<VideoPlay> videoPlays, int selected, int itemWidth) {
        this.videoPlays = videoPlays;
        this.selected = selected;
        this.itemWidth = itemWidth;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.player_cover_item_episode_layout,
                parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        VideoPlay videoPlay = videoPlays.get(position);
        holder.mTextView.setText("" + videoPlay.getEpisode());
        if (selected == position) {
            videoPlay.isPlaying = 1;
            holder.mTextView.setTextColor(Color.parseColor("#FFB500"));
        } else {
            videoPlay.isPlaying = 0;
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
        return videoPlays == null ? 0 : videoPlays.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        TextViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.episode_text);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
            params.width = itemWidth;
            params.height = itemWidth;
            mTextView.setLayoutParams(params);
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
