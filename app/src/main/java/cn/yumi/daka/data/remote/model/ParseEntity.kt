package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/9/12.
 */

data class ParseResponse(
        val code: Int, //0
        val message: String, //success
        val data: ParseEntity
) : Serializable

data class ParseEntity(
        val code: Int, //0
        val message: String, //success
        val cost: Double, //3.25
        val type: String, //vod
        val data: ParseData
) : Serializable

data class ParseData(
        val title: String, //下一站婚姻_05
        val duration: Double, //2704
        val streams: MutableList<ParseStream>
) : Serializable

data class ParseStream(
        val quality: String, //SD
        val type: String, //M3U8
        val segs: MutableList<ParseSeg>
) : Serializable

data class ParseSeg(
        val duration: Double, //2704.51
        val url: String //http://defaultts.tc.qq.com/defaultts.tc.qq.com/uwMRJfz-r5jAYaQXGdGnDNa47wxmj-p1Os_O5t_4amE/Z71njdxwATDPEvSrrRy32_EvBh_NjMuMXseocW1wlaPa4Yoh4x_E03HnnGm9ryZlWmVPxe3EG1bRak8dTPZyNReffYACJT8r07xo9MMuzJdhnAuEft-AD1d4DyoDQgPi0d1ymZeiqagBqlMtLijuP3kfgDOweJEL/m0016jiibjt.321001.ts.m3u8?ver=4
) : Serializable