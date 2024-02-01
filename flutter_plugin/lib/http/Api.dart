class Api {
  static String app = 'ymdy';
  static String store = 'web';
  static String p = "android";
  static String md5 = '';
  static String appVersion = '';
  static var configUrl = "http://config.19051024.com/"; //配置中心域名
  static var propath =
      "openapi/v1/envs/DEV/apps/moviefans/clusters/yumi430/namespaces/application/releases/latest";
  static var prottkoen = "88281a86f82626940a6a8ce9ff3591684a83786a";
  static var testpath =
      "openapi/v1/envs/DEV/apps/cn.video.giant/clusters/test/namespaces/application/releases/latest";
  static var testttkoen = "dd0b13ab9372d5c0ec398822edf2d172e7f9df88";

  static var toolHost = "http://play.qinwangtao.com";//工具服务器host
  static var playUrl = toolHost + "/parse/zdjx/playurl";//解析播放链接
  // static var playUrl = toolHost + "/parse/zdjx/playurl_test";//解析播放链接,测试header用
  static var checkUrl = toolHost + "/parse/zdjx/verifyapk";//检测apk md5信息是否正常
  static var ipUrl = toolHost + "/parse/myip";//获取本机ip地址
  static var dnsUrl = toolHost + "/parse/lookup/domain";//获取dns iplist
  static var website = "https://www.dingdian.vip";//官网

  static var DD = "api.ybliy.com";

  static var cKey = "8q(yd98^&76d7typ";
  static var dKey = "JA&sbV&X3J)vXAhn";

  static var domainList = <String>[];
}
