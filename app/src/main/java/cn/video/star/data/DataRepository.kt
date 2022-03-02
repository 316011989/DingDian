package cn.video.star.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.remote.RemoteDataSource
import cn.video.star.data.remote.model.*
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
        val instance = DataRepositoryHolder.holder
    }

    private object DataRepositoryHolder {
        val holder = DataRepository()
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
    fun loadVideoType(): MutableLiveData<VideoType?>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getVideoType()
        } else {
            null
        }
    }


    fun loadRecommendFeed(page: Int, size: Int): MutableLiveData<HomeTopic?> {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getRecommendData(page, size)
        } else {
            MutableLiveData()
        }
    }

    fun loadTopicBanner(): MutableLiveData<HomeBanner?>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicHomeBanner()
        } else {
            null
        }
    }

    fun getMovieListData(
        typeId: Int,
        valueIds: String,
        page: Int,
    ): MutableLiveData<MovieListEntity?>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.getMovieListData(typeId, valueIds, page)
        } else {
            null
        }
    }


    fun phoneCode(params: WeakHashMap<String, String>): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.phoneCode(params)
        } else {
            null
        }
    }

    fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.login(params)
        } else {
            null
        }
    }


    fun topicDetail(topicId: Int, head: String): LiveData<TopicDetail>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicDetail(topicId, head)
        } else {
            null
        }
    }

    fun topicMe(topicId: Int, head: String): LiveData<TopicMe>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicMe(topicId, head)
        } else {
            null
        }
    }

    fun topicVideos(topicId: Int, page: Int, size: Int): LiveData<TopicVideos>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicVideos(topicId, page, size)
        } else {
            null
        }
    }

    //type 1视频，2播单   typeId 视频id或播单id
    fun follow(type: Int, typeIds: String, head: String): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.follow(type, typeIds, head)
        } else {
            null
        }
    }

    //type 1视频，2播单   typeId 视频id或播单id
    fun unfollow(type: Int, typeId: String, head: String): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.unfollow(type, typeId, head)
        } else {
            null
        }
    }

    fun feedback(json: String, head: String): LiveData<Int>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.feedback(json, head)
        } else {
            null
        }
    }

    fun hotWords(): LiveData<HotSearchList>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.hotWords()
        } else {
            null
        }
    }

    fun videoSearch(word: String, page: Int): LiveData<SearchResultEntity>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoSearch(word, page)
        } else {
            null
        }
    }

    fun searchSuggest(word: String, page: Int): LiveData<SearchSuggestEntity>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.searchSuggest(word, page)
        } else {
            null
        }
    }

    fun isLoadingSearchData(): LiveData<Boolean>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.isLoadingSearchData()
        } else {
            null
        }
    }


    fun videoDetail(id: Long): MutableLiveData<VideoDetail?>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoDetail(id)
        } else {
            null
        }
    }

    fun videoSourceEpisode(id: Long): MutableLiveData<VideoSourcePlays?> {
        return mRemoteDataSource.videoSourceEpisode(id)
    }

    fun videoEpisodes(id: Long): MutableLiveData<VideoEpisodes?> {
        return mRemoteDataSource.videoEpisodes(id)
    }


    fun videoSuggest(id: Long): MutableLiveData<VideoSuggest>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.videoSuggest(id)
        } else {
            null
        }
    }


    fun playCount(videoId: Long, videoPlayId: Long): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.playCount(videoId, videoPlayId)
        } else {
            null
        }
    }

    fun topicCount(topicId: Int): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.topicCount(topicId)
        } else {
            null
        }
    }

    fun keywordCount(keyword: String): LiveData<String>? {
        return if (NetworkUtils.isConnected()) {
            mRemoteDataSource.keywordCount(keyword)
        } else {
            null
        }
    }


    /**
     * 获取发现页的feed
     */
    fun getDiscoverFeed(page: Int, size: Int): MutableLiveData<Discover?>? {
        return mRemoteDataSource.getDiscover(page, size)
    }
}
