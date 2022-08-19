package cn.yumi.daka.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.model.VideoType

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