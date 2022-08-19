package cn.yumi.daka.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.utils.ConfigCenter
import com.blankj.utilcode.util.ToastUtils
import kotlinx.android.synthetic.main.activity_contactus.*
import kotlinx.android.synthetic.main.include_title.*

class ContactUsActivity : BaseActivity(), View.OnLongClickListener {


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_contactus
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.contactus)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        left_lay.setOnClickListener {
            finish()
        }
        setStatusBar(ContextCompat.getColor(this, R.color.cB3ffffff), true)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cB3ffffff))
        val params = toolbar.layoutParams as LinearLayout.LayoutParams
        params.setMargins(0, getStatusBarHeight(), 0, 0)
        contact_qq.setOnLongClickListener(this)
        contact_official.setOnLongClickListener(this)
        getConfigCenterContacts()
    }


    private fun getConfigCenterContacts() {
        if (ConfigCenter.contactWay != null) {
            contact_official.text = "公众号：" + ConfigCenter.contactWay!!.email
            contact_qq.text = "QQ群：${ConfigCenter.contactWay!!.qqGroup}"
            contact_website.text = "官方网站：${ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL}"
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cmb.setPrimaryClip(
            ClipData.newPlainText(
                null,
                (v as TextView).text.substring(v.text.indexOf("：") + 1)
            )
        )
        ToastUtils.showLong("复制成功")
        return false
    }

}
