package cn.video.star.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.video.star.R
import cn.video.star.base.BaseActivity
import kotlinx.android.synthetic.main.activity_disclaimer.*
import kotlinx.android.synthetic.main.include_title.*

class DisclaimerActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_disclaimer
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.disclaimer)
        toolbar_center_title.setTextColor(resources.getColor(R.color.white))
        left_lay.setOnClickListener {
            finish()
        }

        text.text = getString(R.string.disclaimer_text)
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
    }

    fun killMyself() {
        this.finish()
    }

}