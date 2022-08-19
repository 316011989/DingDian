import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'check.dart';
import 'crypto/crypto.dart';
import 'http/Api.dart';
import 'http/crepto_util.dart';
import 'http/http_error.dart';
import 'http/http_manager.dart';
import 'iss.dart';
import 'myip.dart';
import 'playu.dart';
import 'utils.dart';
import 'vdomain.dart';

//enter flutter_module/.android ./gradlew assembleRelease
void main() {
  httpManager.init(
    baseUrl: "",
  );
  Api.app = 'ymdy';
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(visible: false),
    );
  }
}

typedef void MyCallback(result);

class MyHomePage extends StatefulWidget {
  final bool visible;

  MyHomePage({Key key, this.visible}) : super(key: key);

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String message = '';
  String gourl = Api.website;
  static const nativeChannel = const MethodChannel('com.example.message/mm1');

  Future<void> checkBase(MyCallback callback) async {
    Map<String, dynamic> result = {'message': 'ff'};
    try {
      String arguments = await nativeChannel.invokeMethod('jumpz', result);
      var path = arguments.split(' ')[0];
      if ('$path'.startsWith('/data/app/')) {
        final file = File('$path');
        var times = '${EncreptUtil.currentTimeMillis()}';
        Uint8List str = await file.readAsBytes();
        var md5Str = md5.convert(str).toString();
        var mmdd = Util.makeMd5('$md5Str');

        // mmdd = "23e9859e692c1480bb36b5e248550aec";

        print('checkBase:::mmdd:: $mmdd');
        Api.md5 = '$mmdd';
        var shakey = await EncreptUtil.makeHmacSha1Key(times);
        var data = '$times|${Api.app}|${Api.appVersion}|$mmdd';
        var sign = EncreptUtil.genHMACSHA1(data, shakey);
        var headers = Map<String, dynamic>();
        headers['sign'] = sign;
        headers['timestamp'] = times;
        headers['ver'] = Api.appVersion;
        headers['app'] = Api.app;
        Options options = Options();
        options.headers = headers;
        var params = Map<String, dynamic>();
        params['p'] = Api.p;
        params['v'] = Api.appVersion;
        params['app'] = Api.app;
        params['md5'] = mmdd;

        var response = await httpManager.postAsync(
            url: '${Api.checkUrl}',
            params: params,
            options: options,
            tag: 'apk');
        var _prefs = await SharedPreferences.getInstance();
        // print('checkBase:::headers:: $headers');
        // print('checkBase:::params:: $params');
        // print('checkBase:::url:: ${Api.checkUrl}');
        // print('checkBase:::response:: ${response.toString()}');
        if (response is HttpError) {
          nativeChannel.invokeMethod('cc100', result); //***************
          _prefs.setString("username", "");
          callback('${response.message}');
        } else {
          var check = Check.fromJson(response);
          if (check.code != 1000) {
            nativeChannel.invokeMethod('cc100', result); //***************
            if (check.url != null && "" != check.url) {
              gourl = check.url;
            }
            _prefs.setString("username", "");
            callback(check.message);
          } else {
            _prefs.setString("username", "check");
          }
        }
      }
    } on Exception catch (e) {
      print(e);
      nativeChannel.invokeMethod('cc100', result);
      callback('');
    }
  }

  Future<void> changeIp() async {
    var _prefs = await SharedPreferences.getInstance();
    _prefs.setString("ipp", "");
  }

  Future<void> clearDomain() async {
    var _prefs = await SharedPreferences.getInstance();
    _prefs.setString("username", "");
    _prefs.setString("ipp", "");
    var domains = _prefs.getStringList('domains');
    if (domains != null && domains.length > 0) {
      domains.forEach((domain) {
        _prefs.setString('$domain', "");
      });
    }
    _prefs.setStringList('domains', null);
  }

  Future<String> getMyIp() async {
    var ip = "";
    var path = "${Api.ipUrl}";
    var response = await httpManager.getAsync(url: path, tag: 'myip');
    var myip = MyIp.fromJson(response);
    if (myip.code == 1000) {
      ip = myip.data.myIP;
    }
    return ip;
  }

