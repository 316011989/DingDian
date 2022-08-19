package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.data.local.db.AppDatabaseManager
import cn.yumi.daka.data.local.db.entity.CollectEntity
import cn.yumi.daka.ui.adapter.FavoriteAdapter
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.startActivity

class FavoriteActivity : BaseActivity() {

    var data: MutableList<CollectEntity> = mutableListOf()

    lateinit var adapter: FavoriteAdapter

    private var stringBufIds = StringBuffer()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_favorite
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        right_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.mine_like)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.text = getString(R.string.edit)
        left_lay.setOnClickListener {
            finish()
        }

        //编辑删除
        right_lay.setOnClickListener {
            eidtStatus()
        }

        recyclerView.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        adapter = FavoriteAdapter(data)
        recyclerView.adapter = adapter

        //长按删除监听
        adapter.delCallback = { videoId ->
            alertDialog(getString(R.string.sure_delete), "") {
                disLikeVideo(videoId)
            }
        }

        //点选item监听
        adapter.clickItem = {
            if (adapter.isEdit) {
                calSelectCount()
            } else {
                startActivity<PlayerWindowActivity>("id" to it)
            }
        }

        refreshData()
        setListener()
    }

    private fun eidtStatus() {
        adapter.isEdit = !adapter.isEdit
        if (adapter.isEdit) {
            line2.visibility = View.VISIBLE
            toolbar_right_title2.text = getString(R.string.cancel)
            selectLay.visibility = View.VISIBLE
            val layoutParams = recyclerView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(45f)
            )
        } else {
            line2.visibility = View.GONE
            toolbar_right_title2.text = getString(R.string.edit)
            selectLay.visibility = View.GONE
            val layoutParams = recyclerView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(10f),
                ScreenUtil.dp2px(10f),
                0
            )
        }
        adapter?.notifyDataSetChanged()
    }

    private fun setListener() {
        //全选
        var isSelectAll = false
        select_all.setOnClickListener {
            if (isSelectAll) {
                data.forEach { item ->
                    item.selected = 0
                }
                select_all.text = getString(R.string.select_all)
                isSelectAll = false
            } else {
                data.forEach { item ->
                    item.selected = 1
                }
                select_all.text = getString(R.string.cancel_select_all)
                isSelectAll = true
            }
            calSelectCount()
            adapter.notifyDataSetChanged()
        }

        //删除按钮 多选
        delete_btn.setOnClickListener {
            adapter.notifyDataSetChanged()
        }
    }

    //计算选中项
    private fun calSelectCount() {
        var count = 0
        stringBufIds.delete(0, stringBufIds.length)
        data.forEach { item ->
            if (item.selected == 1) {
                count += 1
                stringBufIds.append(item.id.toString() + ",")
            }
        }
        val numText = String.format(getString(R.string.delete_num), count)
        delete_btn.text = numText
    }


    //关注
    private fun disLikeVideo(movieId: Long) {
    }

    //重新获取数据
    private fun refreshData() {
        AppDatabaseManager.dbManager.queryAllCollect {
            runOnUiThread {
                data.clear()
                if (it != null && it.size > 0) {
                    data.addAll(it)
                    adapter.replaceData(it)
                }
            }
        }
    }
}