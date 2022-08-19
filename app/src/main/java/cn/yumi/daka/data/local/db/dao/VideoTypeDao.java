package cn.yumi.daka.data.local.db.dao;

import cn.yumi.daka.data.local.db.entity.VideoTypeEntity;
import androidx.room.*;

@Dao
public interface VideoTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideoType(VideoTypeEntity videoType);

    @Query("select * from video_type limit 1")
    VideoTypeEntity queryVideoType();
}
