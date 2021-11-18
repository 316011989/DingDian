package cn.video.star.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import cn.video.star.R
import cn.video.star.base.BaseActivity
import cn.video.star.base.GlideApp
import cn.video.star.data.local.db.entity.DownloadEpisodeEntity
import cn.video.star.download.DownloadEntity
import cn.video.star.download.DownloadFeature
import cn.video.star.ui.adapter.CacheFinishAdapter
import kotlinx.android.synthetic.main.activity_cachefinish.*
import kotlinx.android.synthetic.main.activity_cachefinish.line2
import kotlinx.android.synthetic.main.activity_cachefinish.recyclerView
import kotlinx.android.synthetic.main.activity_cachefinish.selectLay
import kotlinx.android.synthetic.main.activity_cachefinish.select_all
import kotlinx.android.synthetic.main.include_title.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CacheFinishActivity : BaseActivity(), View.OnClickListener {


    lateinit var adapter: CacheFinishAdapter
    var video: DownloadEpisodeEntity? = null


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_cachefinish
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        left_lay.setOnClickListener(this)

        right_lay.visibility = View.VISIBLE
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.text = getString(R.string.edit)
        //编辑删除
        right_lay.setOnClickListener(this)
        deleteBtn.setOnClickListener(this)
        select_all.setOnClickListener(this)

        toolbar_center_title.text = intent.extras?.get("title").toString()

        recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        adapter = CacheFinishAdapter(GlideApp.with(this), mutableListOf())
        adapter.bindToRecyclerView(recyclerView)

        video = intent.getSerializableExtra("video") as DownloadEpisodeEntity
        if (video != null) {
            toolbar_center_title.text = video!!.seriesName
            subcribeData(video!!.seriesId!!)
        }
    }

    private fun subcribeData(seriesId: Long) {
        DownloadFeature.queryDownloadedEpisodeBySeriesid(seriesId) {
            runOnUiThread {
                adapter.replaceData(it)
                if (it.isEmpty()) {
                    noDataView.visibility = View.VISIBLE
                } else {
                    noDataView.visibility = View.GONE
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.left_lay -> {
                finish()
            }
            R.id.right_lay -> {
                if (adapter.selectable) {
                    toolbar_right_title2.text = "编辑"
                    line2.visibility = View.GONE
                    selectLay.visibility = View.GONE
                    left_lay.visibility = View.VISIBLE
                } else {
                    toolbar_right_title2.text = "取消"
                    line2.visibility = View.VISIBLE
                    selectLay.visibility = View.VISIBLE
                    left_lay.visibility = View.GONE
                    adapter.chooseIds.clear()
                    select_all.text = "全选"
                }
                adapter.selectable = !adapter.selectable
                adapter.notifyDataSetChanged()
            }
            R.id.deleteBtn -> {
                val dialog = CustomAlertDialog(
                    this, "删除选中任务?", "", "确定"
                ) {
                    if (adapter.chooseIds.size > 0) {
                        for (i in adapter.data.indices) {
                            if (adapter.data[i].episodeId in adapter.chooseIds) {
                                deleteDownloadTask(adapter.data[i])
                            }
                        }
                        adapter.chooseIds.clear()
                        select_all.text = "全选"
                        adapter.notifyDataSetChanged()
                    }
                }
                dialog.show()

            }
            R.id.select_all -> {
                if (select_all.text == "全选") {
                    adapter.chooseIds.clear()
                    for (d in adapter.data) {
                        adapter.chooseIds.add(d.episodeId ?: 0)
                    }
                    select_all.text = "全不选"
                } else {
                    adapter.chooseIds.clear()
                    select_all.text = "全选"
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 删除某集的下载任务
     */
    private fun deleteDownloadTask(task: DownloadEpisodeEntity) {
        //先修改数据库状态
        DownloadFeature.deleteEpisode(task.episodeId.toString()) {
            runOnUiThread {
                if (video != null) {//数据库删除成功
                    subcribeData(video!!.seriesId!!)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    fun onMessageEvent(task: DownloadEntity) {//接收回调只处理缓存任务完成状态,更新UI
        if (task.downloadState == DownloadEntity.state_success)
            subcribeData(video!!.seriesId!!)
    }

    override fun onResume() {
        super.onResume()
        if (video != null)
            subcribeData(video!!.seriesId!!)
    }
}