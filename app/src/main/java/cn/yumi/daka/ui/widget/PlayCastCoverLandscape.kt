package cn.yumi.daka.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yumi.daka.R
import cn.yumi.daka.base.DataInter
import cn.yumi.daka.base.DataInter.Key.KEY_PLAY_ESP
import cn.yumi.daka.data.remote.model.VideoPlay
import cn.yumi.daka.ui.activity.PlayerHelper
import cn.yumi.daka.ui.activity.PlayerHelper.Companion.CAST_SCREEN
import cn.yumi.daka.ui.activity.PlayerWindowActivity
import cn.yumi.daka.ui.adapter.PlayerCoverEpisodeAdapter
import cn.yumi.daka.utils.TCAgentUtil.Companion.airplayOk
import com.alibaba.fastjson.JSON
import com.hpplay.sdk.source.api.LelinkPlayerInfo
import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.event.BundlePool
import com.kk.taurus.playerbase.event.EventKey
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.lebo.IUIUpdateListener
import com.kk.taurus.playerbase.lebo.LeCast
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.player.OnTimerUpdateListener
import com.kk.taurus.playerbase.receiver.BaseCover
import com.kk.taurus.playerbase.receiver.IReceiverGroup.OnGroupValueUpdateListener
import com.kk.taurus.playerbase.touch.OnTouchGestureListener
import com.kk.taurus.playerbase.utils.TimeUtil
import com.kk.taurus.playerbase.utils.VideoUtil
import java.util.*

