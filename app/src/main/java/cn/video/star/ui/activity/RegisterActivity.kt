package  cn.video.star.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.view.View
import  cn.video.star.R
import  cn.video.star.base.BaseActivity
import cn.junechiu.junecore.rxevent.RxBus
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by android on 2018/3/20.
 */
class RegisterActivity : BaseActivity() {

    var count: Int = 60

    //mobile password  phonecode deviceid
    var params: WeakHashMap<String, String> = WeakHashMap()

    var flag = ""

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_register
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.app_name)
        toolbar_right_title2.text = getString(R.string.login)
        left_lay.setOnClickListener {
            finish()
        }

        flag = intent.extras?.get("flag").toString()

        changeCodebtn(0)
        //登录
        toolbar_right_title2.setOnClickListener {
            if (flag == MainActivity::class.java.simpleName) {
                startActivity<LoginActivity>()
                finish()
            } else {
                finish()
            }
        }

        //获取手机验证码
        getPhoneCode.setOnClickListener {
            //            if (mPresenter?.checkPhone(editPhone.text.toString())!!) {
//                params.clear()
//                params.put("phone", editPhone.text.toString()!!)
//                params.put("deviceid", DeviceUtils.getIMEI(application))
//                params.put("isregister", "1")
//                mPresenter?.getPhoneCode(params, { code ->
//                    if (code == Api.PHONEEXITS_ERROR) {
//                        showMessage(getString(R.string.phone_exites))
//                    }
//                    startTimer()
//                })
//            } else {
//                showMessage(getString(R.string.not_phonenum))
//            }
        }

        //清除手机号
        clearPhone.setOnClickListener {
            editPhone.text.clear()
        }

        visibleIcon.tag = "1"
        visibleLay.setOnClickListener {
            if (visibleIcon.tag.equals("1")) {
                editPasswd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
                visibleIcon.setImageResource(R.mipmap.visiable_passwd_icon)
                visibleIcon.tag = "0"
            } else {
                editPasswd.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;
                visibleIcon.setImageResource(R.mipmap.invisiable_passwd_icon)
                visibleIcon.tag = "1"
            }
        }

        //注册
        Register.setOnClickListener {
            //            if (mPresenter?.checkInfo(editPhone.text.toString().trim()!!,
//                    editPasswd.text.toString().trim()!!,
//                    PhoneCode.text.toString())!!) {
//                params.clear()
//                params.put("deviceid", DeviceUtils.getIMEI(application))
//                params.put("phone", editPhone.text.toString().trim())
//                params.put("password", editPasswd.text.toString().trim())
//                params.put("code", PhoneCode.text.toString())
//                mPresenter?.register(params)
//            }
        }

//        mPresenter?.changeBtn(editPhone, editPasswd, PhoneCode)
    }

    fun startTimer() {
        var codeTime = getString(R.string.get_phonecode_str)
        getPhoneCode.setEnabled(false)
        getPhoneCode.setTextColor(resources.getColor(R.color.cCCCCCC))
        getPhoneCode.setBackgroundResource(R.drawable.border_ccc_fill_e6)
        val countDownTimer = object : CountDownTimer((count * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count -= 1
                getPhoneCode.setText(String.format(codeTime, count))
            }

            override fun onFinish() {
                count = 60
                getPhoneCode.setEnabled(true)
                getPhoneCode.setText(R.string.getcode_again)
                getPhoneCode.setTextColor(resources.getColor(R.color.cFCB230))
                getPhoneCode.setBackgroundResource(R.drawable.border_f14b4b_round)
            }
        }
        countDownTimer.start()
    }

    fun clearPhoneView(s: String) {
        if (s!!.length > 0) {
            clearPhone.visibility = View.VISIBLE
        } else {
            clearPhone.visibility = View.GONE
        }
    }

    fun registerView(phone: String, passwd: String, phonecode: String) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(passwd) || TextUtils.isEmpty(phonecode) ||
            phone.length != 11 || passwd.length < 6
        ) {
            Register.alpha = 0.3f
            Register.setEnabled(false)
        } else {
            Register.alpha = 1f
            Register.setEnabled(true)
        }
    }

    fun changeCodebtn(len: Int) {
        if (len >= 11) {
            getPhoneCode.setEnabled(true)
            getPhoneCode.setTextColor(resources.getColor(R.color.cFCB230))
            getPhoneCode.setBackgroundResource(R.drawable.border_f14b4b_round)
        } else {
            getPhoneCode.setEnabled(false)
            getPhoneCode.setTextColor(resources.getColor(R.color.cCCCCCC))
            getPhoneCode.setBackgroundResource(R.drawable.border_ccc_fill_e6)
        }
    }

    fun registerSuccess() {
        Handler().postDelayed({
            //注册成功后 发送消息到登录页面 直接登录
            //如果从我的页面点击注册进入
            if (flag.equals(MainActivity::class.java.simpleName)) {
                startActivity<LoginActivity>(
                    "phonepass" to editPhone.text.toString().trim() + " "
                            + editPasswd.text.toString().trim()
                )
            } else {
                RxBus.getInstance().post(
                    "event_after_register",
                    editPhone.text.toString().trim() + " " + editPasswd.text.toString().trim()
                )
            }
            finish()
        }, 1500)
    }

    fun killMyself() {
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}