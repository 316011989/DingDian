package  cn.video.star.data.local.db.entity;

import androidx.room.*;
import java.io.Serializable;

/**
 * Created by android on 2018/3/22.
 */

@Entity(tableName = "user_info")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 536872108;

    @PrimaryKey
    public long userid;

    public String token;

    public String name;

    public String avatar;

    public int isVip;

    public String role;

    public String phone;

    public String refreshToken;


    public UserEntity() {
    }

    public long getUserid() {
        return this.userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getIsVip() {
        return this.isVip;
    }

    public void setIsVip(int isVip) {
        this.isVip = isVip;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}