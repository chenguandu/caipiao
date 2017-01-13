package cn.asbest.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by chenyanlan on 2016/11/18.
 */

public class SecureTool {
    /**
     * 执行加密算法
     * @param strSrc 需要加密的字符串
     * @param encName 加密规则
     * @return
     */
    public static String encrypt(String strSrc, String encName) {

        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try {
            if (encName == null || encName.equals("")) {
                encName = "MD5";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return strDes;
    }

    public static String encryptApiCloudKey(String appId,String appKey, String encName){
        Date date = new Date();
        Long time = date.getTime();
        String strSrc = appId+"UZ"+appKey+"UZ" + time;
        String encStr = encrypt(strSrc, encName)+"."+time;
        return encStr;
    }

    private static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }
}
