package cn.video.star.data.remote

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.junechiu.junecore.utils.ALogger
import cn.video.star.base.Api
import cn.video.star.data.IDataSource
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.remote.api.ApiManager
import cn.video.star.data.remote.model.*
import cn.video.star.data.remote.model.Discover
import com.google.gson.Gson
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RemoteDataSource : IDataSource {

    private var mIsLoadingSearchList: MutableLiveData<Boolean>? = null
    private var mIsLoadingSuggestList: MutableLiveData<Boolean>? = null

    companion object {
        val instance = RemoteDataSourceHolder.holder
    }

    private object RemoteDataSourceHolder {
        val holder = RemoteDataSource()
    }

    init {
        mIsLoadingSearchList = MutableLiveData()
    }

    //获取影视频道列表
    override fun getVideoType(): MutableLiveData<VideoType?> {
        val videoType = MutableLiveData<VideoType?>()
        ApiManager.instance.getApi().videoType().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                videoType.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    try {
                        videoType.value =
                            Gson().fromJson(response.body(), VideoType::class.java)
                        refreshVideoType(response.body()!!) //保存到数据库
                    } catch (e: Exception) {
                        e.printStackTrace()
                        videoType.value = null
                    }
                } else {
                    videoType.value = null
                }
            }
        })
        return videoType
    }

    /**
     * 获取推荐列表
     */
    override fun getRecommendData(page: Int, size: Int): MutableLiveData<HomeTopic?> {
        val videoRecommend = MutableLiveData<HomeTopic?>()
        ApiManager.instance.getApi().topicHome(page, size).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("getHomeFeed", "failure")
                t.printStackTrace()
                videoRecommend.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.body() == null) {
                    Log.d("getHomeFeed", "空数据$response")
                    videoRecommend.value = null
                } else if (!TextUtils.isEmpty(response.body())) {
                    //更新数据
                    try {
                        videoRecommend.value =
                            Gson().fromJson(response.body(), HomeTopic::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        videoRecommend.value = null
                    }
                }
            }
        })
        return videoRecommend
    }


    override fun topicHomeBanner(): MutableLiveData<HomeBanner?> {
        val videoRecommend = MutableLiveData<HomeBanner?>()
        ApiManager.instance.getApi().topicHomeBanner().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                videoRecommend.value = null
                t.printStackTrace()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    try {
                        videoRecommend.value =
                            Gson().fromJson(response.body(), HomeBanner::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return videoRecommend
    }

    //获取视频列表
    override fun getMovieListData(
        typeId: Int, valueIds: String, page: Int,
    ): MutableLiveData<MovieListEntity?> {
        val movieListData = MutableLiveData<MovieListEntity?>()
        ApiManager.instance.getApi().videoTypeList(typeId, valueIds, page)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    movieListData.value = null
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        try {
                            movieListData.value =
                                Gson().fromJson(response.body(), MovieListEntity::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            movieListData.value = null
                        }
                    } else {
                        movieListData.value = null
                    }
                }
            })
        return movieListData
    }


    //加载发现列表
    override fun getDiscover(page: Int, size: Int): MutableLiveData<Discover?> {
        val discoverData = MutableLiveData<Discover?>()
        ApiManager.instance.getApi().discover(page, size).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                discoverData.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    //更新数据
                    try {
                        discoverData.value = Gson().fromJson(response.body(), Discover::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return discoverData
    }


    override fun phoneCode(params: WeakHashMap<String, String>): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().phoneCode(params).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("getPhoneCode", response.body())
                    val json = JSONObject(response.body())
                    if (json.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>? {
        val result = MutableLiveData<LoginResponse>()
        ApiManager.instance.getApi().login(params).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("login", response.body())
                    try {
                        result.value = Gson().fromJson<LoginResponse>(
                            response.body(),
                            LoginResponse::class.java
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }



    override fun modify(json: String, head: String): LiveData<Int> {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().modify(json, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("modify", response.body())
                    val jo = JSONObject(response.body()!!)
                    if (jo.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun avatar(partFile: MultipartBody.Part, token: String): LiveData<Int> {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().avatar(partFile, token).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("avatar", response.body())
                    val jo = JSONObject(response.body()!!)
                    if (jo.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun disLike(json: String, head: String): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().disLikeTopic(json, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("avatar", response.body())
                    var json = JSONObject(response.body())
                    if (json.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun like(json: String, head: String): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().likeTopic(json, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("like", response.body())
                    var json = JSONObject(response.body())
                    if (json.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun topicDetail(topicId: Int, head: String): LiveData<TopicDetail>? {
        val result = MutableLiveData<TopicDetail>()
        ApiManager.instance.getApi().topicDetail(topicId, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
//                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("topicDetail", response.body())
                    try {
                        result.value =
                            Gson().fromJson<TopicDetail>(response.body(), TopicDetail::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }

    override fun topicMe(topicId: Int, head: String): LiveData<TopicMe>? {
        val result = MutableLiveData<TopicMe>()
        ApiManager.instance.getApi().topicMe(topicId, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
//                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("topicMe", response.body())
                    try {
                        result.value =
                            Gson().fromJson<TopicMe>(response.body(), TopicMe::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }

    override fun topicVideos(topicId: Int, page: Int, size: Int): LiveData<TopicVideos>? {
        val result = MutableLiveData<TopicVideos>()
        ApiManager.instance.getApi().topicVideos(topicId, page, size)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
//                result.value = 0
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        ALogger.json("topicVideos", response.body())
                        try {
                            result.value = Gson().fromJson<TopicVideos>(
                                response.body(),
                                TopicVideos::class.java
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        return result
    }

    override fun follow(type: Int, typeIds: String, head: String): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().follow(type, typeIds, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("follow", response.body())
                    var json = JSONObject(response.body())
                    if (json.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun unfollow(type: Int, typeId: String, head: String): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().unFollow(type, typeId, head)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    result.value = 0
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        ALogger.json("unfollow", response.body())
                        var json = JSONObject(response.body())
                        if (json.getInt("code") == Api.RESPONSE_OK) {
                            result.value = 1
                        } else {
                            result.value = 0
                        }
                    }
                }
            })
        return result
    }

    override fun feedback(json: String, head: String): LiveData<Int>? {
        val result = MutableLiveData<Int>()
        ApiManager.instance.getApi().feedback(json, head).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = 0
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("avatar", response.body())
                    var json = JSONObject(response.body())
                    if (json.getInt("code") == Api.RESPONSE_OK) {
                        result.value = 1
                    } else {
                        result.value = 0
                    }
                }
            }
        })
        return result
    }

    override fun hotWords(): LiveData<HotSearchList>? {
        val result = MutableLiveData<HotSearchList>()
        ApiManager.instance.getApi().hotWords().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("hotWords", response.body())
                    try {
                        var search = Gson().fromJson(response.body(), HotSearchList::class.java)
                        result.value = search
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }

    override fun videoSearch(word: String, page: Int): LiveData<SearchResultEntity>? {
        val result = MutableLiveData<SearchResultEntity>()
        mIsLoadingSearchList?.value = true
        ApiManager.instance.getApi().videoSearch(word, page).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
                mIsLoadingSearchList?.value = false
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("videoSearch", response.body())
                    try {
                        val search =
                            Gson().fromJson(response.body(), SearchResultEntity::class.java)
                        result.value = search
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mIsLoadingSearchList?.value = false
                    }
                }
                mIsLoadingSearchList?.value = false
            }
        })
        return result
    }

    override fun searchSuggest(word: String, page: Int): LiveData<SearchSuggestEntity>? {
        val result = MutableLiveData<SearchSuggestEntity>()
        mIsLoadingSuggestList?.value = true
        ApiManager.instance.getApi().searchSuggest(word, page).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                ALogger.json("searchSuggest", t.message)
                t.printStackTrace()
                mIsLoadingSearchList?.value = false
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("searchSuggest", response.code().toString() + response.body())
                ALogger.json("searchSuggest", response.body())
                if (!TextUtils.isEmpty(response.body())) {
                    try {
                        var search =
                            Gson().fromJson<SearchSuggestEntity>(
                                response.body(),
                                SearchSuggestEntity::class.java
                            )
                        result.value = search
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mIsLoadingSearchList?.value = false
                    }
                }
                mIsLoadingSearchList?.value = false
            }
        })
        return result
    }

    override fun isLoadingSearchData(): LiveData<Boolean>? {
        return mIsLoadingSearchList
    }


    /**
     * 视频详情
     */
    override fun videoDetail(id: Long): MutableLiveData<VideoDetail?> {
        val result = MutableLiveData<VideoDetail?>()
        ApiManager.instance.getApi().videoDetail(id).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("videoDetail", response.body())
                    try {
                        val videoData =
                            Gson().fromJson(response.body(), VideoDetail::class.java)
                        result.value = videoData
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result.value = null
                    }
                }
            }
        })
        return result
    }

    /**
     * 根据视频详情id获取所有来源和来源对应的所有集
     */
    override fun videoSourceEpisode(id: Long): MutableLiveData<VideoSourcePlays?> {
        val result = MutableLiveData<VideoSourcePlays?>()
        ApiManager.instance.getApi().videoSourcesPlays(id).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
                result.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("VideoSourcePlays", response.body())
                    try {
                        val videoData =
                            Gson().fromJson(response.body(), VideoSourcePlays::class.java)
                        result.value = videoData
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result.value = null
                    }
                }
            }
        })
        return result
    }

    /**
     * 这个接口用于source下plays大于100集时,单独请求所有集
     * 少于100集就直接用source的plays
     */
    override fun videoEpisodes(id: Long): MutableLiveData<VideoEpisodes?> {
        val result = MutableLiveData<VideoEpisodes?>()
        ApiManager.instance.getApi().getEpisodes(id).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
                result.value = null
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("VideoSourcePlays", response.body())
                    try {
                        val videoData =
                            Gson().fromJson(response.body(), VideoEpisodes::class.java)
                        result.value = videoData
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result.value = null
                    }
                }
            }
        })
        return result
    }



    override fun videoSuggest(id: Long): MutableLiveData<VideoSuggest> {
        val result = MutableLiveData<VideoSuggest>()
        ApiManager.instance.getApi().videoSuggest(id).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                ALogger.json("videoSuggest", t.message)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("videoSuggest", response.body())
                    try {
                        val videoData =
                            Gson().fromJson<VideoSuggest>(response.body(), VideoSuggest::class.java)
                        result.value = videoData
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }



    override fun videoResolve(url: String): LiveData<VideoPlayData> {
        val result = MutableLiveData<VideoPlayData>()
        ApiManager.instance.getApi().videoResolve(url).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("videoResolve", response.body())
                    try {
                        val videoData = Gson().fromJson(
                            response.body(),
                            VideoPlayData::class.java
                        )
                        result.value = videoData
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }

    override fun toolsMd5(
        reqUrl: String,
        headers: Map<String, String>,
        json: String,
    ): MutableLiveData<String?> {
        val result = MutableLiveData<String?>()
        ApiManager.instance.getApi().toolsMd5(reqUrl, headers, json)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    result.value = null
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        try {
                            result.value = response.body()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.value = null
                        }
                    } else {
                        result.value = null
                    }
                }
            })
        return result
    }

    override fun toolsPlay(
        reqUrl: String,
        headers: Map<String, String>,
        json: String,
    ): MutableLiveData<ToolsPlay?> {
        val result = MutableLiveData<ToolsPlay?>()
        ApiManager.instance.getApi().toolsPlay(reqUrl, headers, json)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    result.value = null
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        ALogger.json("toolsPlay", response.body())
                        try {
                            var toolsPlay =
                                Gson().fromJson(response.body(), ToolsPlay::class.java)
                            result.value = toolsPlay
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        return result
    }

    override fun playCount(videoId: Long, videoPlayId: Long): LiveData<String>? {
        val result = MutableLiveData<String>()
        ApiManager.instance.getApi().playCount(videoId, videoPlayId)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!TextUtils.isEmpty(response.body())) {
                        ALogger.json("playCount", response.body())
                        try {
//                            var toolsPlay = Gson().fromJson<ToolsPlay>(response.body(), ToolsPlay::class.java)
//                            result.value = toolsPlay
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        return result
    }

    override fun parseDL(json: String): LiveData<ParseResponse>? {
        val result = MutableLiveData<ParseResponse>()
        ApiManager.instance.getApi().parseDL(json).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("parseDL", response.body())
                    try {
                        var parseData = Gson().fromJson<ParseResponse>(
                            response.body(),
                            ParseResponse::class.java
                        )
                        result.value = parseData
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        return result
    }

    override fun topicCount(topicId: Int): LiveData<String>? {
        val result = MutableLiveData<String>()
        ApiManager.instance.getApi().topicCount(topicId).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("topicCount", response.body())
                }
            }
        })
        return result
    }

    override fun keywordCount(keyword: String): LiveData<String>? {
        val result = MutableLiveData<String>()
        ApiManager.instance.getApi().keywordCount(keyword).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("keywordCount", response.body())
                }
            }
        })
        return result
    }

    //存储VideoType数据
    fun refreshVideoType(json: String) {
        AppDatabaseManager.dbManager.insertVideoType(json)
    }


    override fun playFailReport(json: String): LiveData<Boolean>? {
        val result = MutableLiveData<Boolean>()
        ApiManager.instance.getApi().playFailReport(json).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = false
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("playFailReport", response.body())
                    val json = JSONObject(response.body())
                    result.value = json.getInt("code") == Api.RESPONSE_OK
                }
            }
        })
        return result
    }

    override fun errorReport(json: String): LiveData<Boolean>? {
        val result = MutableLiveData<Boolean>()
        ApiManager.instance.getApi().errorReport(json).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = false
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (!TextUtils.isEmpty(response.body())) {
                    ALogger.json("errorReport", response.body())
                    val json = JSONObject(response.body())
                    result.value = json.getInt("code") == Api.RESPONSE_OK
                }
            }
        })
        return result
    }
}