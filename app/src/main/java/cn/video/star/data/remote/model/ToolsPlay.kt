package cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/9/10.
 */

data class ToolsPlay(
        val code: Int, //1000
        val message: String, //ok
        val data: Data
) : Serializable

data class Data(
        val msg: String, //200
        val ext: String, //m3u8
        val site: String, //mgtv
        val url: String //http%3A%2F%2Fpcvideogs.titan.mgtv.com%2Fc1%2F2018%2F09%2F04_0%2F85E69E09DBCE21DD6C269202A2173CBB_20180904_1_1_1222_mp4%2F25173B246C7C096B3AAF9D845B10D49F.m3u8%3Farange%3D0%26pm%3DgEwD5gPPXJN5BvHCtUSczpUGjMY6ty6PTKrpBDQeSUi6x4L1jGt7YolWmVA22rAqWTb7nmlOsX03vchEf4gsSryML4dakAAaS9TPCMnW5RYrCQzRDeyc~FoRLS_K~CKybP5OXeDxWCzmtdAHW7CxUVPM1K8lS41saS9dF0F7GAaAUsOKWwrN_DWa54AynsobFKT6AYpQdgBixZtPwd_jW8CkH_dTtGk2uKwZSxTlPB6lfpAtDK7kliCNL9h89aqy0uMcTUrbp_vHnMdYxO8x690UxSLd4nZd~hDXSMCT8qJgTm7dIBlLeNz6CitLHH0sadaHQ4NmG4pAj3OEu~CfCKTAyxuOBCzx0F22kiyoFbpU5SvRypQDb2H0Mpiq1kQs2Bx2EdJhWZQlZIW0l8vQSQ--%26vcdn%3D0%26scid%3D25012
) : Serializable