package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set_name.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.toast
import org.json.JSONObject

class SetNameActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_set_name
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.nickname)
        toolbar_center_title.setTextColor(resources.getColor(R.color.white))
        left_lay.setOnClickListener {
            killMyself()
        }

        val name = intent.extras?.get("name").toString()
        oldName.text = name

        editName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty()) {
//                    clearName.visibility = View.VISIBLE
//                } else {
//                    clearName.visibility = View.GONE
//                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        submitBtn.setOnClickListener {
            modifyName()
        }

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
    }

    private fun modifyName() {
        var name = editName.text.toString()
        if (TextUtils.isEmpty(name) || name.length >= 10) {
            toast(R.string.set_name_tip)
        } else {
            var json = JSONObject()
            json.put("name", name)
        }
    }

    fun killMyself() {
        this.finish()
    }
}