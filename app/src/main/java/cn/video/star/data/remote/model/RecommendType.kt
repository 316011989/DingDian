package cn.video.star.data.remote.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class RecommendType(val type: Int) : Serializable, MultiItemEntity {

    companion object {

        const val BANNER = 0

        const val FEED = 1
    }

    var adList: MutableList<BannerInfo> = mutableListOf()
    var feedList: MutableList<Topic> = mutableListOf()
    override fun getItemType(): Int {
        return type
    }
}