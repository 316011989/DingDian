package  cn.video.star.data.remote.api

import cn.video.star.data.remote.model.VideoDetail
import cn.video.star.data.remote.model.VideoSourcePlays
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Created by junzhao on 2018/2/15.
 */
interface ApiService {

    //phone，deviceid
    @FormUrlEncoded
    @POST("user/phonecode")
    fun phoneCode(@FieldMap params: WeakHashMap<String, String>): Call<String>

    @Headers("Content-Type: application/json")
    @POST
    fun toolsMd5(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body json: String,
    ): Call<String>

    @Headers("Content-Type: application/json")
    @POST
    fun toolsPlay(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body json: String,
    ): Call<String>

    //微信的e
    @FormUrlEncoded
    @POST("user/wechat/login")
    fun wechatLogin(@FieldMap params: WeakHashMap<String, String>): Call<String>


    @GET("topic/discover")
    fun discover(@Query("page") page: Int, @Query("size") size: Int): Call<String>

    @GET("topic/discover/banner")
    fun discoverBanner(): Call<String>

    @GET("topic/detail")
    fun topicDetail(
        @Query("topicId") topicId: Int,
        @Header("Authorization") head: String,
    ): Call<String>

    @GET("topic/detail/me")
    fun topicMe(@Query("topicId") topicId: Int, @Header("Authorization") head: String): Call<String>

    @GET("topic/videos")
    fun topicVideos(
        @Query("topicId") topicId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Call<String>

    @GET("topic/home")
    fun topicHome(@Query("page") page: Int, @Query("size") size: Int): Call<String>

    @GET("topic/home/banner")
    fun topicHomeBanner(): Call<String>

    @GET("video/detail")
    fun videoDetail(@Query("id") id: Long): Call<String>

    @GET("video/detail")
    fun videoDetailObservable(@Query("id") id: Long): Observable<VideoDetail>

    @GET("v2/video/episodes")
    fun videoSourcesObservable(@Query("id") id: Long): Observable<VideoSourcePlays>

    @GET("v2/video/episodes")
    fun videoSourcesPlays(@Query("id") id: Long): Call<String>

    @GET("video/episodes")
    fun getEpisodes(@Query("id") id: Long): Call<String>

    @GET("video/detail/suggest")
    fun videoSuggest(@Query("id") id: Long): Call<String>

    @GET("video/detail/me")
    fun videoMe(@Query("id") id: Long): Call<String>

    //phone password code deviceid
    @FormUrlEncoded
    @POST("user/into")
    fun login(@FieldMap params: WeakHashMap<String, String>): Call<String>

    //修改资料
    @Headers("Content-Type: application/json")
    @POST("user/modify/info")
    fun modify(@Body json: String, @Header("Authorization") head: String): Call<String>

    //修改头像
    @Multipart
    @POST("user/avatar")
    fun avatar(
        @Part partFile: MultipartBody.Part,
        @Header("Authorization") head: String,
    ): Call<String>

    //用户信息
    @FormUrlEncoded
    @POST("user/me")
    fun me(@Field("userid") userid: Int, @Header("Authorization") head: String): Call<String>

    //影视类别
    @GET("video/type")
    fun videoType(): Call<String>

    @GET("video/list")
    fun videoList(
        @Query("typeId") typeId: Int, @Query("categoryId") categoryId: Int,
        @Query("valueId") valueId: Int,
    ): Call<String>

    @GET("search/video/type")
    fun videoTypeList(
        @Query("typeId") typeId: Int, @Query("valueIds") valueIds: String,
        @Query("page") page: Int,
    ): Call<String>

    @GET("v2/search/video")
    fun videoSearch(@Query("word") word: String, @Query("page") page: Int): Call<String>

    @GET("v2/suggest/video")
    fun searchSuggest(@Query("word") word: String, @Query("page") page: Int): Call<String>


    //热词搜索
    @GET("search/words")
    fun hotWords(): Call<String>

    //type 1视频，2播单   typeId 视频id或播单id
    @GET("subscribe")
    fun follow(
        @Query("type") type: Int,
        @Query("typeIds") typeIds: String,
        @Header("Authorization") head: String,
    ): Call<String>

    //取消关注
    @GET("v2/subscribe")
    fun unFollow(
        @Query("type") type: Int,
        @Query("typeIds") typeIds: String,
        @Header("Authorization") head: String,
    ): Call<String>


    @Headers("Content-Type: application/json")
    @POST("video/resolve")
    fun videoResolve(@Body url: String): Call<String>

    @Headers("Content-Type: application/json")
    @POST("feedback/add")
    fun feedback(@Body json: String, @Header("User-Agent") head: String): Call<String>


    @GET("app/version")
    fun version(): Call<String> //VersionResponse

    @Headers("Content-Type: application/json")
    @POST("topic/heart")
    fun likeTopic(@Body json: String, @Header("Authorization") head: String): Call<String>

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "topic/heart", hasBody = true)
    fun disLikeTopic(@Body json: String, @Header("Authorization") head: String): Call<String>

    @Headers("Content-Type: application/json")
    @POST("video/play/dl")
    fun parseDL(@Body json: String): Call<String>

    @GET("video/countSum")
    fun playCount(
        @Query("videoId") videoId: Long,
        @Query("videoPlayId") videoPlayId: Long,
    ): Call<String>

    @GET("topic/countSum")
    fun topicCount(@Query("topicId") topicId: Int): Call<String>

    @GET("keyword/countSum")
    fun keywordCount(@Query("word") word: String): Call<String>

    //解析新接口,替换videoResolve和parseDL两个接口
    @POST
    fun parsePlayUrl(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body param: String,
    ): Call<String>

    //我方服务器解析南瓜域名和header
    @Headers("Content-Type: application/json")
    @POST
    fun parseNanguaPlayUrl(@Url url: String, @Body json: String): Call<String>

    @GET
    fun requstNanguaUrl(@Url url: String, @HeaderMap headers: Map<String, String>): Call<String>

    @GET
    fun encryptLv(@Url url: String, @HeaderMap lv: Map<String, String>): Call<String>


    @Headers("Content-Type: application/json")
    @POST("api/playFailReport/add")
    fun playFailReport(@Body json: String): Call<String>

    //上报到主库
    @Headers("Content-Type: application/json")
    @POST("clip/v3/error")
    fun errorReport(@Body json: String): Call<String>

    @Headers("Content-Type: application/json")
    @POST
    fun submitPingTraceRoute(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body json: String,
    ): Call<String>
}
