package com.kk.taurus.playerbase.player;

import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.HashMap;

public class VideoSimpleCache {

    //保存多条缓存路径
    private HashMap<String, SimpleCache> cacheMap;

    private VideoSimpleCache() {
        cacheMap = new HashMap<>();
    }

    private static class SingletonHolder {
        private static VideoSimpleCache INSTANCE = new VideoSimpleCache();
    }

    public static VideoSimpleCache getInstance() {
        return VideoSimpleCache.SingletonHolder.INSTANCE;
    }

    public SimpleCache getSimpleCacheByVideoId(String videoId) {
        if (cacheMap != null && cacheMap.size() > 0 && cacheMap.containsKey(videoId)) {
            return cacheMap.get(videoId);
        } else {
            SimpleCache simpleCache = new SimpleCache(new File(AppFileUtils.getSimpleCacheDir(), videoId), new NoOpCacheEvictor());
            cacheMap.put(videoId, simpleCache);
            return simpleCache;
        }
    }
}
