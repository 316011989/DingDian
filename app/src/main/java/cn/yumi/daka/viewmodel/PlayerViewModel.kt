package cn.yumi.daka.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.*
import cn.junechiu.junecore.utils.ALogger
import cn.yumi.daka.base.Api
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.api.ApiManager
import cn.yumi.daka.data.remote.api.ApiService
import cn.yumi.daka.data.remote.model.VideoDetail
import cn.yumi.daka.data.remote.model.VideoSourcePlays
import cn.yumi.daka.data.remote.model.VideoSuggest
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DefaultObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class PlayerViewModel(videoId: Long, application: Application) :
    AndroidViewModel(application) {

    private var videoDetailData: LiveData<VideoDetail>? = null
    private var videoSourcesData: LiveData<VideoSourcePlays?>? = null
    private var videoSuggestData: LiveData<VideoSuggest>? = null

    private var videoIdData = MutableLiveData<Long>()


    init {
        videoIdData.value = videoId
        videoDetailData = Transformations.switchMap(
            videoIdData
        ) { id ->
            ALogger.d("videoDetail", "videoDetail: input: $id")
            DataRepository.instance.videoDetail(id)
        }
        videoSourcesData = Transformations.switchMap(
            videoIdData
        ) { id ->
            ALogger.d("videoSources", "videoSources: input: $id")
            DataRepository.instance.videoSourceEpisode(id)
        }
        videoSuggestData = Transformations.switchMap(
            videoIdData
        ) { id ->
            ALogger.d("VideoSuggest", "videoSuggestData: input: $id")
            DataRepository.instance.videoSuggest(id)
        }

    }

    /**
     * 顺序请求,
     */
    fun getVideoDetailAndSourcesPlays(videoId: Long) {
        // 1. 创建Retrofit实例
        // 2. 创建一个请求服务实例
        // 3. 获取服务对应的请求实例
        val videoDetailCall = ApiManager.instance.getGsonApiService().videoDetailObservable(videoId)
        val videoSourcesCall =
            ApiManager.instance.getGsonApiService().videoSourcesObservable(videoId)

        val flatMap = videoDetailCall.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io()) // 由于请求结果还需要拿来再做一次网络请求，因此我们再次放到io下线程中处理
            .flatMap(object : Function<ObservableSource<VideoDetail>>,
                io.reactivex.functions.Function<VideoDetail, ObservableSource<out VideoSourcePlays>> {
                override fun apply(t: VideoDetail): ObservableSource<out VideoSourcePlays> {
                    val videoDtail = t
                    return videoSourcesCall
                }
            }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DefaultObserver<VideoSourcePlays>() {
                override fun onNext(t: VideoSourcePlays) {
                    val sourcesPlays = t
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {
                }
            })
    }

    fun setVideoId(videoId: Long) {
        videoIdData.value = videoId
    }

    fun getVideoDetail(): LiveData<VideoDetail>? {
        return videoDetailData
    }


    fun getVideoSourcePlays(): LiveData<VideoSourcePlays?>? {
        return videoSourcesData
    }

    fun getVideoSuggest(): LiveData<VideoSuggest>? {
        return videoSuggestData
    }

    fun parseAYTM(parseUrl: String): LiveData<String> {
//        val parseAYTMCall = ApiManager.instance.getGsonApiService().parseAYTM(parseUrl)
//        parseAYTMCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : DefaultObserver<String>() {
//                override fun onNext(t: String) {
//                    val sourcesPlays = t
//                }
//
//                override fun onError(e: Throwable) {
//                    e.printStackTrace()
//                }
//
//                override fun onComplete() {
//                }
//            })

        val retrofit = Retrofit.Builder()
            .baseUrl(Api.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)


        val result = MutableLiveData<String>()
        val parseAYTMCall = apiService.parseAYTM(parseUrl)
        parseAYTMCall.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    val jo: JSONObject = JSON.parse(response.body()) as JSONObject
                    result.value = jo.getString("url")
                }
            }
        })
        return result
    }

    class Factory(
        private val videoId: Long,
        private val mApplication: Application,
    ) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerViewModel(videoId, mApplication) as T
        }
    }

}