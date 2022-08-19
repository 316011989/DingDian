package cn.yumi.daka.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.model.TopicDetail
import cn.yumi.daka.data.remote.model.TopicVideos

class SpecialViewModel(application: Application, topicId: Int) : AndroidViewModel(application) {

    private var topicDetailData: LiveData<TopicDetail>? = null

    private var mapData: MutableLiveData<MutableMap<String, Any>> = MutableLiveData()

    private var loadVideoIndex: MutableLiveData<Int> = MutableLiveData() //页码

    private var videoListData: LiveData<TopicVideos>? = null

    private var size = 15

    init {
        topicDetailData =
                Transformations.switchMap(
                    mapData
                ) { userMap ->
                    DataRepository.instance.topicDetail(
                        (userMap?.get("id").toString().toInt()),
                        userMap?.get("token").toString()
                    )
                }

        videoListData = Transformations.switchMap(
            loadVideoIndex
        ) { videoPage -> DataRepository.instance.topicVideos(topicId, videoPage, size) }
    }

    fun setToken(data: MutableMap<String, Any>) {
        mapData.value = data
    }

    fun getTopicDetail(): LiveData<TopicDetail>? {
        return topicDetailData
    }

    fun like(json: String, head: String): LiveData<Int>? {
        return DataRepository.instance.like(json, head)
    }


    fun getVideoListData(): LiveData<TopicVideos>? {
        return videoListData
    }


    //加载下一页数据
    fun loadNextPageData() {
        if (loadVideoIndex.value == null) {
            loadVideoIndex.value = 1
        } else {
            loadVideoIndex.value = loadVideoIndex.value!! + 1
        }
    }

    class Factory(private val mApplication: Application, private val topicId: Int) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SpecialViewModel(mApplication, topicId) as T
        }
    }

}