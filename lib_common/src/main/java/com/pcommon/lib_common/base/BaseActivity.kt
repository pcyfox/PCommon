package com.pcommon.lib_common.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.elvishew.xlog.XLog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcommon.lib_common.R
import com.pcommon.lib_common.base.BaseActivity.Click.SPACE_TIME
import com.pcommon.lib_common.base.BaseActivity.Click.hash
import com.pcommon.lib_common.base.BaseActivity.Click.lastClickTime
import com.pcommon.lib_common.ext.postDelayed
import com.pcommon.lib_common.ext.toast
import com.pcommon.lib_common.receiver.NetWorkChangReceiver
import com.pcommon.lib_common.utils.MaskProgressDialog
import com.pcommon.lib_utils.CloseBarUtil
import com.pcommon.lib_utils.EventDetector
import com.pcommon.lib_utils.MaskUtils


/**
 * @author pcy
 * @package
 * @describe Activity 基类
 */

@Keep
abstract class BaseActivity<VDB : ViewDataBinding, VM : BaseViewModel>(var vmClass: Class<VM>? = null) : FragmentActivity() {
    private val TAG = "BaseActivity"
    protected var viewModel: VM? = null
        private set
    protected var viewDataBinding: VDB? = null
        private set
    private var onKeyDownListeners: ArrayList<OnKeyDownListener>? = null
    protected abstract val layoutId: Int
    private val eventDetector by lazy { EventDetector(3, 1800) }
    open var isDoubleClickExit = false
    open var mainViewModelId = -1
    open var isFullScreen = true
    open var isClickBack = true
    private var progress: MaskProgressDialog? = null
    private var isBeenHiddenProgress = false//标记菊花转是否被关闭

    open fun createViewModel(): VM {
        return BaseViewModel(application) as VM
    }

    private fun createViewModel(clazz: Class<VM>): VM {
        return getViewModel(clazz)
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (isFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            //全屏
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
            hideNavigationBar()
        }
        super.onCreate(savedInstanceState)

        initViewDataBinding()
        observeData()
        initData()
        initView()
        initListener()
        request()
    }


    protected fun showProgress(delay: Long) {
        if (delay < 0) {
            return
        }
        postDelayed(delay) {
            showProgress()
        }
    }

    protected fun hideProgress(delay: Long) {
        isBeenHiddenProgress = true
        if (progress != null && progress!!.isShowing) {
            hideProgress()
        } else {
            postDelayed(delay) {
                hideProgress()
            }
        }
    }


    protected fun showProgress() {
        if (isBeenHiddenProgress) {
            progress?.dismiss()
            return
        }
        if (progress == null) {
            try {
                progress = MaskProgressDialog(this, object : MaskProgressDialog.DialogListener {
                    override fun onDismiss() {
                        isBeenHiddenProgress = false
                        onProgressClosed()
                    }

                    override fun onCancelClick() {
                        isBeenHiddenProgress = false
                        onProgressClosed()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        progress?.show()
        isBeenHiddenProgress = false
    }

    protected fun hideProgress() {
        isBeenHiddenProgress = true
        progress?.dismiss()
    }

    protected fun startTimeOutTimer(time: Long) {
        postDelayed(time, Runnable {
            onTimeOut(time)
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onCreateOver()
    }

    override fun onPostResume() {
        super.onPostResume()
        onResumeOver()
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel?.run {
            lifecycle.removeObserver(this)
        }
        onKeyDownListeners?.clear()

        progress?.run {
            listener = null
            progress?.dismiss()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (onKeyDownListeners == null) {
            return super.onKeyDown(keyCode, event)
        }
        for (listener in onKeyDownListeners!!) {
            return listener.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (!isClickBack) {
            return
        }
        if (isDoubleClickExit) {
            eventDetector.addEvent()
            if (eventDetector.timesLack - 1 != 0) {
                toast(String.format(getString(R.string.common_click_more_will_finish), eventDetector.timesLack - 1))
            } else if (eventDetector.timesLack == 1) {
                if (!onDoubleClickOverIntercept()) {
                    finish()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    fun registerOnKeyDownListener(vararg listener: OnKeyDownListener) {
        if (onKeyDownListeners == null) {
            onKeyDownListeners = ArrayList()
        }
        onKeyDownListeners!!.addAll(listener)
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding() {
        viewModel = if (vmClass != null) {
            createViewModel(vmClass!!)
        } else {
            createViewModel()
        }
        if (viewModel != null) {
            //让ViewModel拥有View的生命周期感应
            lifecycle.addObserver(viewModel!!)
        }
        //DataBindingUtil类需要在project的build中配置 dataBinding {enabled true }, 同步后会自动关联android.databinding包
        viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        //注入Lifecycle生命周期
        viewDataBinding?.lifecycleOwner = this
        if (mainViewModelId != -1) {
            viewDataBinding?.setVariable(mainViewModelId, viewModel)
        }
    }


    protected fun <T : BaseViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(this).get(clazz)
    }


    fun addVariable(wm: VM?, vararg ids: Int) {
        wm?.let {
            for (id in ids) {
                viewDataBinding?.setVariable(id, wm)
            }
        }
    }

    open fun onDoubleClickOverIntercept(): Boolean {
        return false
    }

    open fun beforeDoubleClickToFinish() {}


    open fun initData() {

    }

    open fun request() {

    }

    open fun observeData() {
        LiveEventBus.get().with(NetWorkChangReceiver.NetWorkChangEvent::class.java.simpleName, NetWorkChangReceiver.NetWorkChangEvent::class.java).observe(this, {
            onNetWorkChange(it.isAvailable)
        })
    }

    open fun initListener() {

    }

    open fun onTimeOut(time: Long) {}


    open fun initView() {}

    open fun onCreateOver() {
    }

    open fun onResumeOver() {
    }

    open fun onProgressClosed() {}

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    protected fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT in 12..18) {
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = uiOptions
        }
        CloseBarUtil.hideBottomUIMenu(window);
    }


    object Click {
        var hash: Int = 0
        var lastClickTime: Long = 0
        const val SPACE_TIME: Long = 900
    }

    infix fun safeClick(clickAction: () -> Unit): Boolean {
        if (this.hashCode() != hash) {
            hash = this.hashCode()
            lastClickTime = System.currentTimeMillis()
            clickAction()
            return true
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > SPACE_TIME) {
                lastClickTime = System.currentTimeMillis()
                clickAction()
                return true
            }
        }
        return false
    }

    open fun getContext(): Context {
        return this
    }

    open fun onNetWorkChange(isAvailable: Boolean) {
        XLog.i("$TAG:onNetWorkChange() called with: isAvailable = $isAvailable")
        //网络异常悬浮窗
        if (!isAvailable) {
            if (!window.decorView.isInvisible) {
                return
            }
            if (window.isActive) {
                MaskUtils.hide(this, this)
                MaskUtils.show(window, R.layout.layout_toast_no_available_network_tip, this)
            }
        } else {
            MaskUtils.hide(this, this)
        }
    }

}
