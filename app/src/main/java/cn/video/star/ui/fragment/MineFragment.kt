package  cn.video.star.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ALogger
import cn.video.star.R
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.remote.model.MineFeedType
import cn.video.star.ui.activity.*
import cn.video.star.ui.adapter.MineFeedAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.fragment_mine.*
import org.jetbrains.anko.startActivity

/**
 * Created by junzhao on 2018/2/16.
 */
class MineFragment : Fragment(), View.OnClickListener {

    val data: MutableList<MineFeedType> = mutableListOf()

    var adapter: MineFeedAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mine, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setStatusBar(Color.TRANSPARENT, false)
        initData()
        ALogger.d("fragment---MineFragment", "加载完毕")
    }

    fun initData() {
        mine_setting.setOnClickListener(this)
        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        data.add(MineFeedType(MineFeedType.HISTORY))
        data.add(MineFeedType(MineFeedType.CACHE))
        data.add(MineFeedType(MineFeedType.LIKE))
        data.add(MineFeedType(MineFeedType.COMMEND))
        data.add(MineFeedType(MineFeedType.SETTING))
        adapter = MineFeedAdapter(data)
        recyclerView.adapter = adapter
        adapter?.openLoadAnimation(BaseQuickAdapter.ALPHAIN)

        adapter?.moreClickCallback = { type ->
            when (type) {
                MineFeedType.CACHE -> {
                    activity?.startActivity<CacheActivity>()
                }
                MineFeedType.HISTORY -> {
                    activity?.startActivity<HistoryActivity>()
                }
                MineFeedType.LIKE -> {
                    activity?.startActivity<FavoriteActivity>()
                }
                MineFeedType.COMMEND -> {
                    activity?.startActivity<ShareActivity>()
                }
                MineFeedType.SETTING -> {
                    activity?.startActivity<SettingActivity>()
                }
            }
        }

        subscribeUI()
    }

    private fun subscribeUI() {
        getHistoryData()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as MainActivity).setStatusBar(Color.TRANSPARENT, false)
            getHistoryData()
        }
    }

    private fun getHistoryData() {
        AppDatabaseManager.dbManager.queryMovies { list ->
            if (list != null)
                adapter!!.addHistoryData(list)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.mine_setting -> {
                activity?.startActivity<SettingActivity>()
            }
        }
    }

}