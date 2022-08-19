package cn.yumi.daka.ui.widget

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import android.view.View
import android.view.animation.Animation
import android.widget.Toast
import cn.yumi.daka.R
import cn.junechiu.junecore.utils.BitmapUtil
import cn.junechiu.junecore.widget.popup.BasePopupWindow
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * 选择图片弹窗
 */
class SelectPhotoPopupWindow(private val instance: Activity) : BasePopupWindow(instance) {

    companion object {
        const val REQUEST_CODE_CHOOSE_PHOTO = 4

        const val PERMISSION_REQUESTCODE = 10011
    }

    private var photoFile: File? = null

    init {
        setPopupAnimaStyle(R.style.PopupAnimation)
    }

    override fun onCreatePopupView(): View {
        return createPopupById(R.layout.select_image_popup)
    }

    fun initData() {
        findViewById(R.id.tv_from_photo).setOnClickListener {
            val intent1 = Intent(Intent.ACTION_GET_CONTENT)
            intent1.addCategory(Intent.CATEGORY_OPENABLE)
            intent1.type = "image/*"
            intent1.putExtra("data", true)
            instance.startActivityForResult(intent1, REQUEST_CODE_CHOOSE_PHOTO)
            dismiss()
        }
        findViewById(R.id.tv_from_photograph).setOnClickListener {
            // 先验证手机是否有sdcard
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                if (photoFile != null)
                    if (ActivityCompat.checkSelfPermission(instance,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        EasyPermissions.requestPermissions(instance, instance.getString(R.string.title_settings_rationale),
                                PERMISSION_REQUESTCODE, Manifest.permission.CAMERA)
                    } else {
                        BitmapUtil.takePhoto(true, instance, photoFile)
                    }
            } else {
                Toast.makeText(instance, "没有找到储存卡!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
        findViewById(R.id.select_cancel).setOnClickListener {
            dismiss()
        }
    }

    // 设置拍摄的照片保存到哪里
    fun setPhotoFile(photoFile: File) {
        this.photoFile = photoFile
    }

    override fun initAnimaView(): View? {
        return null
    }

    override fun initShowAnimation(): Animation? {
        return null
    }

    override fun getClickToDismissView(): View {
        return popupWindowView
    }

    override fun showPopupWindow(v: View) {
        offsetX = (v.width - width) / 2
        offsetY = 0
        super.showPopupWindow(v)
    }

}