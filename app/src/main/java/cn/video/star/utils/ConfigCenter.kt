package  cn.video.star.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import cn.junechiu.junecore.utils.FileUtil
import  cn.video.star.data.remote.model.*
import com.google.gson.Gson
import java.io.File


class ConfigCenter(val context: Context) {
    private var configurator: Configuration? = null//配置中心获取内容(广告位,清晰度)

    companion object {
        var mapClarity = mutableMapOf<String, ClarityModel.Clarity>()//configar中的清晰度解析后格式化
        var clarityRuleModel: ClarityRuleModel? = null
        var contactWay: ContactWay? = null
        var splashRule: SplashRule? = null//不显示开屏的规则
        var appstate: AppStates? = null//app审核正式状态,根据版本号和渠道名称分列
        var parseNgKey: String? = null
    }


    fun readConfig( callback: () -> Unit) {
        val file = File(context.cacheDir?.path + "/configFile")
        //网络请求配置中心内容不为空
        val str: String = FileUtil.getAssetsFile("config")

        //写入缓存文件
        if (!file.exists())
            file.createNewFile()
        file.writeText(str)


//        val str: String = isstr ?: FileUtil.getAssetsFile("config")
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
        parseNgKey = configurator!!.configurations.parseNgKey
        callback()
    }

    /**
     * 解析配置中心app的审核正式状态
     */
    private fun parseAppState() {
        val str = configurator!!.configurations.appstate
        appstate = Gson().fromJson(str, AppStates::class.java)
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
     * //配置中心替换本地清晰度规则
     */
    private fun parseClarity() {
        if (configurator != null && configurator?.configurations != null && configurator?.configurations?.resolutionRule != null) {
            val cm = EncreptUtil.decrypt(
                configurator?.configurations?.resolution,
                EncreptUtil.ENCREPT_PW
            )
            val clarityRule = Gson().fromJson(cm, ClarityModel::class.java)
            if (clarityRule.rate != null && clarityRule.rate.size > 0) {
                clarityRule.rate.forEach { clarity ->
                    mapClarity[clarity.id] = clarity
                }
            }
        }
    }


    /**
     * 广告信息解析
     */
    private fun parseADs() {
        splashRule = Gson().fromJson(
            configurator?.configurations?.splashRule,
            SplashRule::class.java
        )
    }



    /**
     * 清晰度规则(时间段,视频类型)
     */
    private fun parseClarityRuleModel() {
        val crString =
            EncreptUtil.decrypt(
                configurator?.configurations?.resolutionRule,
                EncreptUtil.ENCREPT_PW
            )
        clarityRuleModel = Gson().fromJson(crString, ClarityRuleModel::class.java)
    }

}