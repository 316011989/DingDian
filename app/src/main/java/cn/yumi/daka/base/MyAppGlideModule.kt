package  cn.yumi.daka.base

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.request.RequestOptions


/**
 * Created by android on 2018/10/9.
 */

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
//        val bitmapPoolSizeBytes = 1024 * 1024 * 0 // 0mb
//        val memoryCacheSizeBytes = 1024 * 1024 * 0 // 0mb
//        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))
//        builder.setBitmapPool(LruBitmapPool(bitmapPoolSizeBytes))

        //设置磁盘缓存大小
        val sizeOut = 1024 * 1024 * 1024  //1GB
        //设置磁盘缓存 外部
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context, sizeOut.toLong()))

//        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20mb
//        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
//        val bitmapPoolSizeBytes = 1024 * 1024 * 30 // 30mb
//        builder.setBitmapPool(LruBitmapPool(bitmapPoolSizeBytes.toLong()))
    }
}

