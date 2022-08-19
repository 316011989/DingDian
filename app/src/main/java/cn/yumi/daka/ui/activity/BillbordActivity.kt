package cn.yumi.daka.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.DeviceUtils
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.R
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.Api.Companion.RESPONSE_OK
import cn.yumi.daka.base.App
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.model.TopicDetail
import cn.yumi.daka.ui.adapter.MovieAdapter
import cn.yumi.daka.ui.widget.GridSpacingItemDecoration
import cn.yumi.daka.utils.ConfigCenter
import cn.yumi.daka.viewmodel.SpecialViewModel
import kotlinx.android.synthetic.main.activity_billbord.*
import org.jetbrains.anko.startActivity

/**
 * 播单
 */
class BillbordActivity : BaseActivity() {

    var adapter: MovieAdapter = MovieAdapter(mutableListOf())
    var topicDetail: TopicDetail? = null

    var title = ""

    var topicId = 0

    var model: SpecialViewModel? = null

    var loadingData = 0 //是否在加载中

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_billbord
    }

    override fun initData(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration.newBuilder()
                .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(5f))
                .verticalSpacing(0)
                .build()
        )
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //滑动到底部 加载更多
                if (!recyclerView.canScrollHorizontally(1)) {
                    ALogger.d("loadListData", "加载更多")
                    if (loadingData == 0) {
                        model?.loadNextPageData()
                        loadingData = 1
                    }
                }
            }
        })



        topicId = intent.extras?.get("id") as Int
        DataRepository.instance.topicCount(topicId)
        if (intent.hasExtra("title")) {
            title = intent.extras?.get("title").toString()
            titleView.text = title
        }

        back_layout.setOnClickListener {
            killMyself()
        }

        shareLayout.setOnClickListener {
            share()
        }
        setStatusBar()
        subscribeUI()
    }

    //订阅
    private fun subscribeUI() {
        val factory = SpecialViewModel.Factory(App.INSTANCE, topicId)
        model = ViewModelProviders.of(this, factory).get(SpecialViewModel::class.java)
        model?.getTopicDetail()?.observe(this, {
            if (it != null && it.code == RESPONSE_OK) {
                this.topicDetail = it
                shareLayout.visibility = View.VISIBLE
            }
        })
        model?.getVideoListData()?.observe(this, {
            if (it != null && it.code == RESPONSE_OK) {
                if (it.data != null && it.data.size > 0) {
                    adapter.addData(it.data)
                    recyclerView.postDelayed({ loadingData = 0 }, 666)
                } else {
                    loadingData = 1  //无数据不加载
                }
            }
        })
        model?.loadNextPageData()
        val map = mutableMapOf<String, Any>()
        map["id"] = topicId
        map["token"] = Api.TOKEN_BEARER
        model?.setToken(map)
    }

    //设置toolbar 距离状态栏的高度
    private fun setStatusBar() {
        shareLayout.visibility = View.INVISIBLE
        val params = topbar.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, getStatusBarHeight(), 0, 0)
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)//设置状态栏背景色白色 文字为深色
    }



    //分享
    private fun share() {
        DeviceUtils.showSystemShareOption(
            this,
            "追剧，有玉米电影APP就够了",
            ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL
        )
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
        } else {
            if (id != 0L) {
                startActivity<PlayerWindowActivity>("id" to id)
            }
        }
    }

    fun killMyself() {
        this.finish()
    }
}