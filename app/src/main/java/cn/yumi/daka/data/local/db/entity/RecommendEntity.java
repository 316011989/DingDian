package cn.yumi.daka.data.local.db.entity;

import androidx.room.*;
import java.io.Serializable;

@Entity(tableName = "video_recommend")
public class RecommendEntity implements Serializable {

    @PrimaryKey
    private int id;

    private String json;

    public RecommendEntity(int id, String json) {
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
