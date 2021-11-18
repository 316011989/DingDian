package cn.video.star.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.HotSearchList
import cn.video.star.data.remote.model.SearchResultEntity
import cn.video.star.data.remote.model.SearchSuggestEntity
import cn.junechiu.junecore.utils.ALogger

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private var searchData: LiveData<SearchResultEntity>? = null

    private var suggestData: LiveData<SearchSuggestEntity>? = null

    private var searchPage = MutableLiveData<Int>()
    private var searchWord = MutableLiveData<String>()

    private var suggestWord = MutableLiveData<String>()

    init {
//        searchWord.value = ""
        searchData = Transformations.switchMap(
            searchWord
        ) { page ->
            ALogger.d("search", "search: ${searchWord.value} input: $page")
            DataRepository.instance.videoSearch(searchWord.value!!, 1)
        }

        suggestData = Transformations.switchMap(
            suggestWord
        ) { word ->
            ALogger.d("search", "searchSuggest: ${suggestWord.value} ")
            DataRepository.instance.searchSuggest(suggestWord.value!!, 1)
        }
    }

    fun loadNextPage() {
        if (searchPage.value == null) 1 else searchPage.value = searchPage.value!! + 1
    }

    fun getSearchData(): LiveData<SearchResultEntity>? {
        return searchData
    }

    fun getSuggestData(): LiveData<SearchSuggestEntity>? {
        return suggestData
    }

    fun setKeyWord(word: String) {
        searchPage.value = 1
        searchWord.value = word
    }

    fun setSuggestWord(word: String) {
        suggestWord.value = word
    }

    fun isLoadingSearchData(): LiveData<Boolean>? {
        return DataRepository.instance.isLoadingSearchData()
    }

    fun hotWords(): LiveData<HotSearchList>? {
        return DataRepository.instance.hotWords()
    }

    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(mApplication) as T
        }
    }

}