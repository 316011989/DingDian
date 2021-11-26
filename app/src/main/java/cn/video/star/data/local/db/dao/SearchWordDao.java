package cn.video.star.data.local.db.dao;

import androidx.room.*;
import cn.video.star.data.local.db.entity.SearchWordEntity;

import java.util.List;

@Dao
public interface SearchWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearchWord(SearchWordEntity wordEntity);

    @Query("SELECT * FROM search_word ORDER BY id DESC")
    List<SearchWordEntity> queryWords();

    @Query("SELECT * FROM search_word WHERE word =:word2")
    List<SearchWordEntity> queryWordsByWord(String word2);

    @Query("DELETE FROM search_word")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM search_word")
    int getCount();
}
