package cn.asbest.caipiao.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.util.Log;

public class MessengerService extends Service {
    public final static int MSG_EXIT_APP = 0;
    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("cgd","MessengerService onBind");
        return mMessenger.getBinder();
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_EXIT_APP){
                Log.d("cgd","MessengerService recieve close app message");
                stopSelf();
                Process.killProcess(Process.myPid());
            }
            super.handleMessage(msg);
        }
    };

    private final Messenger mMessenger = new Messenger(mHandler);
}
