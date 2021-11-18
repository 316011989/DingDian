package cn.video.star.data.remote.model

import java.io.Serializable

data class VersionResponse(

    val content: String,//更新文案

    val forceupdate: String,//"1"

    val MD5: String,//MD5值

    val title: String,//标题 "App更新了"

    val url: String,//下载地址

    val version: String,//版本号,升级前还是待升级的不清楚

    val createtime: String,//发布时间

    val k: String,//是否强制升级,1强制,0不强制

    val v: String,//强制升级文案,点击去官网

    val m: String//待升级的版本号
) : Serializable