package cn.video.star.data.local.db

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import cn.video.star.data.local.db.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDatabaseManager {

    private val dbName = "ymdy-db"

    private var database: AppDatabase? = null //room


    companion object {
        val dbManager = AppDatabaseManagerHolder.holder
    }

    private object AppDatabaseManagerHolder {
        val holder = AppDatabaseManager()
    }

    var task: AsyncTask<Void, Void, Void>? = null

    //创建数据库
    fun createDB(context: Context) {
        database = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .build()
    }

    fun insertVideoType(json: String) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    val entity = VideoTypeEntity(0, json)
                    database?.videoType()?.insertVideoType(entity)
                }
                return null
            }
        }.execute()
    }

    fun loadVideoType(callback: (VideoTypeEntity?) -> Unit) {
        object : AsyncTask<Void, Void, VideoTypeEntity>() {
            override fun doInBackground(vararg voids: Void): VideoTypeEntity? {
                var videType: VideoTypeEntity? = null
                database?.runInTransaction {
                    videType = database?.videoType()?.queryVideoType()
                }
                return videType
            }

            override fun onPostExecute(result: VideoTypeEntity?) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }


    fun insertWord(wordEntity: SearchWordEntity) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.searchWordDao()?.insertWord(wordEntity)
                }
                return null
            }
        }.execute()
    }

    fun querySearchWords(callback: (MutableList<SearchWordEntity>?) -> Unit) {
        object : AsyncTask<Void, Void, MutableList<SearchWordEntity>>() {
            override fun doInBackground(vararg voids: Void): MutableList<SearchWordEntity>? {
                var words: MutableList<SearchWordEntity>? = mutableListOf()
                database?.runInTransaction {
                    words = database?.searchWordDao()?.queryWords()!!
                }
                return words
            }

            override fun onPostExecute(result: MutableList<SearchWordEntity>?) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    fun querySearchWordsByWord(word: String, callback: (MutableList<SearchWordEntity>?) -> Unit) {
        object : AsyncTask<Void, Void, MutableList<SearchWordEntity>>() {
            override fun doInBackground(vararg voids: Void): MutableList<SearchWordEntity>? {
                var words: MutableList<SearchWordEntity>? = mutableListOf()
                database?.runInTransaction {
                    words = database?.searchWordDao()?.queryWordsByWord(word)!!
                }
                return words
            }

            override fun onPostExecute(result: MutableList<SearchWordEntity>?) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    fun deleteAllWords() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.searchWordDao()?.deleteAll()
                }
                return null
            }
        }.execute()
    }

    fun getWordCount(callback: (Int) -> Unit) {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg voids: Void): Int? {
                var count = 0
                database?.runInTransaction {
                    count = database?.searchWordDao()?.count!!
                }
                return count
            }

            override fun onPostExecute(result: Int) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    /**
     * 插入观看历史
     */
    fun insertMovie(movieEntity: MovieHistoryEntity) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.movieHistoryDao()?.insertMovie(movieEntity)
                }
                return null
            }
        }.execute()
    }

    /**
     * 查询观看历史
     */
    fun queryMovies(callback: (MutableList<MovieHistoryEntity>?) -> Unit) {
        object : AsyncTask<Void, Void, MutableList<MovieHistoryEntity>>() {
            override fun doInBackground(vararg voids: Void): MutableList<MovieHistoryEntity>? {
                var movies: MutableList<MovieHistoryEntity>? = mutableListOf()
                database?.runInTransaction {
                    movies = database?.movieHistoryDao()?.queryMovies()!!
                }
                return movies
            }

            override fun onPostExecute(result: MutableList<MovieHistoryEntity>?) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    fun deleteAllMovies() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.movieHistoryDao()?.deleteAll()
                }
                return null
            }
        }.execute()
    }

    fun deleteMovies(movieIds: MutableList<Long>) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.movieHistoryDao()?.deleteArray(movieIds)
                }
                return null
            }
        }.execute()
    }

    fun deleteMovie(movie: MovieHistoryEntity) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.movieHistoryDao()?.delete(movie)
                }
                return null
            }
        }.execute()
    }

    fun queryHistoryMovieById(movieId: Long, callback: (MovieHistoryEntity?) -> Unit) {
        object : AsyncTask<Void, Void, MovieHistoryEntity>() {
            override fun doInBackground(vararg voids: Void): MovieHistoryEntity? {
                var movie: MovieHistoryEntity? = null
                database?.runInTransaction {
                    movie = database?.movieHistoryDao()?.getMovieById(movieId)
                }
                return movie
            }

            override fun onPostExecute(result: MovieHistoryEntity?) {
                super.onPostExecute(result)
                callback(result)
            }

        }.execute()
    }

    fun getMovieCount(callback: (Int?) -> Unit) {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg voids: Void): Int? {
                var count = 0
                database?.runInTransaction {
                    count = database?.movieHistoryDao()?.count!!
                }
                return count
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    fun updateMovie(movieEntity: MovieHistoryEntity) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database?.runInTransaction {
                    database?.movieHistoryDao()?.update(movieEntity)
                }
                return null
            }
        }.execute()
    }


    /***缓存中心数据库操作函数***/

    /**
     * 插入缓存任务
     * 没有observer和callback,不处理插入数据库的结果
     */
    fun insertDownloadEpisode(entity: DownloadEpisodeEntity) {
        StaticTask {
            database?.runInTransaction {
                database?.downloadEpisodeDao()?.insertEpisode(entity)
            }
        }.execute()
    }

    /**
     * 根据集的id更新进度
     * m3u8类型,多线程下载ts任务,记录ts完成数量
     */
    fun updateEpisodeDownloadStatus(
        episodeId: Long, state: Int, speed: Long,
        successTsCount: Long, totalTsCount: Long, callback: (Int) -> Unit
    ) {
        StaticTask {
            database?.runInTransaction {
                //update数据库成功数量,暂时没有回调需要
                val count = database?.downloadEpisodeDao()
                    ?.updateEpisodeDownloadStatus(
                        episodeId, state, speed,
                        successTsCount, totalTsCount
                    )
                callback(count ?: 0)
            }
        }.execute()
    }

    /**
     * 删除指定某集
     */
    fun deleteEpisodeById(episodeId: Long, callback: (Int) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                database?.runInTransaction {
                    val count = database?.downloadEpisodeDao()?.deleteEpisodeById(episodeId)
                    callback(count ?: 0)
                }
            }
        }.execute()
    }

    /**
     * 删除所有缓存记录
     */
    fun deleteAllEpisode() {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()?.deleteAllEpisode()
            }
        }
    }


    /**
     * 查询所有某剧下集
     */
    fun queryAllEpisodesBySeriesId(
        seriesId: Long?,
        callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit
    ) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.downloadEpisodeDao()
                        ?.queryAllDownloadedEpisodes(seriesId = seriesId ?: 0)!!
                )
            }
        }.execute()
    }

    /**
     * 设置所有下载中任务为暂停状态
     */
    fun setDownloading2Pause(callback: (Int?) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                callback(database?.downloadEpisodeDao()?.updateEpisodeDownloadPause())
            }
        }.execute()
    }

    /**
     * 取未完成集,使用第一集模拟一个未完成的剧,代表所有未完成集
     * 取所有已经完成集,通过seriesid去重得到所有已完成的剧
     */
    fun queryEpisodeGroupbySeriesid(callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                //可能会有  最多一个下载中的剧,多个已完成的剧
                val list = mutableListOf<DownloadEpisodeEntity>()
                //未完成所有集
                val downloadingList =
                    database?.downloadEpisodeDao()?.queryNotFinishedEpisode()
                //取第一个来模拟成为一个未完成的剧
                if (!downloadingList.isNullOrEmpty()) {
                    val downloadingItem = downloadingList[0]
                    downloadingItem.seriesName = "正在缓存"
                    downloadingItem.count = downloadingList.size
                    list.add(downloadingItem)
                }
                //已完成所有剧(集通过groupid分组)
                val downloadedList =
                    database?.downloadEpisodeDao()?.queryDownloadedEpisodeGroupSeriesid()
                if (!downloadedList.isNullOrEmpty()) {
                    for (e in downloadedList) {
                        if (e.seriesId != null)
                            e.count =
                                database?.downloadEpisodeDao()?.queryCountBySeriesid(e.seriesId!!)!!
                    }
                }
                list.addAll(downloadedList!!)
                callback(list)
            }
        }.execute()
    }


    /**
     * 查询某剧下所有缓存完成的集
     */
    fun queryDownloadedEpisodeBySeriesid(
        seriesId: Long,
        callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit
    ) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.downloadEpisodeDao()
                        ?.queryDownloadedEpisodeBySeriesid(seriesId)!!
                )
            }
        }.execute()
    }


    /**
     * 检索所有未完成的集
     */
    fun queryNotFinishedEpisode(callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.downloadEpisodeDao()?.queryNotFinishedEpisode()
                )
            }
        }.execute()
    }

    /**
     * 查询 集
     */
    fun queryEpisodeById(episodeId: Long,callback: (movie: DownloadEpisodeEntity?) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.downloadEpisodeDao()
                        ?.queryEpisodeByEpisodeId(episodeId)
                )
            }
        }.execute()
    }


    /**
     * 查询所有收藏
     */
    fun queryAllCollect(callback: (movieList: MutableList<CollectEntity>?) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.collectDao()?.queryCollects()
                )
            }
        }.execute()
    }

    /**
     * 通过id查询收藏
     */
    fun queryCollectByMovieId(
        movieId: Long,
        callback: (movieList: MutableList<CollectEntity>?) -> Unit
    ) {
        StaticTask {
            database?.runInTransaction {
                callback(
                    database?.collectDao()?.queryCollectByMovieId(movieId)
                )
            }
        }.execute()
    }

    /**
     * 添加收藏
     */
    fun insertCollect(entity: CollectEntity) {
        StaticTask {
            database?.runInTransaction {
                database?.collectDao()?.insertCollect(entity)
            }
        }.execute()
    }

    /**
     * 取消收藏
     */
    fun deleteCollectByMovieId(movieId: Long, callback: (Int) -> Unit) {
        StaticTask {
            database?.runInTransaction {
                val count = database?.collectDao()?.deleteById(movieId)
                callback(count ?: 0)
            }
        }.execute()
    }

    /**
     * 防止出现warning
     * This AsyncTask class should be static or leaks might occur
     */
    private class StaticTask(val run: () -> Unit) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            run()
            return ""
        }
    }
}