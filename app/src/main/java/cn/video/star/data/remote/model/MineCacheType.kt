package cn.video.star.data.remote.model

import cn.video.star.data.local.db.entity.DownloadEpisodeEntity
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

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