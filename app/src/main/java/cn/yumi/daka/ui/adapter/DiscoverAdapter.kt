package cn.yumi.daka.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.R
import cn.yumi.daka.base.GlideRequests
import cn.yumi.daka.data.remote.model.DiscoverFeed
import cn.yumi.daka.ui.activity.BillbordActivity
import cn.yumi.daka.ui.activity.MainActivity
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class DiscoverAdapter(val glide: GlideRequests, data: MutableList<DiscoverFeed>) :
    BaseQuickAdapter<DiscoverFeed, BaseViewHolder>(R.layout.item_discover, data) {

    companion object {
        val width = ScreenUtil.widthPixels

        val option =
            RequestOptions()
                .transforms(CenterCrop(), RoundedCorners(10))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.mipmap.default_cover_w)
                .fallback(R.mipmap.default_cover_w)
                .override(width,  (width / 1.7).toInt())
    }

    override fun convert(helper: BaseViewHolder?, item: DiscoverFeed?) {
        if (helper == null || item == null)
            return
        val imageView: ImageView = helper.getView(R.id.discover_item_image)
        val label1: TextView = helper.getView(R.id.discover_item_label1)

        glide.load(item.cover).apply(option).into(imageView)
        label1.text = item.title

        helper.itemView.setOnClickListener {
            (mContext as MainActivity).startActivity<BillbordActivity>(
                "id" to item.topicId, "title" to item.title
            )
            TCAgentUtil.topicClick("${item.topicId}")
        }
    }

}