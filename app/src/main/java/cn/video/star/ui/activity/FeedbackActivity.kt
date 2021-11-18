package  cn.video.star.ui.activity

import androidx.lifecycle.Observer
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import android.webkit.WebSettings
import androidx.core.content.ContextCompat
import  cn.video.star.R
import  cn.video.star.base.BaseActivity
import  cn.video.star.data.DataRepository
import  cn.video.star.ui.adapter.FeedbackAdapter
import cn.junechiu.junecore.utils.DeviceUtils
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.toast
import org.json.JSONObject

class FeedbackActivity : BaseActivity() {

    var json = JSONObject()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_feedback
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        right_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.feedback)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.setTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar_right_title2.text = getString(R.string.submit)
        left_lay.setOnClickListener {
            finish()
        }

        //提交
        right_lay.setOnClickListener {
            val feedbackContent = feedbackContent.text.toString()
            if (TextUtils.isEmpty(feedbackContent)) {
                toast(R.string.feedback_tip)
            } else {
                json.put("text", feedbackContent)
            }

            var contactWay = editContact.text.toString()
            if (TextUtils.isEmpty(contactWay)) {
                toast(R.string.link_tip)
            } else {
                json.put("contactWay", contactWay)
            }
            json.put("deviceId", DeviceUtils.getIMEI(application))
            if (json.length() >= 4) {
                DataRepository.instance.feedback(json.toString(), getUserAgent())?.observe(this,
                    Observer<Int> { result ->
                        if (result != null && result == 1) {
                            killMyself()
                        }
                    })
            }
        }

        val data = mutableListOf<String>()
        data.add("功能建议")
        data.add("播放问题")
        data.add("画面/声音问题")
        data.add("性能问题")
        data.add("内容问题")
        data.add("其他")
        val adapter = FeedbackAdapter(data)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        json.put("title", data[adapter.selected])
        adapter.itemCallback = { position ->
            adapter.selected = position
            adapter.notifyDataSetChanged()
            json.put("title", data.get(position))
        }

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
    }

    private fun getUserAgent(): String {
        var userAgent = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(this)
            } catch (e: Exception) {
                userAgent = System.getProperty("http.agent")!!
            }
        } else {
            userAgent = System.getProperty("http.agent")!!
        }
        userAgent += " version: " + packageManager.getPackageInfo(packageName, 0).versionName
        val sb = StringBuffer()
        var i = 0
        val length = userAgent.length
        while (i < length) {
            val c = userAgent[i]
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", c.toInt()))
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    fun killMyself() {
        this.finish()
    }
}