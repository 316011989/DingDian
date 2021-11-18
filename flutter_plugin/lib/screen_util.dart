import 'dart:ui';

class ScreenUtil {
  // 屏幕宽
  static double get widthPixels => window.physicalSize.width;

  // 屏幕高
  static double get heightPixels => window.physicalSize.height;

  // 屏幕密度
  static double get density => window.devicePixelRatio;

  /**
   * dp转px
   * @param dpValue dp
   * @return int px
   * @throws
   */
  static double dp2px(double dpValue) {
    return dpValue * density + 0.5;
  }

  /**
   * px 转 dp
   *
   * @param pxValue px
   * @return int dp
   * @throws
   */
  static double px2dp(double pxValue) {
    return pxValue / density + 0.5;
  }
}
