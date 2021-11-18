package cn.video.star.data.remote.model

import java.io.Serializable

class ClarityModel(
    val rate: MutableList<Clarity>?
) : Serializable {
    class Clarity(
        val id: String,
        val text: String,
        val pre: String,
        val desc: String,
        val order: String
    ) : Serializable
}