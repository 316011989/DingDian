package cn.yumi.daka.data.local.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by android on 2018/5/8.
 * 视频下载列表
 */
@Entity(tableName = "movie_down", indices = {@Index(value = {"movidId"},
        unique = true)})
public class MovieDownloadEntity implements Serializable {

    @PrimaryKey
    private Long id;

    private Long movidId;

    private String name;

    private int percent;

    private int source; //来源

    private String sourceUrl;

    private String diskPath;

    private String cover;

    private long size;

    private int selected;

    private String datetime;

    private int count; //video数目


    public MovieDownloadEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovidId() {
        return this.movidId;
    }

    public void setMovidId(Long movidId) {
        this.movidId = movidId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPercent() {
        return this.percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDatetime() {
        return this.datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDiskPath() {
        return this.diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    //list<VideoDownloadEntity> ref
}
