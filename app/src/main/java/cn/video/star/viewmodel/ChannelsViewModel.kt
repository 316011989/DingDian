package cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.VideoType

class ChannelsViewModel(application: Application) :
    AndroidViewModel(application) {

    fun getVideoType(): MutableLiveData<VideoType?>? {
        return DataRepository.instance.loadVideoType()
    }

    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChannelsViewModel(mApplication) as T
        }
    }

}