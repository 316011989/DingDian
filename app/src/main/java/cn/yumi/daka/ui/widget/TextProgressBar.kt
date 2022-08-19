package cn.yumi.daka.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.R
import java.text.DecimalFormat

class TextProgressBar : ProgressBar {

    private var mContext: Context? = null
    private var mPaint: Paint? = null
    private var mPorterDuffXfermode: PorterDuffXfermode? = null
    private var mProgress: Float = 0.toFloat()
    private var mState: Int = 0

    constructor(context: Context) : super(
        context,
        null,
        android.R.attr.progressBarStyleHorizontal
    ) {
        mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        init()
    }

    /**
     * 设置下载状态
     */
    @Synchronized
    fun setState(state: Int) {
        mState = state
        invalidate()
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        mProgress = progress.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (mState) {
            STATE_DEFAULT -> drawIconAndText(canvas, STATE_DEFAULT)
            STATE_DOWNLOADING -> drawIconAndText(canvas, STATE_DOWNLOADING)
            STATE_PREPARE -> drawIconAndText(canvas, STATE_PREPARE)
            STATE_PAUSE -> drawIconAndText(canvas, STATE_PAUSE)
            STATE_SUCCESS -> drawIconAndText(canvas, STATE_SUCCESS)
            else -> drawIconAndText(canvas, STATE_DEFAULT)
        }
    }

    private fun init() {
        isIndeterminate = false
        indeterminateDrawable = ContextCompat.getDrawable(
            mContext!!,
            android.R.drawable.progress_indeterminate_horizontal
        )
        progressDrawable = ContextCompat.getDrawable(
            mContext!!,
            R.drawable.updatedialog_progressbar
        )
        max = 10000

        mPaint = Paint()
        mPaint!!.isDither = true
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.FILL_AND_STROKE
        mPaint!!.textAlign = Paint.Align.LEFT
        mPaint!!.textSize = ScreenUtil.sp2px(mContext!!, TEXT_SIZE_SP).toFloat()
        mPaint!!.typeface = Typeface.MONOSPACE

        mPorterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private fun initForState(state: Int) {
        when (state) {
            STATE_DEFAULT -> {
                progress = 10000
                mPaint!!.color = Color.WHITE
            }
            STATE_PREPARE -> {
                progress = 0
                mPaint!!.color = ContextCompat.getColor(mContext!!, R.color.main)
            }
            STATE_DOWNLOADING -> mPaint!!.color =
                ContextCompat.getColor(mContext!!, R.color.main)
            STATE_PAUSE -> mPaint!!.color = ContextCompat.getColor(mContext!!, R.color.main)
            STATE_SUCCESS -> {
                progress = 10000
                mPaint!!.color = Color.WHITE
            }
            STATE_FAIL -> {
                progress = 10000
                mPaint!!.color = Color.WHITE
            }
            else -> {
                progress = 10000
                mPaint!!.color = Color.WHITE
            }
        }
    }

    private fun drawIconAndText(canvas: Canvas, state: Int) {
        initForState(state)

        val text = getText(state)
        val textRect = Rect()
        mPaint!!.getTextBounds(text, 0, text.length, textRect)

        val textX = (width / 2 - textRect.centerX()).toFloat()
        val textY = (height / 2 - textRect.centerY()).toFloat()
        canvas.drawText(text, textX, textY, mPaint!!)

        if (state == STATE_DEFAULT) return

        val bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bufferCanvas = Canvas(bufferBitmap)
        bufferCanvas.drawText(text, textX, textY, mPaint!!)
        // 设置混合模式
        mPaint!!.xfermode = mPorterDuffXfermode
        mPaint!!.color = Color.WHITE
        val rectF = RectF(0f, 0f, width * mProgress / 10000, height.toFloat())
        // 绘制源图形
        bufferCanvas.drawRect(rectF, mPaint!!)
        // 绘制目标图
        canvas.drawBitmap(bufferBitmap, 0f, 0f, null)
        // 清除混合模式
        mPaint!!.xfermode = null

        if (!bufferBitmap.isRecycled) {
            bufferBitmap.recycle()
        }
    }


    private fun getText(state: Int): String {
        return when (state) {
            STATE_DEFAULT -> "立即下载"
            STATE_PREPARE -> "校验下载链接"
            STATE_DOWNLOADING -> {
                val decimalFormat = DecimalFormat("#0.00")
                decimalFormat.format(mProgress.toDouble() / 100) + "%"
            }
            STATE_PAUSE -> "继续下载"
            STATE_SUCCESS -> "下载完成,立即安装"
            STATE_FAIL -> "下载失败,点击重试"
            else -> "下载失败,点击重试"
        }
    }


    companion object {
        // IconTextProgressBar的状态
        const val STATE_DEFAULT = 101
        const val STATE_PREPARE = 1011
        const val STATE_DOWNLOADING = 102
        const val STATE_PAUSE = 103
        const val STATE_SUCCESS = 104
        const val STATE_FAIL = 105

        // IconTextProgressBar的文字大小(sp)
        private const val TEXT_SIZE_SP = 14f
    }


}