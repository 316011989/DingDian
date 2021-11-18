package cn.video.star.ui.adapter

import android.text.TextUtils
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.data.remote.model.MineCacheType
import cn.video.star.ui.activity.CachingActivity
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class MineCacheAdapter(data: MutableList<MineCacheType>) :
    BaseMultiItemQuickAdapter<MineCacheType, BaseViewHolder>(data) {

    init {
        addItemType(MineCacheType.CACHING, R.layout.item_mine_caching)
        addItemType(MineCacheType.CACHED, R.layout.item_mine_cache_list)
    }

    override fun convert(helper: BaseViewHolder, item: MineCacheType) {
        when (helper.itemViewType) {
            MineCacheType.CACHING -> {
                setCachingData(helper, item)
            }
            MineCacheType.CACHED -> {
                setCachedData(helper, item)
            }
        }
    }

    private fun setCachingData(helper: BaseViewHolder, item: MineCacheType) {
        if (TextUtils.isEmpty(item.cachingName)) {
            helper.itemView.layoutParams = RelativeLayout.LayoutParams(0, 0)
        } else {
            val itemW = ScreenUtil.widthPixels / 4
            val itemH = (itemW * 1.5).toInt()
            val layoutParams = RelativeLayout.LayoutParams(itemW, itemH)
            layoutParams.leftMargin = ScreenUtil.dp2px(2f)
            helper.itemView.layoutParams = layoutParams
            helper.setText(R.id.cachingItem, item.cachingName)
        }
        //点击进入正在缓存的
        helper.itemView.setOnClickListener {
            mContext.startActivity<CachingActivity>()
        }
    }

    private fun setCachedData(helper: BaseViewHolder, item: MineCacheType) {
        val recylerview = helper.getView<RecyclerView>(R.id.cached_list_view)
        recylerview.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.HORIZONTAL, false
        )
        val adapter = MineCachedListAdapter(item.cachedList)
        recylerview.adapter = adapter
    }
}