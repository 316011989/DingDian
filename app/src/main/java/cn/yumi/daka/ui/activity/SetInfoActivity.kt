package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_info.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.toast
import org.json.JSONObject

class SetInfoActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_set_info
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.info)
        toolbar_center_title.setTextColor(resources.getColor(R.color.white))
        left_lay.setOnClickListener {
            killMyself()
        }
        right_lay.visibility = View.VISIBLE
        toolbar_right_title1.text = getString(R.string.submit)
        toolbar_right_title1.setTextColor(resources.getColor(R.color.cFFB500))
        right_lay.setOnClickListener {
            modifyInfo()
        }

        val info = intent.extras?.get("info").toString()
        editInfo.setText(info)
        editInfo.setSelection(info.length)

        editInfo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                tipNum.text = s.length.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
    }

    private fun modifyInfo() {
        var info = editInfo.text.toString()
        if (TextUtils.isEmpty(info)) {
            toast(R.string.set_info_tip)
        } else if (info.length > 60) {
            toast(R.string.set_info_limit_tip)
        } else {
            var json = JSONObject()
            json.put("introduction", info)
        }
    }

    fun killMyself() {
        this.finish()
    }
}