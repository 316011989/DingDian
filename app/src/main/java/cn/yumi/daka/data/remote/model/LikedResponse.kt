package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/8.
 */

data class LikedResponse(
        val code: Int, //1000
        val message: String, //ok
        val data: MutableList<LikeData>
) : Serializable

data class LikeData(
        val id: Long, //13677
        val type: Int, //2
        val source: Int, //2
        val name: String, //疯狂动物城
        val info: String,
        val img: String, //http://i.gtimg.cn/qqlive/images/20150608/pic_v.png
        val area: String, //美国
        val year: String, //2016-03-04
        val sourceScore: String, //9.4
        val sourceIsVip: Int, //1
        val sourcePlaySum: Long, //0
        val sourcePlaySumText: String, //1亿
        val isVip: Int, //0
        val updateText: String, //2016年度动漫电影口碑神作
        val playSum: Int, //0
        val createUserId: Int, //0
        val isTrailer: Int, //0
        val sourceOrderHot: Int, //191
        val sourceUrl: String, //https://v.qq.com/x/cover/fhe2h7sop52qzza.html
        val isHas: Int, //1
        val season: String,
        val isOver: Int, //0
        var selected: Int
) : Serializable