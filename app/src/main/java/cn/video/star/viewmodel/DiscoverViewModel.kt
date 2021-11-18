package cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.Discover

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    fun getDiscoverFeed(page: Int, size: Int): MutableLiveData<Discover?>? {
        return DataRepository.instance.getDiscoverFeed(page, size)
    }

    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoverViewModel(mApplication) as T
        }
    }
}