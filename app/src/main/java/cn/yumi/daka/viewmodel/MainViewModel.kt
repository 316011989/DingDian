package cn.yumi.daka.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.junechiu.junecore.net.model.BaseJson
import cn.junechiu.junecore.utils.ALogger
import cn.yumi.daka.data.remote.api.ApiManager
import cn.yumi.daka.data.remote.model.VersionResponse
import cn.yumi.daka.base.Api.Companion.RESPONSE_OK
import cn.yumi.daka.data.remote.api.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainViewModel : ViewModel() {

    private var versionData: MutableLiveData<VersionResponse> = MutableLiveData()

    fun getVersionData(): LiveData<VersionResponse> {
        return versionData
    }

    //检测更新
    fun checkVersion(version: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jk.xy11.xyz:9101")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        apiService.version().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                ALogger.d("api--", t.message)
                versionData.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {//20190717出现审核域名服务器挂了的情况,导致此接口返回404,gson解析导致崩溃
                    ALogger.d("api--", response.body())
                    val baseJson = Gson().fromJson<BaseJson<VersionResponse>>(
                        response.body(),
                        object : TypeToken<BaseJson<VersionResponse>>() {}.type
                    )
                    if (baseJson != null && baseJson.code == RESPONSE_OK) {
                        equalsVer(baseJson.data, version)
                    } else {
                        versionData.value = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 版本号对比
     */
    fun equalsVer(updateBean: VersionResponse?, version: String) {
        if (updateBean != null && !TextUtils.isEmpty(updateBean.v)) {
            val mV = Integer.parseInt(updateBean.v.replace(".", ""))
            val localV = Integer.parseInt(version.replace(".", ""))
            if (localV < mV) {
                versionData.value = updateBean //设置数据通知更新
            } else {
                versionData.value = null
            }
        } else {
            versionData.value = null
        }
    }

}
