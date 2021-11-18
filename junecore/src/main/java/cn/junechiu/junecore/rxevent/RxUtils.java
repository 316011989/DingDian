
package cn.junechiu.junecore.rxevent;

import java.util.Map;

public class RxUtils {

    public static boolean isEmpty(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(Map map, Object key) {
        return map.containsKey(key);
    }
}
