package cn.asbest.caipiao;

import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import cn.asbest.utils.DeviceUtils;

public class ErrorHandleDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_handle_dialog);
        getSupportActionBar().hide();
        Log.d("cgd","ErrorHandleDialog create");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    public void close(View view){
        DeviceUtils.openApp(this, getPackageName());
        finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
