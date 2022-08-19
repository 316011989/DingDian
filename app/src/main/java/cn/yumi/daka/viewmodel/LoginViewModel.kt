package cn.yumi.daka.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.model.LoginResponse
import java.util.*

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    fun getPhoneCode(params: WeakHashMap<String, String>): LiveData<Int>? {
        return DataRepository.instance.phoneCode(params)
    }

    fun login(params: WeakHashMap<String, String>): LiveData<LoginResponse>? {
        return DataRepository.instance.login(params)
    }

    fun wechatLogin(params: WeakHashMap<String, String>): MutableLiveData<LoginResponse?>? {
        return DataRepository.instance.wechatLogin(params)
    }

    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(mApplication) as T
        }
    }

}