package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yumi.daka.R
import cn.yumi.daka.base.App
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity
import cn.yumi.daka.download.DownloadEntity
import cn.yumi.daka.download.DownloadFeature
import cn.yumi.daka.ui.adapter.CacheAdapter
import kotlinx.android.synthetic.main.activity_cache.*
import kotlinx.android.synthetic.main.include_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CacheActivity : BaseActivity() {

    lateinit var adapter: CacheAdapter

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_cache
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        right_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.offline_cache)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.text = getString(R.string.edit)
        left_lay.setOnClickListener {
            finish()
        }

        //编辑删除
        right_lay.visibility=View.INVISIBLE
        right_lay.setOnClickListener {
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = CacheAdapter(GlideApp.with(this),mutableListOf())
        adapter.bindToRecyclerView(recyclerView)

        subcribeData()
        EventBus.getDefault().register(this)
        App.refreshCacheData.value = "1"
    }

    private fun subcribeData() {
        DownloadFeature.queryEpisodeGroupbySeriesid{
            runOnUiThread {
                if (it != null) {
                    adapter.replaceData(it)
                    if (it.size > 0)
                        noDataView.visibility = View.GONE
                    else
                        noDataView.visibility = View.VISIBLE
                } else {
                    adapter.replaceData(mutableListOf())
                    noDataView.visibility = View.VISIBLE
                }
            }
        }
    }



    /**
     * 删除某剧的所有集的下载任务
     */
    fun deleteDownloadTask(task: DownloadEpisodeEntity) {
        DownloadFeature.queryDownloadedEpisodeBySeriesid(task.seriesId!!) {
            runOnUiThread {
                if (it.size > 0) {
                    for (i in it) {
                        DownloadFeature.deleteEpisode(i.episodeId.toString()){
                            //更新当前UI
                            subcribeData()
                        }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    fun onMessageEvent(task: DownloadEntity) {//接收回调只处理缓存任务完成状态,更新UI
        if (task.downloadState == DownloadEntity.state_success)
            subcribeData()
    }


    override fun onResume() {
        super.onResume()
        subcribeData()
    }

    fun killMyself() {
        this.finish()
    }
}