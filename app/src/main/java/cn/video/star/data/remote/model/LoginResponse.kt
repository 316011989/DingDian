package  cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/3/23.
 */

data class LoginResponse(
        val code: Int, //1000
        val message: String, //ok
        val data: LoginData
) : Serializable

data class LoginData(
        val phone: String, //18511697894
        val loginType: String, //mobile
        val name: String, //影迷OMGQVHF8
        val avatar: Any, //null
        val userid: Int, //1024
        val token: String, //eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDI0Iiwic2NvcGUiOlsiUk9MRV9VU0VSIl0sImRldmljZWlkIjoiMzUxODUyMDgwNjA3NzQ5IiwiZXhwIjoxNTI3OTMzODc4fQ.a6lbZ-0zUuiWf0se2BGlPZNTgGzcWhUIdHkZltNWPQ0
        val refreshToken: String //eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDI0Iiwic2NvcGUiOlsiUk9MRV9VU0VSIl0sImRldmljZWlkIjoiMzUxODUyMDgwNjA3NzQ5IiwianRpIjoiMzZiZDVhZDctMThlOC00MjBlLTkxYTMtM2Q1NTc2MGJiMjNkIiwiaWF0IjoxNTI1MzQxODc4LCJleHAiOjE1Mjc5MzM4Nzh9.0gfaIHnMoAClilYT4OllKqscJ8aJiy0cBF352reShxA
) : Serializable