  Future<void> parseUrl(String arguments, MyCallback callback) async {
    var _prefs = await SharedPreferences.getInstance();
    var array = arguments.split(" ");
    var playId = int.parse(array[0]);
    var playUrl = array[1];
    var source = int.parse(array[2]);
    Api.appVersion = array[3];
    var body = EncreptUtil.makeBody(playId, playUrl, source);
    Options options = Options();
    options.headers = await EncreptUtil.makeHeaders();
    var params = Map<String, dynamic>();
    params['p'] = Api.p;
    params['v'] = Api.appVersion;
    params['s'] = Api.store;
    params['app'] = Api.app;
    params['d'] = 1;
    var user = _prefs.getString("username");
    //------------------------------------------//
    user = "check";
    print("parseUrl::: $user");
    print("parseUrl:::params= $params");
    print("parseUrl:::body= $body");
    print("parseUrl:::headers= ${options.headers}");
    print("parseUrl:::url= ${Api.playUrl}");
    //------------------------------------------//
    if (user != null && "" != user) {
      var response = await httpManager.postAsync(
          url: '${Api.playUrl}',
          data: json.encode(body),
          params: params,
          options: options,
          tag: 'parse');
      var check = Check.fromJson(response);
      print("parseUrl:::response= ${response}");
      if (check.code == 1000) {
        var dataJson = await EncreptUtil.decryptUrl(check.data);
        var playU = PlayU.fromJson(json.decode(dataJson));
        if (playU.items != null && playU.items.length > 0) {
          var headers = '';
          if (playU.items[0].playHeaders != null) {
            headers = '${json.encode(playU.items[0].playHeaders)}';
          }
          var result = {"playUrl": playU.items[0].url, "headers": '$headers'};
          print("parseUrl:::result ${result}");
          if (array.length == 5 && array[4].isNotEmpty)
            nativeChannel.invokeMethod('jx_download', result);
          else
            nativeChannel.invokeMethod('jx', result);
        }
      }
    }
  }

  Future<String> queryVlist(String domain) async {
    var _prefs = await SharedPreferences.getInstance();
    var url = "${Api.dnsUrl}?domain=$domain&app=${Api.app}";
    var response = await httpManager.getAsync(url: url, tag: 'Vlist');
    var check = Check.fromJson(response);
    var ip = "";
    if (check.code == 1000) {
      var data = check.data;
      if (data.contains("\n")) {
        data = data.replaceAll("\n", "");
      }
      //存储所有domain
      Api.domainList.add(domain);
//      print('value:: ${Api.domainList}');
      _prefs.setStringList('domains', Api.domainList);
      _prefs.setString("$domain", data);
      var jsonStr = await EncreptUtil.decryptIpStr(data);
      var vdomain = VDomain.fromJson(json.decode(jsonStr));
      if (vdomain.iplist != null && vdomain.iplist.length > 0) {
        ip = vdomain.iplist[0];
      }
    }
    return ip;
  }

  Future<String> readVlist(String domain) async {
    var _prefs = await SharedPreferences.getInstance();
    var data = _prefs.getString("$domain");
    var ip = "";
    if (data != null && data != "") {
      try {
        var jsonStr = await EncreptUtil.decryptIpStr(data);
        var domainObj = VDomain.fromJson(json.decode(jsonStr));
        if (domainObj.iplist != null && domainObj.iplist.length > 0) {
          ip = domainObj.iplist[0];
          _prefs.setString("ipp", ip);
        }
      } on Exception catch (e) {
        ip = await queryVlist(domain);
        _prefs.setString("ipp", ip);
      }
    } else {
      ip = await queryVlist(domain);
      _prefs.setString("ipp", ip);
    }
    return ip;
  }

  Future<String> getDns(String hostName) async {
    var _prefs = await SharedPreferences.getInstance();
    var path = "${Api.dnsUrl}?domain=$hostName&app=${Api.app}";
    // print("getDns:::url $path");
    var response = await httpManager.getAsync(url: path, tag: 'getDns');
    var check = Check.fromJson(response);
    var ip = "";
    if (check.code == 1000) {
      var data = check.data;
      if (data.contains("\n")) {
        data = data.replaceAll("\n", "");
      }
      _prefs.setString("${hostName}dns", data);
      var jsonStr = await EncreptUtil.decryptIpStr(data);
      var vdomain = VDomain.fromJson(json.decode(jsonStr));
      if (vdomain.iplist != null && vdomain.iplist.length > 0) {
        ip = vdomain.iplist[0];
      } else {
        ip = await readDns(hostName);
      }
    } else {
      ip = await readDns(hostName);
    }
    return ip;
  }

  Future<String> readDns(String hostName) async {
    var _prefs = await SharedPreferences.getInstance();
    var ip = "";
    var data = _prefs.getString("${hostName}dns");
    if (data != null && "" != data) {
      var jsonStr = await EncreptUtil.decryptIpStr(data);
      var vdomain = VDomain.fromJson(json.decode(jsonStr));
      if (vdomain.iplist != null && vdomain.iplist.length > 0) {
        ip = vdomain.iplist[0];
      } else {
        ip = await getDns(hostName);
      }
    } else {
      ip = await getDns(hostName);
    }
    return ip;
  }

