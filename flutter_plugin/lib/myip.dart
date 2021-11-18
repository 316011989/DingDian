class MyIp {
  int code;
  String message;
  Data data;

  MyIp({this.code, this.message, this.data});

  MyIp.fromJson(Map<String, dynamic> json) {
    code = json['code'];
    message = json['message'];
    data = json['data'] != null ? new Data.fromJson(json['data']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['code'] = this.code;
    data['message'] = this.message;
    if (this.data != null) {
      data['data'] = this.data.toJson();
    }
    return data;
  }
}

class Data {
  String myIP;
  List<String> v;

  Data({this.myIP, this.v});

  Data.fromJson(Map<String, dynamic> json) {
    myIP = json['myIP'];
    v = json['v'].cast<String>();
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['myIP'] = this.myIP;
    data['v'] = this.v;
    return data;
  }
}
