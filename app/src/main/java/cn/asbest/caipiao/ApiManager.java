package cn.asbest.caipiao;

import cn.asbest.model.ErrorInfo;
import cn.asbest.model.Ssq;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by chenyanlan on 2016/11/11.
 */

public interface ApiManager {
    @GET("ssq-20.json")
    Observable<Ssq> getData();

    @POST("mcm/api/error")
    Observable<ErrorInfo> uploadErrorInfo(@Body RequestBody errorInfo);
}
