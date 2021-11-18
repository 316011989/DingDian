import 'package:flutter/widgets.dart';
import 'http_manager.dart';
import 'log_interceptor.dart';

void main() {
  HttpManager().init(
    baseUrl: "https://api.1024renren.com/daxiang/",
    interceptors: [LogInterceptor()],
  );

  var params = {"page": 1, "word": "西游记"};
  var data = HttpManager()
      .getAsync(url: "v2/search/video", tag: "search", params: params);
  print(data);
}
