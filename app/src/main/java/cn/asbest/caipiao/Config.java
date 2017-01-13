package cn.asbest.caipiao;

import java.util.Date;

/**
 * Created by chenyanlan on 2016/11/6.
 */

public class Config {
    public static String Server_Caipiao = "http://f.apiplus.cn/";
    public static String Server_Apicloud = "https://d.apicloud.com/";
    public static String APPID = "A6924821034843";
    public static String APPKEY = "3961951B-4DBA-DD2D-E40D-004A5A12F632";

    /**
     * 需要验证的https主机列表
     */
    public static String Https_Hosts[]= {"d.apicloud.com"};
    /**
     * https证书列表
     */
    public static int[] Certificate = new int[]{R.raw.apicloud};
}
