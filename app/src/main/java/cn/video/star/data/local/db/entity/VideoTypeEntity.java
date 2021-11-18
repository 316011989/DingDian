package cn.video.star.data.local.db.entity;

import androidx.room.*;
import java.io.Serializable;

@Entity(tableName = "video_type")
public class VideoTypeEntity implements Serializable {

    @PrimaryKey
    private int id;

    private String json;

    public VideoTypeEntity(int id, String json) {
        this.json = json;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
