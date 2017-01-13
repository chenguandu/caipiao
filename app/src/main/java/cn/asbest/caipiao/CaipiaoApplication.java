package cn.asbest.caipiao;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import cn.asbest.caipiao.services.ErrorUploadService;
import cn.asbest.utils.DeviceUtils;

/**
 * Created by chenyanlan on 2016/11/18.
 */

public class CaipiaoApplication extends Application {
    static CaipiaoApplication mCaipiaoApplication = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mCaipiaoApplication = this;
        String processName = DeviceUtils.getProcessName(this);
        Log.d("cgd","processName:"+processName);
        if (!(getPackageName()+":error").equals(processName)){
            initLogHandle();
        }
    }

    public static CaipiaoApplication getInstance(){
        return mCaipiaoApplication;
    }

    private void initLogHandle(){
        CaipiaoHandleException xpecHandleException = CaipiaoHandleException.getInstance();
        xpecHandleException.init(this);
        ErrorUploadService.uploadError(this, null, true);
    }

    public void shutupApp(){
        Process.killProcess(Process.myPid());
    }
}
