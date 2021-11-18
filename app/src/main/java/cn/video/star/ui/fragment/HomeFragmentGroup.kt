package cn.video.star.ui.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import cn.junechiu.junecore.utils.ALogger
import cn.video.star.R
import cn.video.star.base.App
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.remote.model.TypeData
import cn.video.star.data.remote.model.VideoType
import cn.video.star.base.Api.Companion.RESPONSE_OK
import cn.video.star.ui.activity.MainActivity
import cn.video.star.ui.activity.SearchActivity
import cn.video.star.ui.adapter.ChannelAdapter
import cn.video.star.ui.adapter.TabItemAdapter
import cn.video.star.viewmodel.ChannelsViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_channels.*
import org.jetbrains.anko.startActivity

class HomeFragmentGroup : Fragment() {

    private var fragments = mutableListOf<Fragment>()

    private var tabTextlist = mutableListOf<String>()

    var tabAdapter: TabItemAdapter? = null

    var adapter: ChannelAdapter? = null

    var tabLayoutManager: LinearLayoutManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channels, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeUI()
        home_search.setOnClickListener {
            activity?.startActivity<SearchActivity>()
        }
        (activity as MainActivity).setStatusBar(
            getColor(requireContext(), R.color.colorPrimary),
            false
        )
        ALogger.d("fragment---ChannelsFragment", "加载完毕")
    }

    private fun subscribeUI() {
        val factory = ChannelsViewModel.Factory(App.INSTANCE)
        val model = ViewModelProviders.of(this, factory).get(ChannelsViewModel::class.java)
        model.getVideoType()?.observe(viewLifecycleOwner, { typeData ->
            if (typeData != null) {
                setTypeData(typeData)
            } else {
                AppDatabaseManager.dbManager.loadVideoType { videoType ->
                    if (videoType != null && !TextUtils.isEmpty(videoType.json)) {
                        val category =
                            Gson().fromJson(videoType.json, VideoType::class.java)
                        if (category != null)
                            setTypeData(category)
                    }
                }
            }
        })
    }

    private fun setTypeData(category: VideoType) {
        fragments.add(HomeFragmentItemType1())
        tabTextlist.add("推荐")
        if (category.code == RESPONSE_OK) {
            val categorys = category.data
            if (categorys != null && categorys.size > 0) {
                categorys.forEach { typeData ->
                    //添加fragment
                    val fragment = HomeFragmentItemType2()
                    fragments.add(fragment)
                    tabTextlist.add(typeData.name)
                }
                initTabsFrags(categorys)
            } else {
                initTabsFrags(null)
            }
        }
    }

    private fun initTabsFrags(typeData: MutableList<TypeData>?) {
        adapter = ChannelAdapter(childFragmentManager, fragments, typeData)
        viewPager.adapter = adapter

        tabAdapter = TabItemAdapter(tabTextlist)
        tabLayoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        tabListView.layoutManager = tabLayoutManager
        tabListView.adapter = tabAdapter
        initListener()
    }

    private fun initListener() {
        //滑动监听
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                //联动
                tabAdapter?.selected = position
                tabAdapter?.notifyDataSetChanged()

                //滑动居中
                val firstVisibleItem = tabLayoutManager?.findFirstVisibleItemPosition()
                val lastVisibleItem = tabLayoutManager?.findLastVisibleItemPosition()
                val left = tabListView.getChildAt(position - firstVisibleItem!!).left
                val right = tabListView.getChildAt(lastVisibleItem!! - position).left

                tabListView.scrollBy((left - right) / 2, 0)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        //选项点击事件
        tabAdapter?.itemCallback = { position ->
            viewPager.setCurrentItem(position, false)
        }
        viewPager.currentItem = 0
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ALogger.d("ChannelsFragment", "onDestroyView")
        tabTextlist.clear()
        fragments.clear()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as MainActivity).setStatusBar(
                getColor(requireContext(), R.color.colorPrimary),
                false
            )
        }
    }
}