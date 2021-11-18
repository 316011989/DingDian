package cn.junechiu.junecore.net.retrofit;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by junzhao on 2017/12/2.
 */

public interface RestService {

    @GET
    Call<String> get(@Url String url, @QueryMap Map<String, Object> map);

    @GET
    Call<String> getWithHeaders(@Url String url, @HeaderMap Map<String, Object> map);

    @FormUrlEncoded
    @POST
    Call<String> post(@Url String url, @FieldMap Map<String, Object> map);

    @POST
    Call<String> postRaw(@Url String url, @Body RequestBody body);

    @Headers("Content-Type: application/json")
    @POST
    Call<String> postJson(@Url String url, @Body String json);

    @FormUrlEncoded
    @PUT
    Call<String> put(@Url String url, @FieldMap Map<String, Object> map);

    @POST
    Call<String> putRaw(@Url String url, @Body RequestBody body);

    @DELETE
    Call<String> delete(@Url String url, @QueryMap Map<String, Object> map);

    @Streaming
    @GET
    Call<ResponseBody> download(@Url String url, @QueryMap Map<String, Object> map);

    @Multipart
    @POST
    Call<String> upload(@Url String url, @Part MultipartBody.Part file);

}
