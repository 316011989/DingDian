package cn.video.star.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.video.star.data.remote.model.*
import cn.video.star.data.remote.model.Discover
import okhttp3.MultipartBody
import java.util.*


//数据源
interface IDataSource {

    //查询影视数据分类videoType
    fun getVideoType(): MutableLiveData<VideoType?>

    fun isLoadingRecommendData(): LiveData<Boolean>?

    fun getRecommendData(page: Int, size: Int): LiveData<HomeTopic>?

    fun topicHomeBanner(): MutableLiveData<HomeBanner?>

    fun getMovieListData(
        typeId: Int,
        valueIds: String,
        page: Int,
    ): MutableLiveData<MovieListEntity?>

    fun getDiscover(page: Int, size: Int): MutableLiveData<Discover?>

    fun phoneCode(params: WeakHashMap<String, String>): LiveData<Int>?

    fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>?

    fun modify(json: String, head: String): LiveData<Int>?

    fun avatar(partFile: MultipartBody.Part, token: String): LiveData<Int>?

    fun disLike(json: String, head: String): LiveData<Int>?

    fun like(json: String, head: String): LiveData<Int>?

    fun topicDetail(topicId: Int, head: String): LiveData<TopicDetail>?

    fun topicMe(topicId: Int, head: String): LiveData<TopicMe>?

    fun topicVideos(topicId: Int, page: Int, size: Int): LiveData<TopicVideos>?

    //type 1视频，2播单   typeId 视频id或播单id
    fun follow(type: Int, typeIds: String, head: String): LiveData<Int>?

    //type 1视频，2播单   typeId 视频id或播单id
    fun unfollow(type: Int, typeId: String, head: String): LiveData<Int>?

    fun feedback(json: String, head: String): LiveData<Int>?

    fun hotWords(): LiveData<HotSearchList>?

    fun videoSearch(word: String, page: Int): LiveData<SearchResultEntity>?

    fun searchSuggest(word: String, page: Int): LiveData<SearchSuggestEntity>?

    fun isLoadingSearchData(): LiveData<Boolean>?

    fun videoDetail(id: Long): MutableLiveData<VideoDetail?>

    fun videoSourceEpisode(id: Long): MutableLiveData<VideoSourcePlays?>

    fun videoEpisodes(id: Long): MutableLiveData<VideoEpisodes?>

    fun videoSuggest(id: Long): LiveData<VideoSuggest>?

    fun videoResolve(url: String): LiveData<VideoPlayData>?

    fun toolsMd5(
        reqUrl: String,
        headers: Map<String, String>,
        url: String,
    ): MutableLiveData<String?>

    fun toolsPlay(
        reqUrl: String,
        headers: Map<String, String>,
        json: String,
    ): MutableLiveData<ToolsPlay?>

    fun playCount(videoId: Long, videoPlayId: Long): LiveData<String>?

    fun topicCount(topicId: Int): LiveData<String>?

    fun keywordCount(keyword: String): LiveData<String>?

    fun parseDL(json: String): LiveData<ParseResponse>?

    fun playFailReport(json: String): LiveData<Boolean>?

    fun errorReport(json: String): LiveData<Boolean>?
}