package  cn.video.star.ui.activity

import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import  cn.video.star.R
import  cn.video.star.base.App
import  cn.video.star.base.BaseActivity
import  cn.video.star.data.local.db.AppDatabaseManager
import  cn.video.star.data.remote.model.HistoryWatchType
import cn.video.star.data.local.db.entity.MovieHistoryEntity
import cn.video.star.ui.adapter.HistoryTypeAdapter
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.ScreenUtil
import com.blankj.utilcode.util.TimeUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.include_title.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : BaseActivity() {

    val data: MutableList<HistoryWatchType> = mutableListOf()

    var adapter: HistoryTypeAdapter? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_history
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        right_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.watch_history)
        toolbar_center_title.setTextColor(resources.getColor(R.color.white))
        toolbar_right_title2.setTextColor(resources.getColor(R.color.white))
        toolbar_right_title2.text = getString(R.string.edit)
        left_lay.setOnClickListener {
            finish()
        }

        //编辑删除
        right_lay.setOnClickListener {
            adapter?.isEdit = !adapter!!.isEdit
            editChangelayout()
            adapter?.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        adapter = HistoryTypeAdapter(data)
        recyclerView.adapter = adapter

        //长按删除监听
        adapter?.delCallback = { item ->
            alertDialog(getString(R.string.sure_delete), "") {
                deleteItem(item)
                queryHistory()
                App.refreshData.value = App.REFRESH_ME_HISTORY
            }
        }

        //点选item监听
        adapter?.clickItem = {
            calSelectCount()
        }

        //获取观看历史
        queryHistory()
        setListener()
    }

    private fun queryHistory() {
        AppDatabaseManager.dbManager.queryMovies {
            if (it != null)
                getHistory(it)
        }
    }

    private fun editChangelayout() {
        if (adapter!!.isEdit) {
            line2.visibility = View.VISIBLE
            toolbar_right_title2.text = getString(R.string.cancel)
            selectLay.visibility = View.VISIBLE
            val layoutParams = recyclerView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(
                ScreenUtil.dp2px(10f),
                0,
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(45f)
            )
        } else {
            line2.visibility = View.GONE
            toolbar_right_title2.text = getString(R.string.edit)
            selectLay.visibility = View.GONE
            val layoutParams = recyclerView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(ScreenUtil.dp2px(10f), 0, ScreenUtil.dp2px(10f), 0)
        }
    }

    private fun setListener() {
        //全选
        var isSelectAll = false
        select_all.setOnClickListener {
            if (isSelectAll) {
                data.forEach { watchType ->
                    if (watchType.type == HistoryWatchType.LIST) {
                        watchType.historyList!!.forEach { item ->
                            item.selected = 0
                        }
                    }
                }
                select_all.text = getString(R.string.select_all)
                isSelectAll = false
            } else {
                data.forEach { watchType ->
                    if (watchType.type == HistoryWatchType.LIST) {
                        watchType.historyList!!.forEach { item ->
                            item.selected = 1
                        }
                    }
                }
                select_all.text = getString(R.string.cancel_select_all)
                isSelectAll = true
            }
            calSelectCount()
            adapter?.notifyDataSetChanged()
        }
    }

    //计算选中项
    private fun calSelectCount() {
        var count = 0
        var deleteList = mutableListOf<Long>()
        data.forEach { watchType ->
            if (watchType.type == HistoryWatchType.LIST) {
                watchType.historyList!!.forEach { item ->
                    if (item.selected == 1) {
                        count += 1
                        deleteList.add(item.movidId)
                    }
                }
            }
        }
        var numText = String.format(getString(R.string.delete_num), count)
        deleteBtn.text = numText

        //删除所选
        deleteBtn.setOnClickListener {
            if (deleteList.size > 0) {
                alertDialog(getString(R.string.sure_delete), "") {
                    deleteAll(deleteList)
                }
            }
        }
    }

    private fun deleteAll(deleteList: MutableList<Long>) {
        Observable.create(ObservableOnSubscribe<String> { e ->
            AppDatabaseManager.dbManager.deleteMovies(deleteList)
            e.onNext("ok")
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { spaceText ->
                adapter!!.isEdit = false
                editChangelayout()
                queryHistory()
                App.refreshData.value = App.REFRESH_ME_HISTORY
            }
    }

    private fun deleteItem(item: MovieHistoryEntity) {
        try {
            AppDatabaseManager.dbManager.queryHistoryMovieById(item.movidId) {
                if (it != null) {
                    AppDatabaseManager.dbManager.deleteMovie(it)
                    ALogger.d("deleteItem", "movie:" + it.name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getHistory(historyList: MutableList<MovieHistoryEntity>) {
        try {
            var dateList = mutableSetOf<String>()
            if (historyList != null && historyList.size > 0) {
                var history = mutableListOf<HistoryWatchType>()
                historyList.forEach { item ->
                    dateList.add(item.datetime)
                }
                var today =
                    TimeUtils.getNowString(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))
                dateList.forEach { date ->
                    var hisDate = HistoryWatchType(HistoryWatchType.DATE)
                    if (date.equals(today)) {
                        hisDate.date = "今天"
                    } else {
                        hisDate.date = date
                    }
                    var hisList = HistoryWatchType(HistoryWatchType.LIST)
                    hisList.historyList = mutableListOf()
                    historyList.forEach { item ->
                        if (item.datetime.equals(date)) {
                            hisList.historyList!!.add(item)
                        }
                    }
                    history.add(hisDate)
                    history.add(hisList)
                }
                setHistoryData(history)
            } else {
                setHistoryData(mutableListOf())
            }
        } catch (e: Exception) {
            setHistoryData(mutableListOf())
            e.printStackTrace()
        }
    }

    private fun setHistoryData(list: MutableList<HistoryWatchType>) {
        if (list != null) {
            data.clear()
            data.addAll(list)
            adapter!!.notifyDataSetChanged()
            Handler().postDelayed({ recyclerView.scrollToPosition(0) }, 250)
        }
        if (data.size <= 0) {
            noDataView.visibility = View.VISIBLE
            right_lay.visibility = View.GONE
        } else {
            noDataView.visibility = View.GONE
            right_lay.visibility = View.VISIBLE
        }
    }
}