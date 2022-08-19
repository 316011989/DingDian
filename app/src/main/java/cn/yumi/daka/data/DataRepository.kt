package cn.yumi.daka.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.yumi.daka.data.local.db.AppDatabaseManager
import cn.yumi.daka.data.remote.RemoteDataSource
import cn.yumi.daka.data.remote.model.*
import com.blankj.utilcode.util.NetworkUtils
import java.util.*

/**
 * DataRepository.kt
 * 数据源操作---本地和网络数据
 */
class DataRepository {

    lateinit var mRemoteDataSource: RemoteDataSource//远程数据源

    var mContext: Context? = null

    companion object {
        val instance: DataRepository by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DataRepository() }
    }

    fun init(
        context: Context, remoteDataSource: RemoteDataSource,
    ) {
        AppDatabaseManager.dbManager.createDB(context)
        this.mContext = context
        this.mRemoteDataSource = remoteDataSource
    }

    /**
     * 频道,影视类型接口
     */
    fun loadVideoType(): MutableLiveData<VideoType?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getVideoType()
        } else {
            MutableLiveData()
        }
    }


    fun loadRecommendFeed(page: Int, size: Int): MutableLiveData<HomeTopic?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getRecommendData(page, size)
        } else {
            MutableLiveData()
        }
    }

    fun loadTopicBanner(): MutableLiveData<HomeBanner?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicHomeBanner()
        } else {
            MutableLiveData()
        }
    }

    fun getMovieListData(
        typeId: Int,
        valueIds: String,
        page: Int,
    ): MutableLiveData<MovieListEntity?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getMovieListData(typeId, valueIds, page)
        } else {
            MutableLiveData()
        }
    }


    fun phoneCode(params: WeakHashMap<String, String>): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.phoneCode(params)
        } else {
            MutableLiveData()
        }
    }

    fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.login(params)
        } else {
            MutableLiveData()
        }
    }

    fun wechatLogin(params: WeakHashMap<String, String>): MutableLiveData<LoginResponse?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.wechatLogin(params)
        } else {
            MutableLiveData()
        }
    }


    fun like(json: String, head: String): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.like(json, head)
        } else {
            MutableLiveData()
        }
    }

    fun topicDetail(topicId: Int, head: String): LiveData<TopicDetail>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicDetail(topicId, head)
        } else {
            MutableLiveData()
        }
    }


    fun topicVideos(topicId: Int, page: Int, size: Int): LiveData<TopicVideos>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicVideos(topicId, page, size)
        } else {
            MutableLiveData()
        }
    }


    fun feedback(json: String, head: String): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.feedback(json, head)
        } else {
            MutableLiveData()
        }
    }

    fun hotWords(): LiveData<HotSearchList>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.hotWords()
        } else {
            MutableLiveData()
        }
    }

    fun videoSearch(word: String, page: Int): LiveData<SearchResultEntity>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoSearch(word, page)
        } else {
            MutableLiveData()
        }
    }

    fun searchSuggest(word: String, page: Int): LiveData<SearchSuggestEntity>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.searchSuggest(word, page)
        } else {
            MutableLiveData()
        }
    }

    fun isLoadingSearchData(): LiveData<Boolean>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.isLoadingSearchData()
        } else {
            MutableLiveData()
        }
    }

    fun videoDetail(id: Long): MutableLiveData<VideoDetail?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoDetail(id)
        } else {
            MutableLiveData()
        }
    }


    fun videoSourceEpisode(id: Long): MutableLiveData<VideoSourcePlays?> {
        return mRemoteDataSource.videoSourceEpisode(id)
    }

    fun videoEpisodes(id: Long): MutableLiveData<VideoEpisodes?> {
        return mRemoteDataSource.videoEpisodes(id)
    }



    fun videoSuggest(id: Long): MutableLiveData<VideoSuggest> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoSuggest(id)
        } else {
            MutableLiveData()
        }
    }


    fun playCount(videoId: Long, videoPlayId: Long): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.playCount(videoId, videoPlayId)
        } else {
            MutableLiveData()
        }
    }

    fun topicCount(topicId: Int): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicCount(topicId)
        } else {
            MutableLiveData()
        }
    }

    fun keywordCount(keyword: String): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.keywordCount(keyword)
        } else {
            MutableLiveData()
        }
    }


    /**
     * 错误上报,解析
     */
    fun errorReport(json: String): LiveData<Boolean>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.errorReport(json)
        } else {
            MutableLiveData()
        }
    }

    /**
     * 获取发现页的feed
     */
    fun getDiscoverFeed(page: Int, size: Int): MutableLiveData<Discover?> {
        return mRemoteDataSource.getDiscover(page, size)
    }
}
