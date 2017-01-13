package cn.asbest.widget;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by chenyanlan on 2016/11/21.
 */

public class ToastExt extends Toast {
    private boolean isShowing = false;

    public ToastExt(Context context) {
        super(context);
    }

    public ToastExt(Context c, View v) {
        this(c);
        setView(v);
    }

    /****************** 自己写的接口方法 **************************/
    // 让view 一直显示;取消用cancel方法，其他用法同Toast；
    public void showAlways() {
        Field mTNField = null;
        try {
            mTNField = Toast.class.getDeclaredField("mTN");
            // 获取权限
            mTNField.setAccessible(true);
            // 得到实例，这里反射不用newinstance;
            Object mTN = mTNField.get(this);
            try {
                // android 4.0以上系统要设置mT的mNextView属性
                Field mNextViewField = mTN.getClass().getDeclaredField(
                        "mNextView");
                mNextViewField.setAccessible(true);
                mNextViewField.set(mTN, getView());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            Method showMethod = mTN.getClass().getDeclaredMethod("show", (Class<?>[]) null);
            showMethod.setAccessible(true);
            showMethod.invoke(mTN, (Object[]) null);
            isShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        super.show();
        isShowing = true;
    }

    // 显示多少豪秒自动退出；用了show方法的多态；
    public void show(long delayMillis) {
        // TODO Auto-generated method stub
        // 定时退出
        showAlways();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                cancel();
            }
        }, delayMillis);
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
        super.cancel();
        isShowing = false;

    }

    // 判断toast是否正在显示
    public boolean isShowing() {
        return isShowing;
    }
    /****************结束接口******************/
}
