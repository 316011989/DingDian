package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/4.
 */
data class VideoType(
    val code: Int, //1000
    val message: String, //ok
    val data: MutableList<TypeData>
) : Serializable

data class TypeData(
    val id: Int, //5
    val orders: Int, //9
    val name: String, //美丽人生
    val des: String, //66666666
    val icon: String, //http://wx.qlogo.cn/mmopen/vi_32/hbl8y4dicvmWcUkCjEDhY1qmictDE2UVG6aQauA4egb6vCK5FX5zlxzQFLufnpdrajSFX8K5qLgbgAB1hSibdZbKA/132
    val categories: MutableList<TypeCate>
) : Serializable

data class TypeCate(
    val id: Int,
    val orders: Int,
    val typeId: Int,
    val name: String,
    val des: String,
    val icon: String,
    val values: MutableList<TypeValue>
) : Serializable

data class TypeValue(
    val value: Int,
    val type: Int,
    val id: Int, //40
    val orders: Int, //-1
    val typeId: Int, //1
    val categoryId: Int, //4 TypeCate id
    val name: String, //免费
    val des: String, //免费
    val icon: String,
    var isSelected: Boolean
) : Serializable