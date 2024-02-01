package  cn.yumi.daka.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import cn.junechiu.junecore.utils.FileUtil
import cn.yumi.daka.base.TTAdManagerHolder
import cn.yumi.daka.data.remote.model.*
import com.google.gson.Gson
import com.kk.taurus.playerbase.config.PlayerLibrary
import java.io.File


class ConfigCenter(val context: Context) {
    private var configurator: Configuration? = null//配置中心获取内容(广告位,清晰度)

    companion object {
        var appstate: AppStates? = null//app审核正式状态,根据版本号和渠道名称分列
        var adControl: ADControl? = null
        var union: Union? = null//穿山甲
        var mapClarity = mutableMapOf<String, ClarityModel.Clarity>()//configar中的清晰度解析后格式化
        var clarityRuleModel: ClarityRuleModel? = null
        var contactWay: ContactWay? = null
        var splashRule: SplashRule? = null//不显示开屏的规则
        var parseUrl4Station: ParseUrl4Station? = null
    }


    fun readConfig(isstr: String?, callback: () -> Unit) {
        val file = File(context.cacheDir?.path + "/configFile")
        //网络请求配置中心内容不为空
        val str = FileUtil.getAssetsFile("config")

        try {
            Log.d("configCenter readConfig", str)
            configurator = Gson().fromJson(str, Configuration::class.java)
            //配置中心替换本地清晰度规则
            parseClarity()
            //广告内容解析
            parseADs()
            //清晰度规则(时间段,视频类型)
            parseClarityRuleModel()
            //联系方式
            parseContactWay()
            //解析配置中心app的审核正式状态
            parseAppState()
            //解析南瓜需要的aes key(playerbase库中)
            parseNanGuaYingShiKey()
            //解析4大站1235来源url
            parse4StationUrl()
            callback()
        } catch (e: Exception) {
            readConfig(FileUtil.getAssetsFile("config")) {
                callback()
            }
        }
        //写入缓存文件
        if (!file.exists())
            file.createNewFile()
        file.writeText(str)
    }

    private fun parse4StationUrl() {
        val str = configurator!!.configurations.parseUrl4Station
        parseUrl4Station = Gson().fromJson(str, ParseUrl4Station::class.java)
    }

    /**
     * 解析配置中心app的审核正式状态
     */
    private fun parseAppState() {
        val str = configurator!!.configurations.appstate
        appstate = Gson().fromJson(str, AppStates::class.java)
    }

    /**
     * 解析南瓜需要的aes key(playerbase库中)
     */
    private fun parseNanGuaYingShiKey() {
        if (!configurator!!.configurations.parseNgKey.isNullOrEmpty())
            PlayerLibrary.playKey = configurator!!.configurations.parseNgKey
    }


    /**
     * 联系方式
     */
    private fun parseContactWay() {
        val contactStr = configurator!!.configurations.contactWay
        if (!TextUtils.isEmpty(configurator!!.configurations.contactWay)) {
            contactWay = Gson().fromJson(contactStr, ContactWay::class.java)
        }
    }


    /**
     * 配置中心替换本地清晰度规则
     */
    private fun parseClarity() {
        if (configurator != null && configurator?.configurations != null && configurator?.configurations?.resolution != null) {
            val clarityRule = Gson().fromJson(
                configurator?.configurations?.resolution,
                ClarityModel::class.java
            )
            if (clarityRule.rate != null && clarityRule.rate.size > 0) {
                clarityRule.rate.forEach { clarity ->
                    mapClarity[clarity.id] = clarity
                }
            }
        }
    }


    /**
     * 广告信息解析
     * adControl控制广告位选择平台
     */
    private fun parseADs() {
        val adControlStr = configurator!!.configurations.adControl
        adControl = Gson().fromJson(adControlStr, ADControl::class.java)
        val splashRuleStr = configurator?.configurations?.splashRule
        splashRule = Gson().fromJson(splashRuleStr, SplashRule::class.java)
        val unionStr = configurator!!.configurations.union
        union = Gson().fromJson(unionStr, Union::class.java)
        TTAdManagerHolder.sInit = false
    }


    /**
     * 清晰度规则(时间段,视频类型)
     */
    private fun parseClarityRuleModel() {
        clarityRuleModel = Gson().fromJson(
            configurator?.configurations?.resolutionRule,
            ClarityRuleModel::class.java
        )
    }

}