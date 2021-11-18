package cn.video.star.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.Api.Companion.RESPONSE_OK
import cn.video.star.base.App
import cn.video.star.base.BaseActivity
import cn.video.star.data.DataRepository
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.local.db.entity.SearchWordEntity
import cn.video.star.data.remote.model.HotSearch
import cn.video.star.data.remote.model.SearchResult
import cn.video.star.data.remote.model.VideoPlayD
import cn.video.star.ui.adapter.HotSearchAdapter
import cn.video.star.ui.adapter.SearchHistoryAdapter
import cn.video.star.ui.adapter.SearchResultAdapter
import cn.video.star.ui.adapter.SearchSuggestAdapter
import cn.video.star.ui.widget.GridSpacingItemDecoration2
import cn.video.star.ui.widget.SelectEpisodePopupWindow
import cn.video.star.viewmodel.SearchViewModel
import com.blankj.utilcode.util.KeyboardUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.loading_more_view.*
import kotlinx.android.synthetic.main.no_more_view.*
import org.jetbrains.anko.startActivity


class SearchActivity : BaseActivity() {
    private var hisAdapter: SearchHistoryAdapter? = null
    private var hisData = mutableListOf<SearchWordEntity>()

    private var hotAdapter: HotSearchAdapter? = null

    private var hotData = mutableListOf<HotSearch>()

    private var suggestList = mutableListOf<String>()
    private var adapterSuggest: SearchSuggestAdapter? = null

    private var resultList = mutableListOf<SearchResult>()
    private var adapterResult: SearchResultAdapter? = null

    private var pageSize = 15

    private var hasMore = true

    private var popup: SelectEpisodePopupWindow? = null

    private var model: SearchViewModel? = null


    private var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when {
                msg.what == 1 -> {
                    load_view.visibility = View.VISIBLE
                    no_moreview.visibility = GONE
                }
                msg.what == 2 -> {
                    no_moreview.visibility = View.VISIBLE
                    load_view.visibility = GONE
                }
                else -> {
                    no_moreview.visibility = GONE
                    load_view.visibility = GONE
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_search
    }

    override fun initData(savedInstanceState: Bundle?) {
        searchEdit.requestFocus()
        searchEdit.tag = "请输入片名"
        historyList.layoutManager = GridLayoutManager(this, 2)
        val spanCount = 2 // 2 columns
        val spacing = ScreenUtil.dp2px(10.0f)
        val includeEdge = false
        historyList.addItemDecoration(GridSpacingItemDecoration2(spanCount, spacing, includeEdge))
        hisAdapter = SearchHistoryAdapter(hisData)
        historyList.adapter = hisAdapter

        hotSearchList.layoutManager = GridLayoutManager(this, 2)
        val spanCount2 = 2 // 2 columns
        val spacing2 = ScreenUtil.dp2px(16.0f)
        val includeEdge2 = false
        hotSearchList.addItemDecoration(
            GridSpacingItemDecoration2(
                spanCount2,
                spacing2,
                includeEdge2
            )
        )
        hotAdapter = HotSearchAdapter(hotData)
        hotSearchList.adapter = hotAdapter

        //联想查询
        searchSuggestList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        adapterSuggest = SearchSuggestAdapter(suggestList)
        adapterSuggest?.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        searchSuggestList.adapter = adapterSuggest
        //结果查询
        searchResultList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        adapterResult = SearchResultAdapter(resultList, resources.getStringArray(R.array.sources))
        adapterResult?.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        searchResultList.adapter = adapterResult

        updateHisData()
        actions()

        Handler().postDelayed({ scrollListener() }, 1000)

        popup = SelectEpisodePopupWindow(this)
        popup?.initSelfView()
        popup?.itemCallback = { id, position ->
            startActivity<PlayerWindowActivity>("id" to id, "esp" to position)
        }

        adapterResult?.itemCallback = { id, position ->
            startActivity<PlayerWindowActivity>("id" to id, "esp" to position)
        }
        adapterResult?.popupDataBack = { id, name, plays ->
            showPop(id, name, plays)
        }

        //清除搜索关键字
        closeIcon.setOnClickListener {
            searchEdit.setText("")
        }

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)

        subscribeUi()
    }

