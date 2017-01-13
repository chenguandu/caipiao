package cn.asbest.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by chenyanlan on 2016/11/18.
 */

public class FileUtils {

    public static boolean saveJsonToFile(Context context, String jsonString, String fileName) {
        boolean ret = false;
        File file = new File(context.getFilesDir(), fileName);
        // 删除指定路径下的文件
        if (file.exists()) {
            file.delete();
        }
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(file)); // 实例化打印流对象
            out.print(jsonString); // 输出加密数据
            ret = true;
        } catch (FileNotFoundException e) {
            ret = false;
            e.printStackTrace();
        } finally {
            if (out != null) { // 如果打印流不为空，则关闭打印流
                out.close();
            }
        }
        return ret;
    }

    public static String getJsonStrFromFile(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        BufferedReader out = null;
        String jsonString = "";
        if (!file.exists()) {
            return null;
        }
        try {
            out = new BufferedReader(new FileReader(file));
            while (out.ready()) {
                try {
                    jsonString += out.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonString == null ? null : jsonString;
    }
}
