package cn.yumi.daka.ui.adapter

import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.local.db.entity.MovieHistoryEntity
import cn.yumi.daka.ui.activity.PlayerWindowActivity
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity
import java.lang.ref.WeakReference

class HistoryAdapter(data: MutableList<MovieHistoryEntity>) :
    BaseQuickAdapter<MovieHistoryEntity, BaseViewHolder>(R.layout.item_history_item, data) {

    companion object {
        private val option = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .transforms(CenterCrop(), RoundedCorners(10))
    }

    var delCallback = fun(_: MovieHistoryEntity) {}

    //点选item回调
    var clickItem = fun() {}

    var isEdit = false

    override fun convert(helper: BaseViewHolder, item: MovieHistoryEntity) {
        val coverImg = helper.getView<ImageView>(R.id.movie_cover)
        val imageViewWeakReference = WeakReference(coverImg)
            GlideApp.with(mContext).asDrawable()
                .load(item.cover)
                .apply(option)
                .into(imageViewWeakReference.get()!!)
        helper.setText(R.id.movie_title, "${item.esp}")
        if (isEdit) {
            helper.setGone(R.id.select_img, true)
        } else {
            helper.setGone(R.id.select_img, false)
        }

        helper.getView<ProgressBar>(R.id.watch_progress).progress = item.percent
        helper.getView<TextView>(R.id.watch_text).text = "已看到" + item.percent + "%"

        if (item.selected == 1) {
            helper.setImageResource(R.id.select_img, R.mipmap.history_selected_icon)
        } else {
            helper.setImageResource(R.id.select_img, R.mipmap.history_select_icon)
        }

        helper.itemView.setOnClickListener {
            if (isEdit) {
                item.selected = if (item.selected == 1) 0 else 1
                if (item.selected == 1) {
                    helper.setImageResource(R.id.select_img, R.mipmap.history_selected_icon)
                } else {
                    helper.setImageResource(R.id.select_img, R.mipmap.history_select_icon)
                }
                clickItem()
            } else {
                mContext.startActivity<PlayerWindowActivity>("id" to item.movidId)
                TCAgentUtil.videoClick("${item.movidId}", "", "historyDetail")
            }
        }

        //长按删除
        helper.itemView.setOnLongClickListener {
            delCallback(item)
            true
        }
    }
}