package cn.asbest.caipiao.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.asbest.caipiao.ApiManager;
import cn.asbest.caipiao.Config;
import cn.asbest.caipiao.factory.GsonSecureConverterFactory;
import cn.asbest.http.HttpsHelper;
import cn.asbest.model.ErrorInfo;
import cn.asbest.utils.DeviceUtils;
import cn.asbest.utils.FileUtils;
import cn.asbest.utils.SecureTool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ErrorUploadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPLOADERROR = "cn.asbest.caipiao.services.action.UPLOADERROR";
    private static final String ACTION_RESTARTAPP = "cn.asbest.caipiao.services.action.RESTARTAPP";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "cn.asbest.caipiao.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "cn.asbest.caipiao.services.extra.PARAM2";

    private Subscription mSubscription;
    private boolean mCommitLastFail;

    public ErrorUploadService() {
        super("ErrorUploadService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("cgd","ErrorUploadService onDestroy");
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mCommitLastFail) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void uploadError(Context context, String param1, boolean param2) {
        Intent intent = new Intent(context, ErrorUploadService.class);
        intent.setAction(ACTION_UPLOADERROR);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void restartApp(Context context, int delay) {
        Intent intent = new Intent(context, ErrorUploadService.class);
        intent.setAction(ACTION_RESTARTAPP);
        intent.putExtra(EXTRA_PARAM1, delay);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOADERROR.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final boolean param2 = intent.getBooleanExtra(EXTRA_PARAM2, false);
                mCommitLastFail = param2;
                handleActionUploadError(param1, param2);
            } else if (ACTION_RESTARTAPP.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_PARAM1, 0);
                handleRestartApp(param1);
            }
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUploadError(String param1, boolean commitLastFail) {
        Log.d("cgd","commit last fail:"+commitLastFail);
        if (!commitLastFail) {
            String device = getDeviceInfo();
            String version = getVersionInfo();
            String versionName = DeviceUtils.getVersionName(this);
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            Map<String, String> params = new HashMap<>();
            params.put("deviceInfo", device);
            params.put("softwareInfo", version);
            params.put("errorInfo", param1);
//            params.put("userId", LauncherApp.getInstance().getUserInfoId());
//            params.put(DataStorage.APP_CLIENT_MAC, FileUtils.getMachineCode());
//            params.put("errorTime", date);
            String fileName = "crash_version_" + versionName + "_" + date + ".log";
            FileUtils.saveJsonToFile(this, new JSONObject(params).toString(), fileName);
        }
        commitLogs();
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleRestartApp(int delay) {
        try {
            Thread.sleep(delay);
            DeviceUtils.openApp(ErrorUploadService.this, getPackageName());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("cgd","InterruptedException:"+e.getMessage());
        }
    }

    private String getVersionInfo(){
        return "VersionName = "+ DeviceUtils.getVersionName(this)+", VersionCode = "+DeviceUtils.getVersionCode(this);
    }

    private String getDeviceInfo(){
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("RELEASE="+ Build.VERSION.RELEASE).append("\n");
            sb.append("SDK_INT="+ Build.VERSION.SDK_INT).append("\n");

            Field[] fields = Build.class.getDeclaredFields();
            for(Field field: fields){
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name+"="+value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String[] getLogs(){
        File filesDir = getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        };
        String[] logs = filesDir.list(filter);
        return logs;
    }

    public void commitLogs(){
        if (!DeviceUtils.netIsConnected(this)){
            return;
        }
        String[] logs = getLogs();
        if (logs != null && logs.length > 0){
            for (String log: logs) {
                File file = new File(getFilesDir(), log);
                String jsonStr = FileUtils.getJsonStrFromFile(this, log);
                file.delete();
                if (!TextUtils.isEmpty(jsonStr)){
                    mSubscription = uploadErrorInfo(jsonStr)
                            .subscribe(new Subscriber<ErrorInfo>() {
                                @Override
                                public void onCompleted() {
                                    Log.d("cgd","commit log completed");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d("cgd","commit log failed->"+e.getMessage());
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(ErrorInfo errorInfo) {
                                    Log.d("cgd","commit log success");
                                }
                            });
                }
            }
        }
    }

    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        //手动创建一个OkHttpClient并设置超时时间
        okHttpClientBuilder.connectTimeout(15, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        okHttpClientBuilder.sslSocketFactory(HttpsHelper.getSSLSocketFactory(this, Config.Certificate));
        okHttpClientBuilder.hostnameVerifier(HttpsHelper.getHostnameVerifier(Config.Https_Hosts));
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("X-APICloud-AppId", Config.APPID)
                        .addHeader("X-APICloud-AppKey", SecureTool.encryptApiCloudKey(Config.APPID, Config.APPKEY,"SHA-1"))
                        .build();
                return chain.proceed(request);
            }

        });

        return okHttpClientBuilder.build();
    }

    GsonSecureConverterFactory.ResultHandler handler = new GsonSecureConverterFactory.ResultHandler(){
        @Override
        public String handle(ResponseBody value) {
            String result = null;
            try {
                result = value.string();
                //这里可以做解密工作
                Log.d("cgd", "response:"+result);
            } catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }
    };

    private RequestBody getJsonRequestBody(String param){
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),param);
    }

    private Observable<ErrorInfo> uploadErrorInfo(String info){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.Server_Apicloud)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 添加Rx适配器
                .addConverterFactory(GsonSecureConverterFactory.create(handler)) // 添加Gson转换器
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(provideOkHttpClient())
                .build();
        ApiManager apiManager = retrofit.create(ApiManager.class);
        return apiManager.uploadErrorInfo(getJsonRequestBody(info))
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
