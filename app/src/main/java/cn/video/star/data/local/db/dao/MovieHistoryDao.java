package cn.video.star.data.local.db.dao;

import androidx.room.*;
import cn.video.star.data.local.db.entity.MovieHistoryEntity;

import java.util.List;

@Dao
public interface MovieHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MovieHistoryEntity movieHistoryEntity);

    @Query("SELECT * FROM movie_history ORDER BY id DESC")
    List<MovieHistoryEntity> queryMovies();

    @Query("SELECT * FROM movie_history WHERE movidId = :movidId")
    MovieHistoryEntity getMovieById(Long movidId);

    @Delete
    void delete(MovieHistoryEntity movie);

    @Update()
    void update(MovieHistoryEntity movie);

    @Query("DELETE FROM movie_history")
    void deleteAll();

    @Query("DELETE FROM movie_history WHERE movidId IN (:list)")
    void deleteArray(List<Long> list);

    @Query("SELECT COUNT(*) FROM movie_history")
    int getCount();
}
