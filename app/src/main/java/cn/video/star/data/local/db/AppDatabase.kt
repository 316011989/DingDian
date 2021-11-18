package cn.video.star.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import cn.video.star.data.local.db.dao.*
import cn.video.star.data.local.db.entity.*
import androidx.room.*
import androidx.room.migration.Migration

@Database(
    entities = [VideoTypeEntity::class, SearchWordEntity::class, MovieHistoryEntity::class, DownloadEpisodeEntity::class, CollectEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoType(): VideoTypeDao
    abstract fun searchWordDao(): SearchWordDao
    abstract fun movieHistoryDao(): MovieHistoryDao
    abstract fun downloadEpisodeDao(): DownloadEpisodeDao//缓存集
    abstract fun collectDao(): CollectDao//缓存集
}

//版本1到版本2
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
//        //创建表
//        database.execSQL("CREATE TABLE search_word_temp (id INTEGER, word TEXT, PRIMARY KEY(id), UNIQUE(word))")
//        //复制表
//        database.execSQL("INSERT INTO search_word_temp (id, word) SELECT id, word FROM search_word")
//        //删除表
//        database.execSQL("DROP TABLE search_word")
//        //修改表名称
//        database.execSQL("ALTER TABLE search_word_temp RENAME TO search_word")
        //添加字段
        database.execSQL("ALTER TABLE video_down  ADD COLUMN downLength INTEGER  NOT NULL DEFAULT 0")
        //添加字段
        database.execSQL("ALTER TABLE video_down  ADD COLUMN rate TEXT")
    }
}

//版本2到版本3
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //添加字段
        database.execSQL("ALTER TABLE video_down  ADD COLUMN host TEXT")
    }
}

//版本3到版本4
val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //删除表原下载功能的"剧"表
        database.execSQL("DROP TABLE movie_down")
        //删除表原下载功能的"集"表
        database.execSQL("DROP TABLE video_down")

        //创建新表,下载
        database.execSQL(
            "CREATE TABLE download_tv_episode (episodeId INTEGER, episodeName TEXT," +
                    "seriesId INTEGER,seriesName TEXT, img TEXT,playUrl TEXT,headers TEXT,downloadPrograss INTEGER," +
                    "downloadStatus INTEGER,source INTEGER ,sourceIsVip INTEGER ,playId INTEGER ,videoId INTEGER ," +
                    "rate TEXT DEFAULT '1',vType INTEGER ,episode INTEGER , speed INTEGER , successTsCount INTEGER ," +
                    "totalTsCount INTEGER, PRIMARY KEY(episodeId), UNIQUE(episodeId))"
        )
    }
}

//版本4到版本5
val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //创建新表,收藏
        database.execSQL(
            "CREATE TABLE movie_collect (id INTEGER, movieId INTEGER," +
                    "name TEXT,percent INTEGER NOT NULL, cover TEXT,selected INTEGER NOT NULL,datetime TEXT,esp TEXT," +
                    "playIndex INTEGER NOT NULL,position INTEGER NOT NULL, PRIMARY KEY(movieId), UNIQUE(movieId))"
        )
    }
}


//版本2到版本3
val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //添加字段
        database.execSQL("ALTER TABLE movie_history  ADD COLUMN source INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE movie_collect  ADD COLUMN source INTEGER NOT NULL DEFAULT 0")
    }
}