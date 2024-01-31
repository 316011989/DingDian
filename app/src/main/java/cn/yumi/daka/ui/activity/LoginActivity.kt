package  cn.yumi.daka.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.junechiu.junecore.utils.DeviceUtils
import cn.yumi.daka.viewmodel.LoginViewModel
import cn.yumi.daka.R
import cn.yumi.daka.base.Api.Companion.RESPONSE_OK
import cn.yumi.daka.base.App
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.data.remote.model.LoginResponse
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.toast
import java.util.*

/**
 * Created by android on 2018/3/20.
 */
class LoginActivity : BaseActivity() {

    var count: Int = 120

    private var countDownTimer: CountDownTimer? = null

    var model: LoginViewModel? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_login
    }

    override fun initData(savedInstanceState: Bundle?) {
        right_img.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.phone_login)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.main))
        right_img.setImageResource(R.mipmap.close_icon)
        right_lay.setOnClickListener {
            this.finish()
        }
        Login.setOnClickListener {
            login()
        }
        phoneCodeBtn.setOnClickListener {
            getPhoneCode()
        }
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)

        subscribeUI()
    }

    private fun subscribeUI() {
        val factory = LoginViewModel.Factory(App.INSTANCE)
        model = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
    }

    private fun getPhoneCode() {
        val phone = editPhone.text.toString()
        if (!TextUtils.isEmpty(phone)) {
            toast(R.string.phone_error)
        }
    }

    fun changeLoginBtn(phone: String, passwd: String) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(passwd) ||
            phone.length != 11 || passwd.length < 6
        ) {
            Login.alpha = 0.3f
            Login.isEnabled = false
        } else {
            Login.alpha = 1f
            Login.isEnabled = true
        }
    }

    private fun startTimer() {
        var codeTime = getString(R.string.get_phonecode_str)
        phoneCodeBtn.isEnabled = false
        phoneCodeBtn.setTextColor(ContextCompat.getColor(this, R.color.c7b50db))
        countDownTimer = object : CountDownTimer((count * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count -= 1
                phoneCodeBtn.text = String.format(codeTime, count)
            }

            override fun onFinish() {
                count = 120
                phoneCodeBtn.isEnabled = true
                phoneCodeBtn.setText(R.string.getcode_again)
                phoneCodeBtn.setTextColor(
                    ContextCompat.getColor(
                        this@LoginActivity,
                        R.color.c444444
                    )
                )
            }
        }
        countDownTimer?.start()
    }

    //登录
    fun login() {
        val phone = editPhone.text.toString()
        val code = editCode.text.toString()
        if (!TextUtils.isEmpty(phone)) {
            if (phone.length == 11) {
                toast(R.string.phone_error)
            } else {
                toast(R.string.phone_error)
            }
        } else {
            toast(R.string.input_phone_num)
        }
    }


    override fun onDestroy() {
        countDownTimer?.cancel()
        countDownTimer = null
        super.onDestroy()
    }
}