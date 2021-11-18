package cn.video.star.data.remote.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class MovieType(val type: Int) : Serializable, MultiItemEntity {

    companion object {

        const val FILTER = 0

        const val MOVIE_LIST = 1
    }

    var filterlist: MutableList<TypeCate>? = null

    override fun getItemType(): Int {
        return type
    }
}