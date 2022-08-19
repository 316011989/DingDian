package cn.yumi.daka.data.local.db.dao;

import androidx.room.*;
import cn.yumi.daka.data.local.db.entity.MovieHistoryEntity;

import java.util.List;

@Dao
public interface MovieHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistoryMovie(MovieHistoryEntity movieHistoryEntity);

    @Query("SELECT * FROM movie_history ORDER BY id DESC")
    List<MovieHistoryEntity> queryHistoryMovies();

    @Query("SELECT * FROM movie_history WHERE movidId = :movidId")
    MovieHistoryEntity getMovieById(Long movidId);

    @Delete
    void deleteHistoryMovie(MovieHistoryEntity movie);

    @Update()
    void update(MovieHistoryEntity movie);

    @Query("DELETE FROM movie_history")
    void deleteAll();

    @Query("DELETE FROM movie_history WHERE movidId IN (:list)")
    void deleteHistoryMovies(List<Long> list);

    @Query("SELECT COUNT(*) FROM movie_history")
    int getCount();
}
