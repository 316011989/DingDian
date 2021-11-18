
package cn.junechiu.junecore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5String {

    public static String getMd5(String str) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String crypt(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }

    /**
     * 获取文件的md5值
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static String getMd5(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0,
                    file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}

// public static String getMd5(String str) {
// MessageDigest md5 = null;
// try {
// md5 = MessageDigest.getInstance("MD5");
// } catch (Exception e) {
// e.printStackTrace();
// return "";
// }
//
// char[] charArray = str.toCharArray();
// byte[] byteArray = new byte[charArray.length];
//
// for (int i = 0; i < charArray.length; i++) {
// byteArray[i] = (byte) charArray[i];
// }
// byte[] md5Bytes = md5.digest(byteArray);
//
// StringBuffer hexValue = new StringBuffer();
//
// for (int i = 0; i < md5Bytes.length; i++) {
// int val = ((int) md5Bytes[i]) & 0xff;
// if (val < 16) {
// hexValue.append("0");
// }
// hexValue.append(Integer.toHexString(val));
// }
//
// return hexValue.toString();
// }
// private static String toMd5(byte[] bytes) {
// try {
// MessageDigest algorithm = MessageDigest.getInstance("MD5");
// algorithm.reset();
// algorithm.update(bytes);
// return toHexString(algorithm.digest(), "");
// } catch (NoSuchAlgorithmException e) {
// throw new RuntimeException(e);
// // 05-20 09:42:13.697: ERROR/hjhjh(256):
// // 5d5c87e61211ab7a4847f7408f48ac
// }
// }
//
// private static String toHexString(byte[] bytes, String separator) {
// StringBuilder hexString = new StringBuilder();
// for (byte b : bytes) {
// hexString.append(Integer.toHexString(0xFF & b)).append(separator);
// }
// return hexString.toString();
// }
