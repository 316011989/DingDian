package cn.video.star.data.remote.api

import cn.junechiu.junecore.net.retrofit.RestCreator

class ApiManager {

    private var apiService: ApiService? = null

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ApiManager()
    }


    fun getApi(): ApiService {
        if (apiService == null) {
            apiService = RestCreator.getRetrofit().create(ApiService::class.java)
        }
        return apiService!!
    }

}