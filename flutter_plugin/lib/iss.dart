import 'dart:convert';

class Iss {
  Configurations configurations;

  Iss({
    this.configurations,
  });

  Iss.fromJson(Map<String, dynamic> json) {
    configurations = json['configurations'] != null
        ? new Configurations.fromJson(json['configurations'])
        : "";
  }
}

class Configurations {
  ContactWay contactWay; //联系方式
  Appstate appstate;
  String configCenterUrl; //配置中心主域名和备用域名

  Configurations(
      {this.contactWay, //联系方式
      this.appstate, //app分版本使用不同的工具域名
      this.configCenterUrl //配置中心主域名和备用域名
      });

  Configurations.fromJson(Map<String, dynamic> json) {
    contactWay =
        new ContactWay.fromJson(JsonCodec().decode(json["contactWay"])); //联系方式
    appstate = new Appstate.fromJson(JsonCodec().decode(json['appstate']));
    configCenterUrl = json["configCenterUrl"]; //配置中心主域名和备用域名
  }
}

class Appstate {
  List<Version> versions;

  Appstate({this.versions});

  Appstate.fromJson(Map<String, dynamic> json) {
    versions = new List();
    List<dynamic> jsonList = json["versions"];
    jsonList.forEach((element) {
      versions.add(Version.fromJson(element));
    });
  }
}

class Version {
  String version;
  String toolUrl;

  Version({
    this.version,
    this.toolUrl,
  });

  Version.fromJson(Map<String, dynamic> json) {
    version = json["version"];
    toolUrl = json["toolUrl"];
  }
}

class ContactWay {
  String webSite;

  ContactWay({
    this.webSite,
  });

  ContactWay.fromJson(Map<String, dynamic> json) {
    webSite = json["webSite"];
  }
}
