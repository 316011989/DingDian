package cn.video.star.utils;


import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ParsePlayUrlUtil {


    /**
     * 服务器返回的解析后数据解密
     *
     * @param result
     * @return
     */
    public static String AESDecryptIpList(String result) {
        try {
            String password = "JA&sbV&X3J)vXAhn";
            byte[] content = Base64.decode(result, Base64.NO_WRAP);
            SecretKeySpec keySpec = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] param = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec iv = new IvParameterSpec(param);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return new String(cipher.doFinal(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}