package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import com.just.agentweb.AgentWeb
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.include_title.*


class WebActivity : BaseActivity() {

    private var agentWeb: AgentWeb? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_web
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        left_lay.setOnClickListener {
            finish()
        }

        if (intent.hasExtra("title")) {
            toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
            toolbar_center_title.text = intent.extras?.get("title").toString()
        }

        val url = intent.extras?.get("url").toString()

        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(
                webViewLay,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
            .useDefaultIndicator()
            .setWebViewClient(mWebViewClient())
            .createAgentWeb()
            .ready()
            .go(url)

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
    }

    class mWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            if (request != null && request.url.scheme != null) {
                Log.e("shouldOverrideUrlLoadin", request.url.scheme!!)
                try {
                    if (request.url.scheme == "hntvmobile" ||
                        request.url.scheme == "imgotv"
                    ) {
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view!!.loadUrl(request.url.toString())
            }
            return true
        }


    }

    override fun onResume() {

        agentWeb!!.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        agentWeb!!.webLifeCycle.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        agentWeb!!.webLifeCycle.onDestroy()
    }
}