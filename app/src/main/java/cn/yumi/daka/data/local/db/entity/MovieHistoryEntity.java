package cn.yumi.daka.data.local.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by android on 2018/5/8.
 */
@Entity(tableName = "movie_history", indices = {@Index(value = {"movidId"},
        unique = true)})
public class MovieHistoryEntity implements Serializable {

    @PrimaryKey
    private Long id;

    private Long movidId;

    private String name;

    private int percent;

    private String cover;

    private int selected;

    private String datetime;

    private int source;//来源

    private String esp; //剧集

    private int playIndex; //剧集索引

    private long position;//播放进度

    public MovieHistoryEntity() {
    }


    public Long getMovidId() {
        return this.movidId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public String getDatetime() {
        return this.datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public long getPosition() {
        return this.position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getEsp() {
        return this.esp;
    }

    public void setEsp(String esp) {
        this.esp = esp;
    }

    public int getPlayIndex() {
        return this.playIndex;
    }

    public void setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
