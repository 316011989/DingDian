package cn.yumi.daka.data.remote.model

import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class MineCacheType(val type: Int) : Serializable, MultiItemEntity {

    companion object {

        const val CACHING = 0

        const val CACHED = 1
    }

    var cachingName = ""

    var cachedList: MutableList<DownloadEpisodeEntity> = mutableListOf()

    override fun getItemType(): Int {
        return type
    }
}