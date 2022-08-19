package cn.yumi.daka.ui.adapter

import android.view.View.GONE
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yumi.daka.R
import cn.yumi.daka.data.local.db.entity.MovieHistoryEntity
import cn.yumi.daka.data.remote.model.MineCacheType
import cn.yumi.daka.data.remote.model.MineFeedType
import com.blankj.utilcode.util.SPUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Created by android on 2018/4/19.
 */
class MineFeedAdapter(data: MutableList<MineFeedType>) :
    BaseMultiItemQuickAdapter<MineFeedType, BaseViewHolder>(data) {

    private var cacheAdapter = MineCacheAdapter(mutableListOf())
    private val historyAdapter = MineHistoryAdapter(mutableListOf())

    init {
        addItemType(MineFeedType.HISTORY, R.layout.item_mine_history)
        addItemType(MineFeedType.LIKE, R.layout.item_mine_like)
        addItemType(MineFeedType.CACHE, R.layout.item_mine_cache)
        addItemType(MineFeedType.COMMEND, R.layout.item_mine_history)
        addItemType(MineFeedType.SETTING, R.layout.item_mine_history)
        addItemType(MineFeedType.MOBILEDATA, R.layout.item_mine_mobiledata)
    }

    var itemCallback = fun(_: Int, _: Int) {}

    var moreClickCallback = fun(_: Int) {}

    override fun convert(helper: BaseViewHolder, item: MineFeedType) {
        when (helper.itemViewType) {
            MineFeedType.HISTORY -> {
                setHistoryData(helper)
            }
            MineFeedType.CACHE -> {
                setCacheData(helper)
            }
            MineFeedType.LIKE -> {
                setLikeData(helper, item)
            }
            MineFeedType.COMMEND, MineFeedType.SETTING -> {
                toOtherPage(helper, helper.itemViewType)
            }
            MineFeedType.MOBILEDATA -> {
                setAccessMobileData(helper)
            }
        }
    }


    private fun setHistoryData(helper: BaseViewHolder) {
        val recyclerView = helper.getView<RecyclerView>(R.id.historyListView)
        recyclerView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.HORIZONTAL, false
        )
        recyclerView.adapter = historyAdapter
        helper.getView<RelativeLayout>(R.id.history_list_layout).setOnClickListener {
            moreClickCallback(MineFeedType.HISTORY)
        }
    }

    private fun setCacheData(helper: BaseViewHolder) {
        val recyclerView = helper.getView<RecyclerView>(R.id.cacheListView)
        if (recyclerView.tag != "1") {
            recyclerView.layoutManager = LinearLayoutManager(
                mContext,
                LinearLayoutManager.HORIZONTAL, false
            )
            recyclerView.adapter = cacheAdapter
            recyclerView.tag = "1"
        }
        helper.getView<RelativeLayout>(R.id.cache_list_layout).setOnClickListener {
            moreClickCallback(MineFeedType.CACHE)
        }
    }

    private fun setLikeData(helper: BaseViewHolder, item: MineFeedType) {
        val recyclerView = helper.getView<RecyclerView>(R.id.likeListView)
        recyclerView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.HORIZONTAL, false
        )
        if (item.likeList != null && item.likeList.size > 0) {
            val adapter = MineLikeAdapter(item.likeList)
            recyclerView.adapter = adapter
        }

        helper.getView<RelativeLayout>(R.id.like_list_layout).setOnClickListener {
            moreClickCallback(MineFeedType.LIKE)
        }
    }

    /**
     * 跳转页面
     */
    private fun toOtherPage(helper: BaseViewHolder, type: Int) {
        helper.getView<RecyclerView>(R.id.historyListView).visibility = GONE
        val tv = helper.getView<TextView>(R.id.history_list_title)
        when (type) {
            MineFeedType.COMMEND -> {
                tv.text = "分享好友"
            }
            MineFeedType.SETTING -> {
                tv.text = "设置"
            }
//            MineFeedType.PROFILE -> {
//                tv.text = "个人信息"
//            }
        }
        helper.getView<RelativeLayout>(R.id.history_list_layout).setOnClickListener {
            moreClickCallback(type)
        }
    }

    /**
     * 点击允许或不允许使用流量播放和下载
     */
    private fun setAccessMobileData(helper: BaseViewHolder) {
        val selector = helper.getView<CheckBox>(R.id.mine_item_mobiledata)
        selector.isChecked = SPUtils.getInstance().getBoolean("OpenMobileData")
        selector.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.getInstance().put("OpenMobileData", isChecked)
        }
    }


    //添加缓存数据
    fun addCacheData(data: MutableList<MineCacheType>) {
        cacheAdapter.data.clear()
        cacheAdapter.addData(data)
        cacheAdapter.notifyDataSetChanged()
    }

    fun addHistoryData(data: MutableList<MovieHistoryEntity>) {
        historyAdapter.data.clear()
        historyAdapter.addData(data)
        historyAdapter.notifyDataSetChanged()
    }
}