    private fun scrollListener() {
        mNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {// 向下滑动
            }
            if (scrollY < oldScrollY) {// 向上滑动
            }
            if (scrollY == 0) {// 顶部
            }
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                //滑动到底部 加载更多
                ALogger.d("mNestedScrollView", "加载更多")
                if (searchResultList.visibility == View.VISIBLE
                    && noDataView.visibility == View.GONE
                ) {
                    if (hasMore) {
                        onLoadMoreRequested()
                        handler.sendEmptyMessage(1)
                    } else {
                        handler.sendEmptyMessage(2)
                    }
                }
            }
        })
    }

    private fun actions() {
        cancelText.setOnClickListener {
            killMyself()
        }

        searchEdit.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                KeyboardUtils.hideSoftInput(this)
                search(v.text.toString())
            }
            false
        }

        clearLayout.setOnClickListener {
            AppDatabaseManager.dbManager.getWordCount { count ->
                if (count != null && count > 0) {
                    alertDialog(getString(R.string.sure_clear), "") {
                        AppDatabaseManager.dbManager.deleteAllWords()
                        hisData.clear()
                        hisAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        searchEdit.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            if (!hasFocus) {// 失去焦点
                if (et.tag != null)
                    et.hint = et.tag.toString()
            } else {
                val hint = et.hint.toString()
                et.tag = hint//保存预设字
                et.hint = ""
            }
        }
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    closeIcon.visibility = View.VISIBLE
                    searchSuggest(s.toString())
                } else {
                    closeIcon.visibility = View.GONE
                    search("")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }

    private fun searchSuggest(keyWord: String) {
        if (!TextUtils.isEmpty(keyWord)) {
            suggestList.clear()
            adapterSuggest!!.notifyDataSetChanged()
            model?.setSuggestWord(keyWord)
            historylayout.visibility = GONE
            searchSuggestList.visibility = View.VISIBLE
            searchResultList.visibility = GONE
            search_banner2.visibility = View.VISIBLE
            val word = SearchWordEntity()
            word.word = keyWord
            DataRepository.instance.keywordCount(keyWord)
            AppDatabaseManager.dbManager.querySearchWordsByWord(keyWord) { list ->
                if (list == null || (list != null && list.size <= 0)) {
                    updateHisData()
                }
            }
        } else {
            historylayout.visibility = View.VISIBLE
            searchResultList.visibility = GONE
            searchSuggestList.visibility = GONE
            search_banner2.visibility = GONE
            noDataView.visibility = GONE
            handler.sendEmptyMessage(3)
        }
    }

    fun search(keyWord: String) {
        if (!TextUtils.isEmpty(keyWord)) {
            searchEdit.setText(keyWord)
            resultList.clear()
            adapterResult!!.notifyDataSetChanged()
            ALogger.d("search", "search: $keyWord")
            model?.setKeyWord(keyWord)
            historylayout.visibility = GONE
            searchResultList.visibility = View.VISIBLE
            searchSuggestList.visibility = GONE
            search_banner2.visibility = View.VISIBLE
            val word = SearchWordEntity()
            word.word = keyWord
            DataRepository.instance.keywordCount(keyWord)
            AppDatabaseManager.dbManager.querySearchWordsByWord(keyWord) { list ->
                if (list == null || list.size <= 0) {
                    AppDatabaseManager.dbManager.insertWord(word)
                    updateHisData()
                }
            }
        } else {
            historylayout.visibility = View.VISIBLE
            searchResultList.visibility = View.GONE
            searchSuggestList.visibility = View.GONE
            search_banner2.visibility = View.GONE
            noDataView.visibility = View.GONE
            handler.sendEmptyMessage(3)
        }
    }

    //历史搜索数据
    private fun updateHisData() {
        try {
            AppDatabaseManager.dbManager.querySearchWords { words ->
                if (words != null) {
                    hisData.clear()
                    words.forEachIndexed { index, searchHisWord ->
                        if (index <= 9) {
                            hisData.add(searchHisWord)
                        }
                    }
                    hisAdapter?.notifyDataSetChanged()
                    hisAdapter?.itemCallback = { word ->
                        search(word)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //热词搜索
    private fun setHotSearch(hotSearchList: MutableList<HotSearch>) {
        hotSearchList.forEachIndexed { index, hotSearch ->
            hotSearch.id = index + 1
        }
        hotData.addAll(hotSearchList)
        hotAdapter?.notifyDataSetChanged()
        hotAdapter?.itemCallback = { word ->
            search(word)
        }
    }

    private fun onLoadMoreRequested() {
        model?.loadNextPage()
    }

    private fun setSuggest(data: MutableList<String>?) {
        if (data != null) {
            //判断是否还有数据
            hasMore = data.size >= pageSize
            suggestList.addAll(data)
        } else {
            hasMore = false
        }
        //空空如也
        if (suggestList.size <= 0) {
            noDataView.visibility = View.VISIBLE
        } else {
            noDataView.visibility = View.GONE
        }
        adapterSuggest?.notifyDataSetChanged()
    }


    private fun setResult(data: MutableList<SearchResult>?) {
        if (data != null) {
            //判断是否还有数据
            hasMore = data.size >= pageSize
            resultList.addAll(data)
        } else {
            hasMore = false
        }
        //空空如也
        if (resultList.size <= 0) {
            noDataView.visibility = View.VISIBLE
        } else {
            noDataView.visibility = View.GONE
        }
        adapterResult?.notifyDataSetChanged()
    }

    private fun showPop(id: Long, name: String, plays: MutableList<VideoPlayD>) {
        popup?.initData(id, name, plays)
        popup?.showPopupWindow()
    }

    private fun subscribeUi() {
        val factory = SearchViewModel.Factory(App.INSTANCE!!)
        model = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        model?.getSearchData()?.observe(this,
            { resultList ->
                if (resultList != null && resultList.code == RESPONSE_OK) {
                    setResult(resultList.data)
                }
            })
        model?.getSuggestData()?.observe(this,
            { resultList ->
                if (resultList != null && resultList.code == RESPONSE_OK) {
                    setSuggest(resultList.data)
                }
            })
        model?.hotWords()?.observe(this,
            { hotWords ->
                if (hotWords != null && hotWords.code == RESPONSE_OK)
                    setHotSearch(hotWords.data)
            })
        model?.isLoadingSearchData()?.observe(this, object : Observer<Boolean> {
            override fun onChanged(t: Boolean?) {
                if (t == null) return
                if (t) {
                    progressView.visibility = View.VISIBLE
                } else {
                    progressView.visibility = GONE
                }
            }
        })
    }

    fun whereGo(adType: Int, adUrl: String?, id: Long) {
        ALogger.d("where--adType:$adType,adUrl:$adUrl,id:$id")
        when (adType) {
            Api.TYPE_AD_INNER -> {
                webADS(adUrl, id)
            }
            Api.TYPE_AD_PLAY -> {
                startActivity<PlayerWindowActivity>("id" to id)
            }
            Api.TYPE_AD_BROSER -> {
                if (!TextUtils.isEmpty(adUrl)) {
                    val uri = Uri.parse(adUrl)
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    intent.data = uri
                    startActivity(intent)
                }
            }
            Api.TYPE_AD_WEB -> {
                startActivity<PlayerWindowActivity>("id" to id)
            }
        }
    }

    //vfans://video/123  app内部跳转
    private fun webADS(url: String?, id: Long) {
        if (!TextUtils.isEmpty(url) && url!!.startsWith("vfans")) {
            try {
                val cate = url.replace("vfans://", "").split("/")[0]
                val id = url.replace("vfans://", "").split("/")[1]
                if (cate == "video") {
                    startActivity<PlayerWindowActivity>("id" to id.toLong())
                } else {
                    startActivity<BillbordActivity>("id" to id.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (id != 0L) {
            startActivity<PlayerWindowActivity>("id" to id)
        }
    }

    fun killMyself() {
        this.finish()
    }

}