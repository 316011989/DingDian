import 'dart:convert';

import 'package:convert/convert.dart';
import 'package:crypto/crypto.dart' as crypto;
import 'package:cryptography/cryptography.dart';
import 'package:encrypt/encrypt.dart';
import 'package:random_string/random_string.dart';

import '../utils.dart';
import 'Api.dart';

class EncreptUtil {
  ///解析视频播放链接用到的headers
  static String timestamp = '';

  static Future<Map<String, dynamic>> makeHeaders() async {
    var headers = Map<String, dynamic>();
    timestamp = currentTimeMillis(); //时间戳
    var rand = randomAlphaNumeric(10); //字母数字的随机10位字符串

    var hMacSha1key = await makeHmacSha1Key(timestamp);
    headers["Content-Type"] = "application/json"; //设定请求参数为json格式
    headers["rand"] = rand;
    headers["app"] = Api.app;
    headers["timestamp"] = timestamp;
    headers["md5"] = Api.md5;
    headers["nonce"] =
        genHMACSHA1(("$timestamp|$rand"), hMacSha1key); //通过时间戳和随机字符串生成nonce
    headers["sign"] = genHMACSHA1(
        "${Uri.parse(Api.playUrl).path}|$timestamp|${Api.appVersion}|${headers["nonce"]}|${Api.app}|${Api.store}|${Api.md5}",
        hMacSha1key);
    return headers;
  }

  ///解析视频播放链接用到的body,json类型
  static Map<String, dynamic> makeBody(num playId, String playUrl, num source) {
    var body = Map<String, dynamic>();
    body["url"] = playUrl;
    body["source"] = source;
    body["videoid"] = playId;
    return body;
  }

  ///使用md5加密生成的md5key进行chacha20加密生成aeskey,再使用aeskey进行aes加密生成hmacsha1的key
  static Future<String> makeHmacSha1Key(String timestamp) async {
    String key = Util.makeMd5("$timestamp${Api.app}");
    String aesKey = "$timestamp${Api.app}";

    ///dart chacha20
    while (aesKey.length < 16) {
      var encrptResult = await chacha20.encrypt(utf8.encode(aesKey),
          secretKey: SecretKey(utf8.encode(key)),
          nonce: Nonce(utf8.encode(key.substring(0, 12))));
      aesKey = base64Encode(encrptResult);
    }

    print("aeskey chacha20 result === $aesKey");

    try {
      List<int> raw = utf8.encode(aesKey.substring(0, 16));
      final key = Key.fromBase64(base64Encode(raw));
      final encrypter = Encrypter(AES(key, mode: AESMode.cbc));
      List<int> zeroiv = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
      final encrypted = encrypter.encrypt("${Api.app}",
          iv: IV.fromBase64(base64Encode(zeroiv)));
      return hex
          .encode(encrypted.bytes)
          .replaceAll(new RegExp(r"\s+\b|\b\s"), "");
    } catch (err) {
      print("aes encode error:$err");
      return "";
    }
  }

  ///hmacsha1加密
  static String genHMACSHA1(String data, String hMacSha1key) {
    var hmacSha1 =
        new crypto.Hmac(crypto.sha1, utf8.encode(hMacSha1key)); // HMAC-SHA1
    var digest = hmacSha1.convert(utf8.encode(data));
    return digest.toString().replaceAll(new RegExp(r"\s+\b|\b\s"), "");
  }

  static String currentTimeMillis() {
    return (DateTime.now().millisecondsSinceEpoch ~/ 1000).toString();
  }

  ///对播放链接解析结果的加密数据进行解密
  static Future<String> decryptUrl(String encyptStr) async {
    if (encyptStr.contains("\n")) {
      encyptStr = encyptStr.replaceAll("\n", "");
    }
    var keyHalf2 = encyptStr.substring(encyptStr.length - 8);
    encyptStr = encyptStr.substring(0, encyptStr.length - 8);
    var keyHalf1 = await getAesKey();
    keyHalf1 = keyHalf1.substring(0, 8);
    final key = Key.fromUtf8(keyHalf1 + keyHalf2);
    final aes = Encrypter(AES(key, mode: AESMode.cbc));
    var result = aes.decrypt64(encyptStr, iv: IV.fromUtf8(keyHalf2 + keyHalf2));
    return result;
  }

  ///取得aes解密需要的key
  static Future<String> getAesKey() async {
    String key = Util.makeMd5(
        '${timestamp}${Api.app}${Api.store}${Api.appVersion}'); // key=md5(A+B)
    return key;
  }

  ///对ip地址进行aes解密
  static Future<String> decryptIpStr(String ipStr) async {
    if (ipStr.contains("\n")) {
      ipStr = ipStr.replaceAll("\n", "");
    }
    final key = Key.fromUtf8(Api.dKey);
    final aes = Encrypter(AES(key, mode: AESMode.cbc));
    List<int> zeroiv = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    var result = aes.decrypt64(ipStr, iv: IV.fromBase64(base64Encode(zeroiv)));
    return result;
  }

  ///配置中心正式环境的加密数据进行解密
  static String decryptConfigurations(String enStr) {
    if (enStr.contains("\n")) {
      enStr = enStr.replaceAll("\n", "");
    }
    final key = Key.fromUtf8(Api.cKey);
    final aes = Encrypter(AES(key, mode: AESMode.cbc));
    List<int> iv = utf8.encode("DJ@#dd'a'asl;dfk");
    var result = aes.decrypt64(enStr, iv: IV.fromBase64(base64Encode(iv)));
    return result;
  }

  static String encodeConfigurations(String enStr) {
    final key = Key.fromUtf8(Api.cKey);
    final aes = Encrypter(AES(key, mode: AESMode.cbc));
    List<int> iv = utf8.encode("DJ@#dd'a'asl;dfk");
    var result = aes.encrypt(enStr, iv: IV.fromBase64(base64Encode(iv)));
    return result.base64;
  }

  static String decryptVlist(String enStr) {
    final key = Key.fromUtf8(Api.dKey);
    final aes = Encrypter(AES(key, mode: AESMode.cbc));
    List<int> iv = utf8.encode("DJ@#dd'a'asl;dfk");
    var result = aes.decrypt64(enStr, iv: IV.fromBase64(base64Encode(iv)));
    return result;
  }
}
