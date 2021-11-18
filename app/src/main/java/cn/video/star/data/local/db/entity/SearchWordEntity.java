package cn.video.star.data.local.db.entity;

import androidx.room.*;
import java.io.Serializable;

/**
 * Created by android on 2018/5/8.
 */
@Entity(tableName = "search_word")
public class SearchWordEntity implements Serializable {

    @PrimaryKey
    private Long id;

    private String word;

    public SearchWordEntity() {
    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
