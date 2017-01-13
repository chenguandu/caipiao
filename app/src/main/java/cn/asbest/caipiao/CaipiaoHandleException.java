package cn.asbest.caipiao;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import cn.asbest.caipiao.services.ErrorUploadService;

/**
 * Created by chenguandu on 2016-07-14.
 */
public class CaipiaoHandleException implements Thread.UncaughtExceptionHandler {
    private static CaipiaoHandleException mCaipiaoHandleException;
    private Context mContext;

    private CaipiaoHandleException(){}

    public static synchronized CaipiaoHandleException getInstance(){
        if(mCaipiaoHandleException == null){
            mCaipiaoHandleException = new CaipiaoHandleException();
        }
        return mCaipiaoHandleException;
    }

    public void init(Context ctx) {
        mContext = ctx;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private String getErrorInfo(Throwable ex){
        String result = null;
        try {
            Writer info = new StringWriter();
            PrintWriter printWriter = new PrintWriter(info);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            result = info.toString();
            printWriter.close();
            info.close();
        } catch (Exception e) {
        }
        return result;
    }

    private void showErrorDialog(){

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
//                final AlertDialog dialog = new AlertDialog.Builder(mContext).setCancelable(false)
//                .create();
//                Window window = dialog.getWindow();
//                window.setType(WindowManager.LayoutParams.TYPE_TOAST);
//                dialog.show();
//                window.setContentView(R.layout.dialog_error);
//                window.findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                        ErrorUploadService.restartApp(mContext, 500);//需要延时，在杀掉后再启动
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                    }
//                });

                Intent intent = new Intent(CaipiaoApplication.getInstance(), ErrorHandleDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CaipiaoApplication.getInstance().startActivity(intent);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 2000);
                Looper.loop();
            }
        }.start();

    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            showErrorDialog();
            String error = getErrorInfo(ex);
            upload(error);
            ex.printStackTrace();
        } catch (Exception e) {
            Log.e("cgd", "an error occured while writing report file...", e);
        }
    }

    private void upload(String errorInfo){
        ErrorUploadService.uploadError(mContext, errorInfo, false);
    }
}
