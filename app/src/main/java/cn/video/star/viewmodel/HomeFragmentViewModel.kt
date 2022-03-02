package  cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.*
import  cn.video.star.data.DataRepository
import  cn.video.star.data.remote.model.HomeBanner
import  cn.video.star.data.remote.model.HomeTopic

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private var bannerData: LiveData<HomeBanner>? = null//首页滚动banner
    private val bannerState: MutableLiveData<Boolean> = MutableLiveData()//banner状态,变化时刷新banner

    private var feedData: LiveData<HomeTopic>? = null//首页动态播单
    private var feedIndex: MutableLiveData<Int> = MutableLiveData() //推荐数据页码
    private var feedSize = 2//每页播单

    init {
        //bannerState变化时请求数据
        bannerData = Transformations.switchMap(bannerState) {
            DataRepository.instance.loadTopicBanner()
        }
        //feedIndex变化时请求数据
        feedData = Transformations.switchMap(feedIndex) { input ->
            DataRepository.instance.loadRecommendFeed(input, feedSize)
        }
    }

    /**
     *刷新数据
     *  改变bannerState的value可自动变化banner数据
     *  改变loadFeedIndex的value可自动变化feed数据
     */
    fun refreshHomeData() {
        feedIndex.value = 1
        if (bannerState.value == null) bannerState.value = false else bannerState.value = !bannerState.value!!
    }

    /**
     * 获取数据
     */
    fun getHomeBanner(): LiveData<HomeBanner>? {
        return bannerData
    }

    /**
     * 获取数据
     */
    fun getHomeFeed(): LiveData<HomeTopic>? {
        return feedData
    }

    //加载下一页数据
    fun loadNextPageData() {
        if (feedIndex.value == null) feedIndex.value = 1 else feedIndex.value = feedIndex.value!! + 1
    }

    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeFragmentViewModel(mApplication) as T
        }
    }

}