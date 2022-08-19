package  cn.yumi.daka.viewmodel

import android.app.Application
import androidx.lifecycle.*
import  cn.yumi.daka.data.DataRepository
import  cn.yumi.daka.data.remote.model.MovieListEntity

class ChannelFragmentViewModel(typeId: Int, valueIds: String, application: Application) :
    AndroidViewModel(application) {

    private var movieListData: LiveData<MovieListEntity>? = null
    private var loadMovieIndex: MutableLiveData<Int> = MutableLiveData() //页码
    private var typeIdData: MutableLiveData<Int> = MutableLiveData() //typeId
    private var valueIdsData: MutableLiveData<String> = MutableLiveData() //筛选ids

    init {
        typeIdData.value = typeId
        valueIdsData.value = valueIds
        //typeIdData、valueIdsData、loadMovieIndex 先赋值后订阅
        movieListData = Transformations.switchMap(
            loadMovieIndex
        ) { moviePage ->
            DataRepository.instance.getMovieListData(typeIdData.value!!, valueIdsData.value!!, moviePage)
        }
    }

    //获取movie列表数据
    fun getMovieListData(): LiveData<MovieListEntity>? {
        return movieListData
    }

    //刷新数据
    fun refreshData() {
        loadMovieIndex.value = 1
    }

    //加载下一页数据
    fun loadNextPageData() {
        if (loadMovieIndex.value == null) 1 else loadMovieIndex.value = loadMovieIndex.value!! + 1
    }

    //获取当前页码
    fun getCurrentPage(): Int? {
        return loadMovieIndex.value
    }



    //设置条件筛选
    fun setValueIdsData(valueIds: String) {
        valueIdsData.value = valueIds
    }


    class Factory(private val typeId: Int, private val valueIds: String, private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChannelFragmentViewModel(typeId, valueIds, mApplication) as T
        }
    }
}