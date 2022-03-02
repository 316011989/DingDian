package cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.junechiu.junecore.utils.ALogger
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.*

class PlayerViewModel(videoId: Long, application: Application) :
    AndroidViewModel(application) {

    private var videoDetailData: LiveData<VideoDetail>? = null
    private var videoSourcesData: LiveData<VideoSourcePlays?>? = null
    private var videoSuggestData: LiveData<VideoSuggest>? = null

    private var videoIdData = MutableLiveData<Long>()

    private var jsonUrlData = MutableLiveData<String>()
    private var videoResolveData: LiveData<VideoPlayData>? = null

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