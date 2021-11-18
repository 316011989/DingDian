package  cn.video.star.data.remote.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class MineFeedType(val type: Int) : Serializable, MultiItemEntity {

    companion object {
        const val CACHE = 0//我的下载
        const val HISTORY = 1//观看历史
        const val LIKE = 2//我喜欢的
        const val COMMEND = 3//分享好友
        const val SETTING = 4//设置
        const val MOBILEDATA = 6//运营商流量播放下载
    }


    var likeList: MutableList<LikeData> = mutableListOf()

    override fun getItemType(): Int {
        return type
    }
}