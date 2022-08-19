package cn.yumi.daka.base

import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import cn.yumi.daka.R
import com.blankj.utilcode.util.KeyboardUtils


/**
 * Created by junzhao on 2018/2/14.
 */

abstract class BaseActivity : AppCompatActivity() {

    //是否使用特殊的标题栏背景颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected val TAG = this.javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val layoutResID = initView(savedInstanceState)
            //如果initView返回0,框架则不会调用setContentView(),当然也不会 Bind ButterKnife
            if (layoutResID != 0) {
                setContentView(layoutResID)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        statusBarTrans()
        initData(savedInstanceState)
    }

    abstract fun initView(savedInstanceState: Bundle?): Int

    abstract fun initData(savedInstanceState: Bundle?)

    //获取状态栏高度
    fun getStatusBarHeight(): Int {
        var statusBarHeight = -1
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }


    //检查是否有导航栏
    fun checkHasNavigationBar(callback: (height: Int) -> Unit) {
        val listener = View.OnApplyWindowInsetsListener { _, insets ->
            var hasBar = false
            val b: Int
            if (insets != null) {
                b = insets.systemWindowInsetBottom
                hasBar = (b == getNavigationBarHeight())
            }
            Log.d("checkHasNavigationBar", "hasBar:$hasBar")
            if (hasBar) {
                callback(getNavigationBarHeight())
            } else {
                callback(0)
            }
            window.decorView.setOnApplyWindowInsetsListener(null)
            insets
        }
        window.decorView.setOnApplyWindowInsetsListener(listener)
    }

    private fun getNavigationBarHeight(): Int {
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }

    //状态栏透明
    private fun statusBarTrans() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //大于5.0系统
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = resources.getColor(R.color.c00000000)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //4.4到5.0
            val localLayoutParams = window.attributes
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    localLayoutParams.flags)
        }
    }

    //5.0及以上
    fun setStatusBar(color: Int, useLightBarColor: Boolean) {
        //设置状态栏背景颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useLightBarColor) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = color
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val decorView = window.decorView
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                if (color == Color.WHITE) {
                    window.statusBarColor = Color.GRAY
                } else {
                    window.statusBarColor = color
                }
            }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)  //app界面区域
    }


    //隐藏状态栏和导航栏
    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    //显示导航栏和状态栏
    fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    //透明导航栏
    fun showNaviga() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.navigationBarColor = Color.TRANSPARENT
    }


    fun alertDialog(title: String, subTitle: String, block: () -> Unit) {
        val customAlertDialog = CustomAlertDialog(
            this, title,
            subTitle, getString(R.string.ok)
        ) {
            block()
        }
        customAlertDialog.show()
    }


    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInput(this)
    }

}
