package cn.video.star.zuida;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

//AES加密解密
public class EncreptUtil {


    @Test
    public void t() {
        String oldString = "A778F59CF25F0ECE2D3FC44178CB1E0D5DEB5BA828789283DE11B9F4F8EA798C3F6EDB3D0A899AA29374614F93FACB130CAFB3B411837EC0289C9BDEC3A3DF741B544FECCEAACD8B2087371C41E66DA671622CD5FCD939B3299D4C9B9F35F0DBE8BF9A7E211BDD33165DC3E72E313799609E45927D29C30523C65A5C8EB1120A0F9374B3B2B3BDDA33BDE5D90EE5619CCCFD468F3FC1F95AA0CB37CB058065AECB4AB4DE1EEF99B37D957314A60190106B1042E2E72541B3300254918B7979E7587837138F7FE09CAF92F02F7515DA4CB3803DA0BF794F11303EF0E59540378809D86B809BCCF4BCBD3DCB5D1472216A";
        System.out.println("旧加密数据====" + oldString);
        String deString = decrypt(oldString);
        System.out.println("进行解密====" + deString);
        String enString = encrypt(deString);
        System.out.println("再加密验证加解密流程正确性====" + oldString.equals(enString));


        String newString = "{\"rule\":[{\"min\":\"1400\",\"max\":\"1700\",\"clarityId\":\"3\"},{\"min\":\"2000\",\"max\":\"2400\",\"clarityId\":\"3\"},{\"type\":\"2\",\"clarityId\":\"2\"},{\"type\":\"1|4|3\",\"clarityId\":\"3\"}],\"timeApi\":\"\",\"defaultId\":\"3\",\"userFirst\":\"0\",\"episodeChangeClarity\":\"0\"}";
        System.out.println("新未加密数据====" + newString);
        String enNewString = encrypt(newString);
        System.out.println("进行加密====" + enNewString);
        String deNewString = decrypt(enNewString);
        System.out.println("再解密验证加解密流程正确性====" + newString.equals(deNewString));
    }

    // /** 算法/模式/填充 **/
    private static final String CipherMode = "AES/ECB/PKCS5Padding";

    //加密解密原始密钥
    public static final String ENCREPT_PW = "ikickercn";

    ///** 创建密钥 **/
    private static SecretKeySpec createKey(String password) {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuffer sb = new StringBuffer(32);
        sb.append(password);
        while (sb.length() < 32) {
            sb.append("0");
        }
        if (sb.length() > 32) {
            sb.setLength(32);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(data, "AES");
    }

    /**
     * 加密(结果为16进制字符串)
     **/
    public static String encrypt(String content) {
        byte[] data = null;
        try {
            data = content.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = encrypt(data, ENCREPT_PW);
        String result = byte2hex(data);
        return result;
    }


    /**
     * 加密(结果为16进制字符串)
     **/
    private static String encrypt(String content, String password) {
        byte[] data = null;
        try {
            data = content.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = encrypt(data, password);
        String result = byte2hex(data);
        return result;
    }

    /**
     * 加密字节数据
     **/
    private static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字节数组转成16进制字符串
     **/
    private static String byte2hex(byte[] b) { // 一个字节的数，
        StringBuffer sb = new StringBuffer(b.length * 2);
        String tmp = "";
        for (int n = 0; n < b.length; n++) {
            // 整数转成十六进制表示
            tmp = (Integer.toHexString(b[n] & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 转成大写
    }

    /**
     * 解密16进制的字符串为字符串
     **/
    public static String decrypt(String content, String password) {
        byte[] data = null;
        try {
            data = hex2byte(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = decrypt(data, password);
        if (data == null)
            return null;
        String result = null;
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 解密字节数组
     **/
    private static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 将hex字符串转换成字节数组
     **/
    private static byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }


    /**
     * 解密16进制的字符串为字符串
     **/
    public static String decrypt(String content) {
        byte[] data = null;
        try {
            data = hex2byte(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = decrypt(data, ENCREPT_PW);
        if (data == null)
            return null;
        String result = null;
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
