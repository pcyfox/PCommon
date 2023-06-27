package com.pcommon.lib_common.base

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.elvishew.xlog.XLog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcommon.lib_common.R
import com.pcommon.lib_common.ext.postDelayed
import com.pcommon.lib_common.ext.toast
import com.pcommon.lib_common.receiver.NetWorkChangEvent
import com.pcommon.lib_common.receiver.NetWorkChangReceiver
import com.pcommon.lib_common.utils.DisplayUtil
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
abstract class BaseActivity<VDB : ViewDataBinding, VM : BaseViewModel>(var vmClass: Class<VM>? = null) :
    FragmentActivity() {
    private val TAG = "BaseActivity"

    var viewModel: VM? = null
        private set

    var viewDataBinding: VDB? = null
        private set

    protected abstract val layoutId: Int

    private var onKeyDownListeners: ArrayList<OnKeyDownListener>? = null
    private val eventDetector by lazy { EventDetector(3, 1800) }
    private var isNeedHiddenProgress = false
    private var progressDialog: MaskProgressDialog? = null


    open var isShowNetWorkChangNotice = true
    open var isDoubleClickExit = false
    open var mainViewModelId = -1
    open var isFullScreen = true
    open var isClickBack = true
    open var isHideKeyboardWhenTouchOutside = true
    open var progressDialogLayoutId = -1

    private var fontScale = 1f
    private var showProgressDelay = 0L

    open fun createViewModel(): VM {
        return BaseViewModel(application) as VM
    }

    private fun createViewModel(clazz: Class<VM>): VM {
        return getViewModel(clazz)
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        return DisplayUtil.getResources(this, resources, fontScale)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(DisplayUtil.attachBaseContext(newBase, fontScale))
    }

    open fun setFontScale(fontScale: Float) {
        this.fontScale = fontScale
        DisplayUtil.recreate(this)
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (isFullScreen) {
            fullScreen()
        }
        super.onCreate(savedInstanceState)

        initViewDataBinding()
        observeData()
        initData()
        initView()
        initListener()
        request()
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView<VDB>(this, layoutId)?.apply {
            lifecycleOwner = this@BaseActivity
            if (mainViewModelId != -1) {
                setVariable(mainViewModelId, this)
            }
        }
        if (viewDataBinding == null) {
            Log.e(TAG, "initViewDataBinding() called fail!")
        }

        viewModel = if (vmClass != null) createViewModel(vmClass!!) else {
            createViewModel()
        }.apply {
            lifecycle.addObserver(this)
        }
    }


    fun <T : BaseViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(this).get(clazz)
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        hideNavigationBar()
    }


    @Deprecated("use dismissProgress() instead")
    open fun hideProgress(delay: Long) {
        postDelayed(delay) {
            isNeedHiddenProgress = true
            hideProgress()
        }
    }

    @Deprecated("use dismissProgress() instead", ReplaceWith("dismissProgress()"))
    fun hideProgress() {
        dismissProgress()
    }

    fun isProgressCancelable(isCan: Boolean) {
        progressDialog?.setCancelable(isCan)
    }

    fun isProgressDialogShowing(): Boolean {
        progressDialog?.run {
            return isShowing
        }
        return false
    }

    open fun dismissProgress() {
        isNeedHiddenProgress = true
        if (showProgressDelay > 0) {
            postDelayed(showProgressDelay + 80) {
                progressDialog?.dismiss()
            }
            showProgressDelay = 0
            return
        }
        progressDialog?.dismiss()
    }

    open fun dismissProgress(delay: Long) {
        postDelayed(delay) {
            dismissProgress()
        }
    }


    open fun showProgress() {
        showProgress("", false)
    }

    open fun showProgress(delay: Long = 0, tips: String = "", isCancelable: Boolean = false) {
        if (delay < 0) {
            return
        }
        showProgressDelay = delay
        postDelayed(delay) {
            if (!isNeedHiddenProgress) {
                showProgress(tips, isCancelable)
            }
        }
    }

    fun showProgress(tips: String? = "", isCancelable: Boolean = false) {
        progressDialog?.run {
            if (isShowing) {
                progressDialog!!.setTips(tips)
                return
            }
        }
        if (progressDialog == null) {
            progressDialog =
                MaskProgressDialog(layoutId = if (progressDialogLayoutId > 0) progressDialogLayoutId else R.layout.common_layout_progress,
                    ct = this,
                    listener = object : MaskProgressDialog.DialogListener {
                        override fun onDismiss() {
                            isNeedHiddenProgress = false
                            onProgressClosed()
                        }

                        override fun onCancelClick() {
                            isNeedHiddenProgress = false
                            onProgressClosed()
                        }
                    })
        }
        if (isNeedHiddenProgress) return
        progressDialog?.show(tips, isCancelable)
        isNeedHiddenProgress = false
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
        progressDialog?.run {
            listener = null
            dismiss()
        }
        progressDialog = null

        NetWorkChangReceiver.Helper.getInstance().unregister()
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
                toast(
                    String.format(
                        getString(R.string.common_click_more_will_finish),
                        eventDetector.timesLack - 1
                    )
                )
            } else if (eventDetector.timesLack == 1) {
                if (!onDoubleClickOverIntercept()) {
                    finish()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    open fun beforeDoubleClickToFinish() {}
    open fun initListener() {}
    open fun initView() {}
    open fun initData() {}
    open fun request() {}
    open fun onResumeOver() {}
    open fun onProgressClosed() {}

    fun registerOnKeyDownListener(vararg listener: OnKeyDownListener) {
        if (onKeyDownListeners == null) {
            onKeyDownListeners = ArrayList()
        }
        onKeyDownListeners?.addAll(listener)
    }

    open fun onDoubleClickOverIntercept(): Boolean {
        return false
    }

    open fun onCreateOver() {
        if (isShowNetWorkChangNotice) {
            NetWorkChangReceiver.Helper.getInstance().register(this)
        }
    }

    open fun observeData() {
        if (isShowNetWorkChangNotice) {
            LiveEventBus.get()
                .with(NetWorkChangEvent::class.java.simpleName, NetWorkChangEvent::class.java)
                .observe(this) {
                    onNetWorkChange(it.isAvailable)
                }
        }
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    protected fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT in 12..18) {
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val uiOptions =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = uiOptions
        }
        CloseBarUtil.hideBottomUIMenu(window);
    }


    open fun getContext(): Context {
        return this
    }

    open fun onNetWorkChange(isAvailable: Boolean) {
        XLog.i("$TAG:onNetWorkChange() called with: isAvailable = $isAvailable")
        if (!window.decorView.isEnabled) {
            return
        }
        //网络异常悬浮窗
        if (!isAvailable) {
            MaskUtils.hide(this, this)
            MaskUtils.show(window, R.layout.layout_toast_no_available_network_tip, this)
        } else {
            MaskUtils.hide(this, this)
        }
    }


    /**
     * 重写dispatchTouchEvent
     * 点击软键盘外面的区域关闭软键盘
     *
     * @param ev
     * @return
     */

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isHideKeyboardWhenTouchOutside && ev.action == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，
            currentFocus?.run {
                if (isShouldHideInput(this, ev)) {
                    hideSortKeyboard(this)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun hideSortKeyboard(v: View) {
        //根据判断关闭软键盘
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    /**
     * 判断用户点击的区域是否是输入框
     *
     * @param v
     * @param event
     * @return
     */
    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = (left + v.getWidth())
            // 点击EditText的事件，忽略它。
            return (event.x <= left || event.x >= right || event.y <= top || event.y >= bottom)
        }
        return false
    }

}
