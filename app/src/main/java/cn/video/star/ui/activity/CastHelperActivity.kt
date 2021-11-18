package cn.video.star.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.video.star.R
import cn.video.star.base.BaseActivity
import com.blankj.utilcode.util.BarUtils
import kotlinx.android.synthetic.main.include_title.*

class CastHelperActivity: BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_casthelper
    }

    override fun initData(savedInstanceState: Bundle?) {
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.casthelper)
        toolbar.setPadding(0, BarUtils.getStatusBarHeight(),0,0)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        left_lay.setOnClickListener {
            finish()
        }
    }
}