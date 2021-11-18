package cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.LoginResponse
import java.util.*

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    fun getPhoneCode(params: WeakHashMap<String, String>): LiveData<Int>? {
        return DataRepository.instance.phoneCode(params)
    }

    fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>? {
        return DataRepository.instance.login(params)
    }


    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(mApplication) as T
        }
    }

}