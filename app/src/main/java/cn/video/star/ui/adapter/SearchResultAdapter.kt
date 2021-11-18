package cn.video.star.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.widget.TextView
import cn.video.star.R
import cn.video.star.base.GlideApp
import cn.video.star.data.remote.model.SearchResult
import cn.video.star.data.remote.model.VideoPlayD
import cn.video.star.ui.activity.PlayerHelper
import cn.video.star.ui.activity.SearchActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class SearchResultAdapter(data: MutableList<SearchResult>, val sources: Array<String>) :
    BaseQuickAdapter<SearchResult, BaseViewHolder>(R.layout.item_search_result, data) {

    var itemCallback = fun(id: Long, position: Int) {}

    var popupDataBack = fun(id: Long, name: String, data: MutableList<VideoPlayD>) {}


    private val option = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(R.mipmap.default_cover)
        .fallback(R.mipmap.default_cover)
        .transforms(CenterCrop(), RoundedCorners(10))


    override fun convert(helper: BaseViewHolder, item: SearchResult) {
        GlideApp.with(mContext)
            .load(item.img)
            .apply(option)
            .into(helper.getView(R.id.movie_cover))

        helper.setText(R.id.movie_title, item.name)
        if (!TextUtils.isEmpty(item.sourcePlaySumText)) {
            helper.setText(R.id.playNum, "播放：" + Html.fromHtml(item.sourcePlaySumText))
        } else {
            helper.setGone(R.id.playNum, false)
        }
        if (!TextUtils.isEmpty(item.area)) {
            helper.setText(R.id.playArea, "地区：" + item.area)
        } else {
            helper.setGone(R.id.playArea, false)
        }

        if (item.detail != null) {
            if (!TextUtils.isEmpty(item.detail.actor)) {
                helper.setText(R.id.playActor, "主演：" + item.detail.actor)
            } else {
                helper.setGone(R.id.playActor, false)
            }
            if (!TextUtils.isEmpty(item.detail.tagText)) {
                helper.setText(R.id.playCate, "类型：" + item.detail.tagText)
            } else {
                helper.setGone(R.id.playCate, false)
            }
        }

        if (item.source == PlayerHelper.SOURCE_YOUKU || item.source == PlayerHelper.SOURCE_QQ || item.source == PlayerHelper.SOURCE_IQIYI ||
            item.source == PlayerHelper.SOURCE_SOHU || item.source == PlayerHelper.SOURCE_MGTV
        ) {
            helper.setText(
                R.id.playSrc, String.format(
                    mContext.getString(R.string.from_text),
                    sources[item.source]
                )
            )
        } else {
            helper.setText(
                R.id.playSrc,
                String.format(
                    mContext.getString(R.string.from_text),
                    mContext.getString(R.string.from_net)
                )
            )
        }

        if (item.plays != null && item.plays.size > 0) {
            val episodeListView = helper.getView<RecyclerView>(R.id.episodeList)
            episodeListView!!.layoutManager = LinearLayoutManager(
                mContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            if (item.plays.size > 6) {
                val adapter = SearchResultEpisodeAdapter(item.plays.subList(0, 6))
                episodeListView.adapter = adapter
                adapter.itemCallback = { position ->
                    itemCallback(item.id, position)
                }
                adapter.popupCallback = {
                    popupDataBack(item.id, item.name, item.plays)
                }
            } else {
                val adapter = SearchResultEpisodeAdapter(item.plays)
                episodeListView.adapter = adapter
                adapter.itemCallback = { position ->
                    itemCallback(item.id, position)
                }
                adapter.popupCallback = {
                    popupDataBack(item.id, item.name, item.plays)
                }
            }
        }

        helper.itemView.setOnClickListener {
            (mContext as SearchActivity).whereGo(item.adType, item.adUrl, item.id)
        }

        //播放页面
        helper.getView<TextView>(R.id.playBtn)!!.setOnClickListener {
            (mContext as SearchActivity).whereGo(item.adType, item.adUrl, item.id)
        }
    }
}