  Future<String> initSS() async {
    var _prefs = await SharedPreferences.getInstance();
    var path = "${Api.configUrl}${Api.propath}";
    var token = "${Api.prottkoen}";
    // var path = "${Api.domainurl}${Api.testpath}";
    // var token = "${Api.testttkoen}";
    Options options = Options();
    var map = {"Authorization": "$token", "Content-Type": "application/json"};
    options.headers = map;
    var response =
        await httpManager.getAsync(url: path, options: options, tag: 'initSS');
    var str = json.encode(response).toString().replaceAll("&quot;", "\"");
    //保存到sharedpreference
    _prefs.setString("configs", "$str");
    var iss = Iss.fromJson(json.decode(str));
    if (iss.configurations != null) {
      for (var value in iss.configurations.appstate.versions) {
        if (value.version == Api.appVersion) {
          Api.toolHost = "${value.toolUrl}";
          Api.playUrl = "${value.toolUrl}/parse/zdjx/playurl"; //解析播放链接
          Api.checkUrl =
              "${value.toolUrl}/parse/zdjx/verifyapk"; //检测apk md5信息是否正常
          Api.ipUrl = "${value.toolUrl}/parse/myip"; //获取本机ip地址
          Api.dnsUrl = "${value.toolUrl}/parse/lookup/domain"; //获取dns iplist
        }
      }
      Api.website = iss.configurations.contactWay.webSite;
    }
    return str;
  }

  Future<String> readSS() async {
    initSS();
    var _prefs = await SharedPreferences.getInstance();
    var data = _prefs.getString("configs");
    if (data != null && "" != data) {
      var iss = Iss.fromJson(json.decode(data));
      if (iss.configurations != null) {
        for (var value in iss.configurations.appstate.versions) {
          if (value.version == Api.appVersion) {
            Api.toolHost = "${value.toolUrl}";
            Api.playUrl = "${value.toolUrl}/parse/zdjx/playurl"; //解析播放链接
            Api.checkUrl =
                "${value.toolUrl}/parse/zdjx/verifyapk"; //检测apk md5信息是否正常
            Api.ipUrl = "${value.toolUrl}/parse/myip"; //获取本机ip地址
            Api.dnsUrl = "${value.toolUrl}/parse/lookup/domain"; //获取dns iplist
          }
        }
        Api.website = iss.configurations.contactWay.webSite;
      }
      initSS();
      return data;
    } else {
      return await initSS();
    }
  }

  @override
  void initState() {
    super.initState();
    clearDomain();
    // initSS(); //测试配置中心测试
    // checkBase((result) {
    //   print('checkBase::: $result');
    // });//测试apk dex  md5校验
    nativeChannel.setMethodCallHandler((handler) {
      switch (handler.method) {
        case "home":
          checkBase((result) {
            setState(() {
              message = result;
            });
          });
          break;
        case "play":
          print('parseUrl::: ${handler.arguments}');
          String arguments = handler.arguments;
          parseUrl(arguments, (v) {});
          break;
        case "vlist":
          String arguments = handler.arguments;
          var host = arguments.split(' ')[0];
          var url = arguments.split(' ')[1];
          readVlist(host).then((ip) {
            var result = {'host': host, 'url': url};
            nativeChannel.invokeListMethod('vl', result);
          });
          break;
        case "changeip":
          // 切换播放ip,
          changeIp().then((value) => {
                parseUrl(handler.arguments, (v) {
//            print('v::: ${v}');
                })
              });
          break;
        case "netchange":
          // 切换ip,
          changeIp();
          break;
        case "cleard":
          clearDomain();
          break;
        case "myIp":
          getMyIp().then((ip) {
            var result = {'ip': ip};
            nativeChannel.invokeListMethod('ip', result);
          });
          break;
        case "getdns":
          String hostName = handler.arguments;
//          print("hostName:: ${hostName}");
          getDns(hostName).then((ip) {
            var result = {'dns': ip};
            nativeChannel.invokeListMethod('dns', result);
          });
          break;
        case "initSS":
          Api.appVersion = handler.arguments;
          readSS().then((ss) {
            var result = {'iss': ss};
            nativeChannel.invokeListMethod('iss', result);
          });
          break;
        default:
          break;
      }
      return null;
    });
  }

  void openUrl() {
    var result = {"gourl": gourl};
    nativeChannel.invokeListMethod('openUrl', result);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Container(
          decoration: BoxDecoration(
            color: Color(0xffffffff),
            borderRadius: BorderRadius.all(Radius.circular(5.0)),
          ),
          child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Text(
                  '提示',
                  style: TextStyle(
                    fontSize: 18.0,
                    fontWeight: FontWeight.bold,
                    color: Color(0xff222222),
                  ),
                ),
                Container(
                  margin: EdgeInsets.fromLTRB(10.0, 46.0, 10.0, 0.0),
                  child: Text(
                    '$message',
                    style: TextStyle(
                      fontSize: 13.0,
                      fontWeight: FontWeight.w600,
                      color: Color(0xff222222),
                    ),
                  ),
                ),
                Container(
                  margin: EdgeInsets.fromLTRB(0.0, 56.0, 0.0, 0.0),
                  child: RaisedButton(
                    color: Colors.blue,
                    highlightColor: Colors.blue[700],
                    splashColor: Colors.grey,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20.0)),
                    onPressed: openUrl,
                    child: Text(
                      "官网更新",
                      style: TextStyle(
                        fontSize: 15.0,
                        height: 1.2,
                        color: Colors.white,
                      ),
                    ),
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
