package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity
import cn.yumi.daka.download.DownloadEntity
import cn.yumi.daka.download.DownloadFeature
import cn.yumi.daka.ui.adapter.CachingAdapter
import kotlinx.android.synthetic.main.activity_caching.*
import kotlinx.android.synthetic.main.include_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CachingActivity : BaseActivity(), View.OnClickListener {

    lateinit var adapter: CachingAdapter
    lateinit var dialog: CustomAlertDialog


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_caching
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        right_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.offline_caching)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.text = getString(R.string.edit)
        left_lay.setOnClickListener(this)//返回
        right_lay.setOnClickListener(this)//编辑
        deleteBtn.setOnClickListener(this)//删除
        select_all.setOnClickListener(this)//全选

        recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        adapter = CachingAdapter(GlideApp.with(this), mutableListOf())
        adapter.bindToRecyclerView(recyclerView)


        EventBus.getDefault().register(this)
        subcribeData()
    }

    private fun subcribeData() {
        DownloadFeature.queryNotFinishedEpisode {
            runOnUiThread {
                if (it.isEmpty()) {
                    noDataView.visibility = View.VISIBLE
                    cache_text.text = "没有正在下载的视频"
                } else {
                    noDataView.visibility = View.GONE
                }
                adapter.replaceData(it)
            }
        }
    }


    private fun editChangelayout() {
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.left_lay -> {
                finish()
            }
            R.id.right_lay -> {
                editChangelayout()
            }
            R.id.deleteBtn -> {//删除
                if (adapter.chooseIds.size > 0) {
                    dialog = CustomAlertDialog(
                        this, "是否删除该缓存任务?", "", "删除"
                    ) {
                        for (d in adapter.data) {
                            if (d.episodeId in adapter.chooseIds)
                                deleteDownloadTask(d)
                        }
                        adapter.chooseIds.clear()
                    }
                    dialog.show()
                }
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
                if (it > 0) {//数据库删除成功
                    //更新当前UI
                    subcribeData()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    fun onMessageEvent(task: DownloadEntity) {
        subcribeData()
    }
}