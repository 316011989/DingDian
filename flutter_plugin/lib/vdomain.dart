class VDomain {
  String domain;
  String ip;
  List<String> iplist;

  VDomain({this.domain, this.ip, this.iplist});

  VDomain.fromJson(Map<String, dynamic> json) {
    domain = json['domain'];
    ip = json['ip'];
    iplist = json['iplist'].cast<String>();
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['domain'] = this.domain;
    data['ip'] = this.ip;
    data['iplist'] = this.iplist;
    return data;
  }
}
