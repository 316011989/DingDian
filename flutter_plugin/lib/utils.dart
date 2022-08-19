import 'dart:convert';
import 'package:convert/convert.dart';
import 'crypto/src/md5.dart';

class Util {
  static String makeMd5(String data) {
    String key1 = getMD5(data);
    String key2 = getMD5("ymdy" + data + key1);
    return key2;
  }

  static String getMD5(String data) {
    var digest = md5.convert(utf8.encode(data));
    return hex.encode(digest.bytes);
  }
}