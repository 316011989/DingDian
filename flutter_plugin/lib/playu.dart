class PlayU {
  List<Items> items;
  bool parseSuccess;
  int source;
  String sourceUrl;

  PlayU({this.items, this.parseSuccess, this.source, this.sourceUrl});

  PlayU.fromJson(Map<String, dynamic> json) {
    if (json['items'] != null) {
      items = new List<Items>();
      json['items'].forEach((v) {
        items.add(new Items.fromJson(v));
      });
    }
    parseSuccess = json['parseSuccess'];
    source = json['source'];
    sourceUrl = json['sourceUrl'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this.items != null) {
      data['items'] = this.items.map((v) => v.toJson()).toList();
    }
    data['parseSuccess'] = this.parseSuccess;
    data['source'] = this.source;
    data['sourceUrl'] = this.sourceUrl;
    return data;
  }
}

class Items {
  Map<String, dynamic>  playHeaders;
  String resolution;
  String url;

  Items({this.playHeaders, this.resolution, this.url});

  Items.fromJson(Map<String, dynamic> json) {
    playHeaders = json['playHeaders'];
    resolution = json['resolution'];
    url = json['url'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this.playHeaders != null) {
      data['playHeaders'] = this.playHeaders;
    }
    data['resolution'] = this.resolution;
    data['url'] = this.url;
    return data;
  }
}


