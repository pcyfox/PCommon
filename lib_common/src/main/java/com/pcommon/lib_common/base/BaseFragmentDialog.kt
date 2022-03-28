package com.pcommon.lib_common.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialog
import com.blankj.utilcode.util.ScreenUtils


@Keep
open class BaseFragmentDialog(
    @LayoutRes val layoutId: Int,
    var widthPercent: Float = 1.0f,
    var heightPercent: Float = 1.0f,
    private val cancelable: Boolean = false,
    val ct: Context?
) : AppCompatDialog(ct) {
    protected var activity: Activity? = null
    init {
        if (ct is Activity) {
            activity = ct
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity == null && ownerActivity != null) {
            activity = ownerActivity
        }
        setContentView(layoutId)
        initData()
        initView()
    }


    override fun onStart() {
        super.onStart()
        val lp = window?.attributes
        lp?.width = (ScreenUtils.getScreenWidth() * widthPercent).toInt()
        lp?.height = (ScreenUtils.getScreenHeight() * heightPercent).toInt()
        window?.attributes = lp
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(cancelable)
    }

    open fun initView() {
    }

    open fun initData() {
    }
    open fun onDismiss() {}

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun dismiss() {
        super.dismiss()
        onDismiss()
        window?.decorView?.run {
            if (isAttachedToWindow) {
                fullScreenImmersive(this)
            }
        }
    }

    private fun fullScreenImmersive(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}