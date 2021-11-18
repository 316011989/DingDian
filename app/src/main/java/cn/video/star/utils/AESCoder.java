package cn.video.star.utils;

import cn.junechiu.junecore.utils.Base64Java;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {
    //南瓜IV
    public static final byte[] zero_iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //常用iv
    public static final byte[] commmon_iv = "0102030405060708".getBytes();

    private static final byte[] iv1 = "DJ@#dd'a'asl;dfk".getBytes();//人人加密,人人解密,短视频,定位使用的iv

    /**
     * 加密
     * 人人,定位城市,定位
     */
    public static String Encrypt_First(String sSrc, String sKey) throws Exception {
        return Encrypt(sSrc, sKey, iv1);
    }


    /**
     * 南瓜解密
     */
    public static String Decrypt_Second(String sSrc, String sKey) throws Exception {
        return Decrypt(sSrc, sKey, AESCoder.zero_iv);
    }

    /**
     * 解密
     * 人人,短视频,定位
     */
    public static String Decrypt_First(String sSrc, String sKey) throws Exception {
        return Decrypt(sSrc, sKey, iv1);
    }


    /**
     * 基础加密方法
     */
    public static String Encrypt(String sSrc, String sKey, byte[] iv) throws Exception {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return new String(Base64Java.getEncoder().encode(encrypted));//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    /**
     * 基础解密方法
     */

    public static String Decrypt(String sSrc, String sKey, byte[] iv) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("UTF8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
            byte[] encrypted1 = Base64Java.getDecoder().decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    /**
     * 测试类方法
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        /*
         * 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符，虽然不会错，至于怎么裁决，个人看情况而定
         * 此处使用AES-128-CBC加密模式，key需要为16位。
         */
        String cKey = "8q(yd98^&76d7typ";
        // 需要加密的字串
        String cSrc = "seasonMark=&area=USK&category=%E5%85%A8%E9%83%A8%E7%B1%BB%E5%9E%8B&page=1&token=&query=lastUpdate&rows=18&t=1527865961514";
        System.out.println(cSrc);
        // 加密
        long lStart = System.currentTimeMillis();
        String enString = AESCoder.Encrypt_First(cSrc, cKey);
        System.out.println("加密后的字串是：" + enString);

        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");
        // 解密
        enString = "VkyH8nZ6bCaRpM64JrjLLFZiXGummrjqwvO8CBEkqVGp0pzBnsbBP2LyzZ35LQ1VRRJodo1R5Pso" +
                "tj7iN5vi7vW+HZXCHmCReBfQUiV9MZ2iWuVIMpWenpF1Q38kk55BozsUasRIhY94lVNzrcyHC8RW" +
                "cPJq7Qsj5NarGR589DBXbPFBBKvJboCsMYZV4+DnqKsej/b9ZLkt4+ElL9rgw0D2euWz2EZhFH/N" +
                "0e528MuBf6P/ZlOuzHzhTe+6yfwcJIkdUNqG/BDCeh84jG/3TBaFW/AZrb4HH2brkTPM1uudzR2l" +
                "6tvrNcTN9Ra8mRd+rIJ9cm6pHBZgl/li1JOTj5Ppnb/b3BYHRWw86GMAMSfs7pM+TxHoMLIarUL+" +
                "/IdiL8CHEvP58jbKaaYuAL2qNpKBGGXRuFfVSFccXMk/sw19nJ4vJlOk7rJLO3Pw+4/G9km8RglQ" +
                "Ti/uDKZ7E9O68+ySTPScJm78GXKbYlDRULQp2h+YvFly1Gpuv12vN4P27uMjBaShMyoKh4NRnuBY" +
                "EhyGALXMhi6Tjsvg4/Cyh/HMLofoIgf3GEaBcvKe7rrehmWui5X19F6yu96/NRC0skF5hVmevUL5" +
                "RDHZEsvHBam0A7A4BjUtcf4aA7AHQ99mztf5R3YRCo19TBIWvF1CYUwRAgOnJqNR0ZcBoiAM6nZM" +
                "/HH64/M/LXgCB5ozk2wgXI9x1IAEUR6aqTqiYL3BfVpIAZ+DxumOm4s8oztpA/BsWrxAxBY80Uh1" +
                "zsln/OX7lV1AYjF2kwb7fXdsCuIWtbtx/nMFh0PKgI8IUi7G17J1jetML8MuaXIhXM14ARAcWTQJ" +
                "VmS8zpwHq9juBlvn/KgNXj8Wl2oR8DeJ2t+WHl4=";
        lStart = System.currentTimeMillis();
        String DeString = Decrypt_First(enString, cKey);
        System.out.println("解密后的字串是：" + DeString);
        String keyA = "DN7SgBmdeS!WgO@G";

        String keyB = "5UD6afzH@0PlUhJC";

        String DeStringA = AESCoder.Decrypt_First(enString, keyB);
        System.out.println("解密后的字串是：A" + DeStringA);
        enString = "4ObazwsM2RHhA0JlydYJnA==";
        String DeStringB = AESCoder.Decrypt_First(enString, keyA);
        System.out.println("解密后的字串是：B" + DeStringB);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }


    // 解密m3u8 内容
    public static String decrypt2(byte[] sSrc, String sKey) {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("UTF8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//            byte[] encrypted1 = Base64Java.getDecoder().decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(sSrc);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
}

