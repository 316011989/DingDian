/*
 * Copyright 2017 jiajunhui<junhui_jia@163.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.kk.taurus.playerbase.entity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.text.TextUtils;
import com.kk.taurus.playerbase.provider.IDataProvider;

/**
 * Created by Taurus on 2018/3/17.
 * <p>
 * if you have video url ,please set it to data field.{@link DataSource#data}
 * <p>
 * if you use DataProvider{@link IDataProvider},According to yourself needs,
 * set them to the corresponding fields. For example, you need to use a long id
 * to get playback address, you can set the long id to id field{@link DataSource#id}.
 */

public class DataSource implements Serializable {

    private String assetsPath;

    /**
     * extension field, you can use it if you need.
     */
    private String tag;

    private int type; //剧集、电影

    private int from; //来源

    private List<Object> espList;

    private String host;

    //清晰度
    private List<String> clarities = new ArrayList<>();

    /**
     * extension field, you can use it if you need.
     */
    private String sid;

    /**
     * Usually it's a video url.
     */
    private String data;

    /**
     * you can set video name to it.
     */
    private String title;

    /**
     * extension field, you can use it if you need.
     */
    private long id;

    /**
     * if you want set uri data,you can use this filed.
     */
    private Uri uri;

    /**
     * timed text source for video
     */
    private TimedTextSource timedTextSource;

    /**
     * if you want set some data to decoder
     * or some extra data, you can set this field.
     */
    private HashMap<String, String> extra;

    //a FileDescriptor data source, maybe you need.
    private FileDescriptor fileDescriptor;

    //AssetFileDescriptor data source, you can use it to play files in assets dir.
    private AssetFileDescriptor assetFileDescriptor;

    //when play android raw resource, set this.
    private int rawId = -1;

    /**
     * If you want to start play at a specified time,
     * please set this field.
     */
    private int startPos;

    private boolean isLive;

    public DataSource() {
    }

    public DataSource(String data) {
        this.data = data;
    }

    public DataSource(String tag, String data) {
        this.tag = tag;
        this.data = data;
    }

    public String getAssetsPath() {
        return assetsPath;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public HashMap<String, String> getExtra() {
        return extra;
    }

    public void setExtra(HashMap<String, String> extra) {
        this.extra = extra;
    }

    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(FileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public AssetFileDescriptor getAssetFileDescriptor() {
        return assetFileDescriptor;
    }

    public void setAssetFileDescriptor(AssetFileDescriptor assetFileDescriptor) {
        this.assetFileDescriptor = assetFileDescriptor;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean isLive) {
        this.isLive = isLive;
    }

    public int getRawId() {
        return rawId;
    }

    public void setRawId(int rawId) {
        this.rawId = rawId;
    }

    public static Uri buildRawPath(String packageName, int rawId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + rawId);
    }

    public TimedTextSource getTimedTextSource() {
        return timedTextSource;
    }

    public void setTimedTextSource(TimedTextSource timedTextSource) {
        this.timedTextSource = timedTextSource;
    }

    public List<Object> getEspList() {
        return espList;
    }

    public void setEspList(List<Object> espList) {
        this.espList = espList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public List<String> getClarities() {
        return clarities;
    }

    public void setClarities(List<String> clarities) {
        this.clarities = clarities;
    }

    public static Uri buildAssetsUri(String assetsPath) {
        return Uri.parse("file:///android_asset/" + assetsPath);
    }

    public static AssetFileDescriptor getAssetsFileDescriptor(Context context, String assetsPath) {
        try {
            if (TextUtils.isEmpty(assetsPath))
                return null;
            return context.getAssets().openFd(assetsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
