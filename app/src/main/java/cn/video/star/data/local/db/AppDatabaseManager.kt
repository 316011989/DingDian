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
        MainScope().launch {
            withContext(Dispatchers.IO) {
                val entity = VideoTypeEntity(0, json)
                database?.videoType()?.insertVideoType(entity)
            }
        }
    }

    fun loadVideoType(callback: (VideoTypeEntity?) -> Unit) {
        MainScope().launch {
            val videType = withContext(Dispatchers.IO) {
                database?.videoType()?.queryVideoType()
            }
            callback(videType)
        }
    }


    fun insertSearchWord(wordEntity: SearchWordEntity) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.searchWordDao()?.insertSearchWord(wordEntity)
            }
        }
    }

    fun querySearchWords(callback: (MutableList<SearchWordEntity>?) -> Unit) {
        MainScope().launch {
            val words = withContext(Dispatchers.IO) {
                database?.searchWordDao()?.queryWords()
            }
            callback(words)
        }
    }

    fun querySearchWordsByWord(
        word: String,
        callback: (MutableList<SearchWordEntity>?) -> Unit
    ) {
        MainScope().launch {
            val words = withContext(Dispatchers.IO) {
                database?.searchWordDao()?.queryWordsByWord(word)
            }
            callback(words)
        }
    }

    fun deleteAllWords() {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.searchWordDao()?.deleteAll()
            }
        }
    }

    fun getWordCount(callback: (Int) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.searchWordDao()?.count
            }
            callback(count ?: 0)
        }
    }

    /**
     * 插入观看历史
     */
    fun insertHistoryMovie(movieEntity: MovieHistoryEntity) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.movieHistoryDao()?.insertHistoryMovie(movieEntity)
            }
        }
    }

    /**
     * 查询观看历史
     */
    fun queryHistoryMovies(callback: (MutableList<MovieHistoryEntity>?) -> Unit) {
        MainScope().launch {
            val movies = withContext(Dispatchers.IO) {
                database?.movieHistoryDao()?.queryHistoryMovies()
            }
            callback(movies)
        }
    }

    fun deleteHistoryMovies(movieIds: MutableList<Long>) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.movieHistoryDao()?.deleteHistoryMovies(movieIds)
            }
        }
    }

    fun deleteHistoryMovie(movie: MovieHistoryEntity) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.movieHistoryDao()?.deleteHistoryMovie(movie)
            }
        }
    }

    fun queryHistoryMovieById(
        movieId: Long,
        callback: (MovieHistoryEntity?) -> Unit
    ) {
        MainScope().launch {
            val movie = withContext(Dispatchers.IO) {
                database?.movieHistoryDao()?.getMovieById(movieId)
            }
            callback(movie)
        }
    }


    /***缓存中心数据库操作函数***/

    /**
     * 插入缓存任务
     * 没有observer和callback,不处理插入数据库的结果
     */
    fun insertDownloadEpisode(entity: DownloadEpisodeEntity) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()?.insertEpisode(entity)
            }
        }
    }

    /**
     * 根据集的id更新进度
     * m3u8类型,多线程下载ts任务,记录ts完成数量
     */
    fun updateEpisodeDownloadStatus(
        episodeId: Long, state: Int, speed: Long,
        successTsCount: Long, totalTsCount: Long, callback: (Int) -> Unit,
    ) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()
                    ?.updateEpisodeDownloadStatus(
                        episodeId, state, speed,
                        successTsCount, totalTsCount
                    )
            }
            callback(count ?: 0)
        }
    }

    /**
     * 删除指定某集
     */
    fun deleteEpisodeById(episodeId: Long, callback: (Int) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()?.deleteEpisodeById(episodeId)
            }
            callback(count ?: 0)
        }
    }

    /**
     * 删除所有下载记录
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
        callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit,
    ) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()
                    ?.queryAllDownloadedEpisodes(seriesId = seriesId ?: 0)!!
            }
            callback(count)
        }
    }

    /**
     * 设置所有下载中任务为暂停状态
     */
    fun setDownloading2Pause(callback: (Int?) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()?.updateEpisodeDownloadPause()
            }
            callback(count)
        }
    }

    /**
     * 取未完成集,使用第一集模拟一个未完成的剧,代表所有未完成集
     * 取所有已经完成集,通过seriesid去重得到所有已完成的剧
     */
    fun queryEpisodeGroupbySeriesid(callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit) {
        MainScope().launch {
            //可能会有  最多一个下载中的剧,多个已完成的剧
            val list = mutableListOf<DownloadEpisodeEntity>()
            withContext(Dispatchers.IO) {
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
            }
            callback(list)
        }
    }


    /**
     * 查询某剧下所有缓存完成的集
     */
    fun queryDownloadedEpisodeBySeriesid(
        seriesId: Long,
        callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit,
    ) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()
                    ?.queryDownloadedEpisodeBySeriesid(seriesId)!!
            }
            callback(count)
        }
    }


    /**
     * 检索所有未完成的集
     */
    fun queryNotFinishedEpisode(callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()?.queryNotFinishedEpisode()
            }
            callback(count)
        }
    }

    /**
     * 查询 集
     */
    fun queryEpisodeById(episodeId: Long, callback: (movie: DownloadEpisodeEntity?) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.downloadEpisodeDao()
                    ?.queryEpisodeByEpisodeId(episodeId)
            }
            callback(count)
        }
    }


    /**
     * 查询所有收藏
     */
    fun queryAllCollect(callback: (movieList: MutableList<CollectEntity>?) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.collectDao()?.queryCollects()
            }
            callback(count)
        }
    }

    /**
     * 通过id查询收藏
     */
    fun queryCollectByMovieId(
        movieId: Long,
        callback: (movieList: MutableList<CollectEntity>?) -> Unit,
    ) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.collectDao()?.queryCollectByMovieId(movieId)
            }
            callback(count)
        }
    }

    /**
     * 添加收藏
     */
    fun insertCollect(entity: CollectEntity) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                database?.collectDao()?.insertCollect(entity)
            }
        }
    }

    /**
     * 取消收藏
     */
    fun deleteCollectByMovieId(movieId: Long, callback: (Int) -> Unit) {
        MainScope().launch {
            val count = withContext(Dispatchers.IO) {
                database?.collectDao()?.deleteById(movieId)
            }
            callback(count ?: 0)
        }
    }

}