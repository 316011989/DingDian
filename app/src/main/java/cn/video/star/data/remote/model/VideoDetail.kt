package cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/3.
 */

data class VideoDetail(
    val code: Int, //1000
    val message: String, //ok
    val data: VideoData
) : Serializable

data class VideoData(
    val id: Long, //14168
    val type: Int, //2 电影 1剧集
    val source: Int, //2
    val name: String, //雷神3：诸神黄昏
    val info: String,
    val img: String, //http://i.gtimg.cn/qqlive/images/20150608/pic_v.png
    val area: String,
    val year: String,
    val sourceScore: String, //8.6
    val sourceIsVip: Int, //1
    val sourcePlaySum: Long, //0
    val sourcePlaySumText: String, //7447万
    val isVip: Int, //0
    val updateText: String, //雷神抖森绿巨人全员回归
    val playSum: Int, //0
    val isTrailer: Int, //0
    val sourceOrderHot: Int, //131
    val sourceUrl: String, //https://v.qq.com/x/cover/9ikkuz4o8ejavdc.html
    val isHas: Int, //1
    val season: String,
    val isOver: Int, //0
    var plays: MutableList<VideoPlay>?,
    val detail: Detail,
    val esKws: String,  //"如懿传 后宫·如懿传 / 甄嬛传续集 / Ruyi's Royal Love in the Palace 汪俊 周迅 / 霍建华 / 张钧甯 / 李纯 / 董洁 /"
    val esTags: String, //"剧情 / 历史 / 古装 热播／剧情 / 历史 / 古装"
    var rate: String//清晰度选项
) : Serializable

data class Detail(
    val director: String,
    val actor: String, //克里斯·海姆斯沃斯/凯特·布兰切特/汤姆·希德勒斯顿/马克·鲁法洛/泰莎·汤普森/杰夫·高布伦/伊德瑞斯·艾尔巴/卡尔·厄本/安东尼·霍普金斯
    val host: String,
    val guest: String,
    val episodeNum: Int, //1
    val episodeText: String,
    val summary: String, //雷神失去了他强大的锤子，并被囚禁在宇宙的另一端。而他发现自己必须与时间赛跑才能赶回仙宫阻止诸神黄昏！家乡的毁灭以及仙宫文明的终结掌握在一个全新的超强劲敌手里——她就是无情的死神海拉！然而他必须要在一场角斗赛中获胜才行，而他的对手将是昔日复联伙伴——无敌浩克！
    val superId: Int, //0
    val tagText: String, //动作 奇幻 冒险 院线
    val actorText: String
) : Serializable