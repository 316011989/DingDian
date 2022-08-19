package cn.yumi.daka.data.local.db.dao;

import androidx.room.*;
import cn.yumi.daka.data.local.db.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity userEntity);

    @Query("select * from user_info limit 1")
    UserEntity queryUser();

    @Delete
    void delete(UserEntity user);

    @Update()
    void updateUser(UserEntity user);

    @Query("DELETE FROM user_info")
    void deleteAll();
}
