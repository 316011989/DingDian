package cn.video.star.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "movie_collect"
)
class CollectEntity {

    var id: Long? = null

    @PrimaryKey
    var movieId: Long? = null

    var name: String? = null

    var percent = 0

    var cover: String? = null

    var selected = 0

    var datetime: String? = null

    var source = 0//来源

    var esp: String? = null//剧集

    var playIndex = 0//剧集索引

    var position: Long = 0 //播放进度

}