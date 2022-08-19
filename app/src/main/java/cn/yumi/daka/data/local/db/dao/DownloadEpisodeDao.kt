package cn.yumi.daka.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity

/**
 * 下载功能 剧 表操作语句
 */
@Dao
interface DownloadEpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisode(entity: DownloadEpisodeEntity)

    /**
     * 查询所有未完成集 按剧分组
     */
    @Query("SELECT * FROM download_tv_episode  WHERE downloadStatus NOT LIKE 4")
    fun queryNotFinishedEpisode(): MutableList<DownloadEpisodeEntity>?

    /**
     * 查询所有已完成集 按剧分组
     */
    @Query("SELECT * FROM download_tv_episode WHERE downloadStatus  LIKE 4 GROUP BY seriesId ORDER BY seriesId DESC ")
    fun queryDownloadedEpisodeGroupSeriesid(): MutableList<DownloadEpisodeEntity>

    /**
     * 查询某剧已完成集数量
     */
    @Query("SELECT count(*) FROM download_tv_episode WHERE seriesId = :seriesId AND  downloadStatus  LIKE 4")
    fun queryCountBySeriesid(seriesId: Long): Int

    /**
     * 更新某集下载状态和进度
     * mp4等大文件任务,单线程下载
     */
    @Query("UPDATE download_tv_episode SET downloadStatus = :state , downloadPrograss = :prograss,speed=:speed WHERE episodeId = :episodeId")
    fun updateEpisodeDownloadStatus(episodeId: Long, state: Int, prograss: Int, speed: Int): Int

    /**
     * 更新某集下载状态和进度
     * m3u8类型,多线程下载ts任务,记录ts完成数量
     */
    @Query("UPDATE download_tv_episode SET downloadStatus = :state , speed = :speed,successTsCount=:successTsCount,totalTsCount=:totalTsCount WHERE episodeId = :episodeId")
    fun updateEpisodeDownloadStatus(
        episodeId: Long, state: Int, speed: Long,
        successTsCount: Long, totalTsCount: Long
    ): Int


    /**
     * 查询指定某集
     */
    @Query("SELECT * FROM download_tv_episode  WHERE episodeId = :episodeId")
    fun queryEpisodeByEpisodeId(episodeId: Long): DownloadEpisodeEntity?

    /**
     * 查询某剧下所有缓存完成集
     */
    @Query("SELECT * FROM download_tv_episode  WHERE  downloadStatus  LIKE 4 AND seriesId = :seriesId")
    fun queryDownloadedEpisodeBySeriesid(seriesId: Long): MutableList<DownloadEpisodeEntity>

    /**
     * 查询某剧下所有集
     */
    @Query("SELECT * FROM download_tv_episode   WHERE seriesId = :seriesId ORDER BY seriesId DESC ")
    fun queryAllDownloadedEpisodes(seriesId: Long): MutableList<DownloadEpisodeEntity>

    /**
     * 启动app时设置所有集为暂停状态
     * 因为上次关闭应用可能有正在缓存的任务
     */
    @Query("UPDATE download_tv_episode SET downloadStatus = 3 WHERE downloadStatus = 0 OR downloadStatus = 1 OR downloadStatus = 2")
    fun updateEpisodeDownloadPause(): Int

    /**
     * 删除某一集
     */
    @Query("DElETE FROM download_tv_episode WHERE  episodeId = :episodeId")
    fun deleteEpisodeById(episodeId: Long): Int

    /**
     * 删除所有集
     */
    @Query("DElETE FROM download_tv_episode")
    fun deleteAllEpisode()

    /**
     * 查询所有集
     */
    @Query("SELECT * FROM download_tv_episode")
    fun queryAllDownloadedEpisode(): MutableList<DownloadEpisodeEntity>


}