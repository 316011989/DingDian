package cn.video.star.data.remote.model

import cn.video.star.data.local.db.entity.MovieHistoryEntity
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class HistoryWatchType(val type: Int) : Serializable, MultiItemEntity {

    companion object {

        const val DATE = 0

        const val LIST = 1
    }

    var historyList: MutableList<MovieHistoryEntity>? = null

    var date = ""

    override fun getItemType(): Int {
        return type
    }
}