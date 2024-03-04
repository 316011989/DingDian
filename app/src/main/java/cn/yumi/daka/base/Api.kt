package cn.yumi.daka.base

class Api {


    companion object {
        /**
         * 接口域名
         * 例如升级 https://api.19051024.com/yumi/app/version?store=ymdy&version=1.9.0
         */
        var BASE_URL = "https://ym.yert.video/yumi/"//正式环境接口域名,请求到ip地址会替换

        const val TOOLS_HOST = "http://p.haody666.com/parse"//本地工具服务器
        const val IPLIST_URL = "${TOOLS_HOST}/lookup/domain?"//获取dns的iplist接口

        const val SHARE_BASE_URL = "https://www.xy11.xyz"//官网地址,分享使用此地址

        const val RESPONSE_OK = 1000
        const val PLAYER_ERROR_CODE = "111"

        const val TOKEN_BEARER = "Bearer "


        const val TYPE_ESP = 1
        const val TYPE_MOVIE = 2
        const val TYPE_ZY = 3
        const val TYPE_DM = 4

        const val TYPE_AD_PLAY = 0 //默认
        const val TYPE_AD_WEB = 1 //1.web页
        const val TYPE_AD_INNER = 2   //app内部跳转
        const val TYPE_AD_BROSER = 3 //系统浏览器

    }
}