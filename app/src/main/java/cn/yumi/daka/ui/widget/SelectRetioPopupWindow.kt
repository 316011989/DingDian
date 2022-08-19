package cn.yumi.daka.ui.widget

import android.app.Activity
import android.view.View
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.ClarityModel
import cn.yumi.daka.ui.adapter.RatioTextAdapter
import cn.junechiu.junecore.widget.popup.BasePopupWindow

/**
 * 选择清晰度弹窗
 */
class SelectRetioPopupWindow(private val instance: Activity) : BasePopupWindow(instance) {

    var selectCallback = fun(clarityId: ClarityModel.Clarity) {}

    private var ratioList = mutableListOf<String>()
    private var clarityCacheList = mutableListOf<ClarityModel.Clarity>()

    var adapter: RatioTextAdapter? = null


    init {
        setPopupAnimaStyle(R.style.PopupAnimation)
        createView()
    }

    override fun onCreatePopupView(): View {
        return createPopupById(R.layout.select_ratio_popup)
    }

    private fun createView() {
        val ratioListView = findViewById(R.id.ratio_list) as RecyclerView
        ratioListView.layoutManager = LinearLayoutManager(
            instance,RecyclerView.VERTICAL,false
        )
        adapter = RatioTextAdapter(ratioList)
        ratioListView.adapter = adapter
        adapter?.itemCallback = { position ->
            selectCallback(clarityCacheList[position])
        }
    }

    override fun initAnimaView(): View? {
        return null
    }

    override fun initShowAnimation(): Animation? {
        return null
    }

    override fun getClickToDismissView(): View {
        return popupWindowView
    }

    override fun showPopupWindow(v: View) {
        offsetX = (v.width - width) / 2
        offsetY = 0
        super.showPopupWindow(v)
    }

    fun setRatio(clarityCacheList: MutableList<ClarityModel.Clarity>) {
        this.clarityCacheList = clarityCacheList
        ratioList.clear()
        clarityCacheList.forEach { clarity ->
            ratioList.add(clarity.text)
        }
        adapter?.notifyDataSetChanged()
    }

}