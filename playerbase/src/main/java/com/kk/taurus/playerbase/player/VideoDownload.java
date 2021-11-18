package com.kk.taurus.playerbase.player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.kk.taurus.playerbase.utils.MD5Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class VideoDownload {

    private Context mContext;
    protected String userAgent;
    private Timer timer;

    private VideoDownload() {
    }

    public static final class Holder {
        public static final VideoDownload INSTANCE = new VideoDownload();
    }

    public static final VideoDownload getInstance() {
        return VideoDownload.Holder.INSTANCE;
    }

    public void initDownload(Context pContext) {
        mContext = pContext;
        userAgent = Util.getUserAgent(pContext, "ExoPlayerDemo");
    }

    public void download(String url, long videoId) {
        SimpleCache simpleCache = VideoSimpleCache.getInstance()
                .getSimpleCacheByVideoId(MD5Utils.encode(String.valueOf(videoId)),mContext);
        DataSource.Factory okHttpDataSourceFactory = new
                OkHttpDataSourceFactory(OkHttpClientFactory.getInstance().getOkHttpClient(),
                Util.getUserAgent(mContext, "exo"));
        DownloaderConstructorHelper downloaderConstructorHelper = new DownloaderConstructorHelper(
                simpleCache, okHttpDataSourceFactory);
        Uri uri = Uri.parse(url);
        HlsDownloader downloader = new HlsDownloader(uri, new ArrayList<>(), downloaderConstructorHelper);
        new Thread(() -> {
            try {
                downloader.download();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        startTimer(downloader);
    }

    public void startTimer(HlsDownloader downloader) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("downloader--", "per: " + downloader.getDownloadPercentage() +
                        "  downloaded: " + downloader.getDownloadedBytes());
            }
        }, 0, 1000);
    }
}