class PlayCastCoverLandscape(context: Context, index: Int) : BaseCover(context),
    OnTimerUpdateListener,
    OnTouchGestureListener, View.OnClickListener {

    private val rootLay = findViewById<RelativeLayout>(R.id.cast_lay)
    private val mBackIcon = findViewById<ImageView>(R.id.cast_back)
    private val mStateIcon = findViewById<ImageView>(R.id.start_play_img)
    private val mCurrTime = findViewById<TextView>(R.id.cast_current)
    private val mTotalTime = findViewById<TextView>(R.id.cast_total)
    private val mSeekBar = findViewById<SeekBar>(R.id.cast_seek_progress)
    private val quitCast = findViewById<TextView>(R.id.quit_cast)
    private val changeCast = findViewById<TextView>(R.id.change_cast)
    private val castTitle = findViewById<TextView>(R.id.cast_title)
    private val mNextIcon = findViewById<ImageView>(R.id.cast_cover_next)
    private val changeEpisode = findViewById<TextView>(R.id.cast_cover_episode)
    private val change_vol_layout = findViewById<LinearLayout>(R.id.change_vol_layout)
    private val select_panel_lay = findViewById<LinearLayout>(R.id.select_panel_lay)
    private val changeVolImg = findViewById<ImageView>(R.id.change_vol_img);
    private val volUp = findViewById<ImageView>(R.id.change_vol_up);
    private val volDown = findViewById<ImageView>(R.id.change_vol_down);

    private var mSeekProgress = -1
    private val TAG = "PlayCastCoverLandscape"
    private var dataSource: DataSource? = null //播放的视频相关数据

    private val selectPanelLayWidth = 358F
    private var selectPanelAnimator: ObjectAnimator? = null

    var index = 0

    private var espAdapter: PlayerCoverEpisodeAdapter? = null
    private var episodeList = mutableListOf<VideoPlay>()

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {}

    init {
        this.index = index
        mSeekBar.isEnabled = false;
        changeCast.setOnClickListener(this)
        quitCast.setOnClickListener(this)
        mBackIcon.setOnClickListener(this)
        mStateIcon.setOnClickListener(this)
        mNextIcon.setOnClickListener(this)
        changeVolImg.setOnClickListener(this)
        volUp.setOnClickListener(this)
        volDown.setOnClickListener(this)
        changeEpisode.setOnClickListener(this)

        rootLay.setOnTouchListener { _: View?, _: MotionEvent? -> true }
        listenCast()

        val itemWidth =
            (VideoUtil.dp2px(selectPanelLayWidth) - VideoUtil.dp2px(7f) * (5 - 1)
                    - VideoUtil.dp2px(80f)) / 5
        espAdapter = PlayerCoverEpisodeAdapter(
            context, episodeList, PlayerHelper.CHOOSEN_EPISODE_INDEX, itemWidth
        )
        //连接投屏设备
        LeCast.getInstance().connect(LeCast.getInstance().infos[this.index])
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cast_back -> {
                notifyReceiverEvent(DataInter.Event.EVENT_SELECT_REMOVE_CAST, null)
            }
            R.id.start_play_img -> {
                if (LeCast.getInstance().status == IUIUpdateListener.STATE_PAUSE) {
                    LeCast.getInstance().resume()
                    mStateIcon.isSelected = false
                } else {
                    LeCast.getInstance().pause()
                    mStateIcon.isSelected = true
                }
            }
            R.id.cast_cover_next -> {//下一集
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_NEXT_ESP, null)
            }
            R.id.quit_cast -> {
                CAST_SCREEN = 0
                LeCast.getInstance().stop()
                notifyReceiverEvent(DataInter.Event.EVENT_SELECT_REMOVE_CAST, null)
            }
            R.id.change_cast -> {
                val bundle = Bundle()
                bundle.putInt(DataInter.Key.KEY_CAST_ACTION, 1) //换设备
                notifyReceiverEvent(DataInter.Event.EVENT_SELECT_REMOVE_CAST, bundle)
            }
            R.id.change_vol_img -> { //改变音量
                if (change_vol_layout.visibility == View.VISIBLE)
                    change_vol_layout.visibility = View.INVISIBLE
                else
                    change_vol_layout.visibility = View.VISIBLE
            }
            R.id.change_vol_up -> {
                LeCast.getInstance().voulumeUp();
            }
            R.id.change_vol_down -> {
                LeCast.getInstance().voulumeDown();
            }
            R.id.cast_cover_episode -> {
                changeEpisodeView()
            }
        }
    }


    //剧集
    private fun changeEpisodeView() {
        select_panel_lay.visibility =
            if (select_panel_lay.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        val episodeView = LayoutInflater.from(context)
            .inflate(R.layout.player_cover_layout_episode, null)
        val listView: RecyclerView = episodeView.findViewById(R.id.recycler_view)
        listView.layoutManager = GridLayoutManager(context, 5)
        listView.scrollToPosition(PlayerHelper.CHOOSEN_EPISODE_INDEX)
        val spacing = VideoUtil.dp2px(7f)
        listView.addItemDecoration(GridSpacingItemDecoration2(5, spacing, true))
        listView.adapter = espAdapter
        select_panel_lay.addView(episodeView)
        setSelectPanelAnim(true)
        if (espAdapter != null) {
            espAdapter!!.selected = PlayerHelper.CHOOSEN_EPISODE_INDEX
            espAdapter!!.notifyDataSetChanged()
            espAdapter!!.setOnItemClicklistener { position ->
                val bundle = BundlePool.obtain()
                bundle.putInt(KEY_PLAY_ESP, position)
                notifyReceiverEvent(DataInter.Event.EVENT_CODE_CHANGE_ESP, bundle)
                espAdapter!!.selected = position
                espAdapter!!.notifyDataSetChanged()
                setSelectPanelAnim(false)
            }
        }
    }

    private fun listenCast() {
        LeCast.getInstance().setUIUpdateListener(object : IUIUpdateListener {
            override fun onUpdateState(state: Int, `object`: Any?) {
                when (state) {
                    IUIUpdateListener.STATE_CONNECT_SUCCESS -> {
                        Log.d(TAG, "callback connet 链接成功")
                        if (!TextUtils.isEmpty(dataSource!!.data)) {
                            airplayOk("${dataSource!!.id}")
                            LeCast.getInstance()
                                .playNetMedia(
                                    JSON.toJSONString(dataSource!!.extra),
                                    dataSource!!.data,
                                    LelinkPlayerInfo.TYPE_VIDEO
                                )
                        }
                    }
                    IUIUpdateListener.STATE_PLAY -> {
                        Log.d(TAG, "callback play 开始播放")
                        if (CAST_SCREEN != 1)//首次投屏使用播放器已有进度,再次投屏从头开始
                            LeCast.getInstance().seekTo(
                                (context as PlayerWindowActivity).mAssist!!.currentPosition / 1000
                            )
                        CAST_SCREEN = 1 //投屏状态
                        mStateIcon.isSelected = false
                        mSeekBar.isEnabled = true
                    }
                    IUIUpdateListener.STATE_PAUSE -> {
                        mStateIcon.isSelected = true
                    }
                    IUIUpdateListener.STATE_SEEK -> {
                        Log.d(TAG, "callback seek完成:$`object`")
                    }
                    IUIUpdateListener.STATE_PLAY_ERROR -> {
                        Log.d(TAG, "callback error 播放错误:$`object`")
                    }
                    IUIUpdateListener.STATE_POSITION_UPDATE -> {
                        Log.d(TAG, "callback position update:$`object`")
                        val arr = `object` as LongArray
                        val duration = arr[0]
                        val position = arr[1]
                        Log.d(TAG, "position update 总长度：$duration 当前进度:$position")
                        updateUI(position.toInt(), duration.toInt())
                    }
                    IUIUpdateListener.STATE_COMPLETION -> {
                        Log.d(TAG, "callback completion 播放完成")
                        notifyReceiverEvent(DataInter.Event.EVENT_CODE_NEXT_ESP, null)
                    }
                }
            }

            override fun onUpdateText(msg: String?) {
            }
        })
    }

    private fun seekCast(position: Int) {
        LeCast.getInstance().seekTo(position)
    }

    override fun onReceiverBind() {
        super.onReceiverBind()
        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        groupValue.registerOnGroupValueUpdateListener(mOnGroupValueUpdateListener)
    }

    override fun onCoverAttachedToWindow() {
        super.onCoverAttachedToWindow()
        dataSource = groupValue.get(DataInter.Key.KEY_DATA_SOURCE)
        setTitle(dataSource)
    }

    override fun onReceiverUnBind() {
        super.onReceiverUnBind()
        groupValue.unregisterOnGroupValueUpdateListener(mOnGroupValueUpdateListener)
        mHandler.removeCallbacks(mSeekEventRunnable)
    }

    private val mOnGroupValueUpdateListener: OnGroupValueUpdateListener =
        object : OnGroupValueUpdateListener {
            override fun filterKeys(): Array<String> {
                return arrayOf(
                    DataInter.Key.KEY_DATA_SOURCE,
                    DataInter.Key.KEY_CASTDEVICE_INDEX
                )
            }

            override fun onValueUpdate(key: String, value: Any) {
                if (key == DataInter.Key.KEY_DATA_SOURCE) {
                    dataSource = value as DataSource
                    Log.d("dataSource", "dataSource: " + dataSource!!.data)
                    setEspData(dataSource!!)
                } else if (key == DataInter.Key.KEY_CASTDEVICE_INDEX) {
                    Log.d("device_index", "value: " + value as Int)
                    //连接投屏设备
                    LeCast.getInstance().connect(LeCast.getInstance().infos[value])
                }
            }
        }

    private val onSeekBarChangeListener: OnSeekBarChangeListener =
        object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int, fromUser: Boolean
            ) {
                if (fromUser) updateUI(progress, seekBar.max)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendSeekEvent(seekBar.progress)
            }
        }


    private fun sendSeekEvent(progress: Int) {
        mSeekProgress = progress
        mHandler.removeCallbacks(mSeekEventRunnable)
        mHandler.postDelayed(mSeekEventRunnable, 300)
    }

    private val mSeekEventRunnable = Runnable {
        if (mSeekProgress < 0) return@Runnable
        seekCast(mSeekProgress) //cast seek
    }

    private fun setCurrTime(curr: Int) {
        mCurrTime.text = TimeUtil.stringForTimeSec(curr.toLong())
    }

    private fun setTotalTime(duration: Int) {
        mTotalTime.text = TimeUtil.stringForTimeSec(duration.toLong())
    }

    private fun setSeekProgress(curr: Int, duration: Int) {
        mSeekBar.max = duration
        mSeekBar.progress = curr
    }

    override fun onTimerUpdate(curr: Int, duration: Int, bufferPercentage: Int) {
//        updateUI(curr, duration);
    }

    //单位秒
    private fun updateUI(curr: Int, duration: Int) {
        setSeekProgress(curr, duration)
        setCurrTime(curr)
        setTotalTime(duration)
    }

    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when (eventCode) {
            OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET -> {
                updateUI(0, 0)
                val data = bundle!!.getSerializable(EventKey.SERIALIZABLE_DATA) as DataSource
                groupValue.putObject(DataInter.Key.KEY_DATA_SOURCE, data)
                setTitle(data)
            }
            OnPlayerEventListener.PLAYER_EVENT_ON_STATUS_CHANGE -> {
                val status = bundle!!.getInt(EventKey.INT_DATA)
                if (status == IPlayer.STATE_PAUSED) {
                    mStateIcon.isSelected = true
                } else if (status == IPlayer.STATE_STARTED) {
                    mStateIcon.isSelected = false
                }
            }
        }
    }

    private fun setTitle(dataSource: DataSource?) {
        if (dataSource != null) {
            val title = dataSource.title
            castTitle.text = title
        }
    }

    override fun onPrivateEvent(eventCode: Int, bundle: Bundle?): Bundle? {
        when (eventCode) {
            DataInter.PrivateEvent.EVENT_CODE_UPDATE_SEEK -> if (bundle != null) {
                val curr = bundle.getInt(EventKey.INT_ARG1)
                val duration = bundle.getInt(EventKey.INT_ARG2)
                //                    updateUI(curr, duration);
            }
        }
        return null
    }

    private fun setEspData(data: DataSource) {
        episodeList.clear()
        if (data.espList != null && data.espList.size > 0) {
            for (videoPlay in data.espList) {
                episodeList.add(videoPlay as VideoPlay)
            }
        }
        espAdapter?.notifyDataSetChanged()
    }

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onCreateCoverView(context: Context?): View {
        return View.inflate(context, R.layout.player_cover_layout_cast_view, null)
    }

    override fun onEndGesture() {
    }

    override fun onSingleTapUp(event: MotionEvent?) {
    }

    override fun onDown(event: MotionEvent?) {
    }

    override fun onDoubleTap(event: MotionEvent?) {
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) {
    }


    private fun setSelectPanelAnim(push: Boolean) {
        select_panel_lay.clearAnimation()
        cancelSelectPanelAnimator()
        selectPanelAnimator = if (push) {
            val startP = VideoUtil.dp2px(selectPanelLayWidth).toFloat()
            ObjectAnimator.ofFloat(
                select_panel_lay,
                "translationX", startP, 0f
            ).setDuration(250)
        } else {
            val endP = VideoUtil.dp2px(selectPanelLayWidth).toFloat()
            ObjectAnimator.ofFloat(
                select_panel_lay,
                "translationX", 0f, endP
            ).setDuration(250)
        }
        selectPanelAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (!push) {
                    select_panel_lay.visibility = View.GONE
                }
            }
        })
        selectPanelAnimator!!.start()
    }

    private fun cancelSelectPanelAnimator() {
        if (selectPanelAnimator != null) {
            selectPanelAnimator!!.cancel()
            selectPanelAnimator!!.removeAllListeners()
            selectPanelAnimator!!.removeAllUpdateListeners()
        }
    }

}