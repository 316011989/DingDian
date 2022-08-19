package cn.yumi.daka.data.local.db.dao

import androidx.room.*
import cn.yumi.daka.data.local.db.entity.CollectEntity


@Dao
interface CollectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollect(collectEntity: CollectEntity?)

    @Query("SELECT * FROM movie_collect")
    fun queryCollects(): MutableList<CollectEntity>?

    @Query("SELECT * FROM movie_collect WHERE movieId = :movidId")
    fun queryCollectByMovieId(movidId: Long?): MutableList<CollectEntity>?

    @Query("SELECT * FROM movie_collect WHERE movieId = :movidId")
    fun getCollectById(movidId: Long?): CollectEntity?

    @Delete
    fun delete(collect: CollectEntity?)

    @Query("DElETE FROM movie_collect WHERE  movieId = :movidId")
    fun deleteById(movidId: Long?): Int

    @Update
    fun update(collect: CollectEntity?)

    @Query("DELETE FROM movie_collect")
    fun deleteAll()

    @Query("DELETE FROM movie_collect WHERE movieId IN (:list)")
    fun deleteArray(list: List<Long?>?)

    @Query("SELECT COUNT(*) FROM movie_collect")
    fun getCount(): Int
}