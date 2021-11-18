class Check {
  int code;
  String message;
  String data;
  String url;

  Check({this.code, this.message, this.data, this.url});

  Check.fromJson(Map<String, dynamic> json) {
    code = json['code'];
    message = json['message'];
    data = json['data'];
    url = json['url'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['code'] = this.code;
    data['message'] = this.message;
    data['data'] = this.data;
    data['url'] = this.url;
    return data;
  }
}
