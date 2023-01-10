package com.pcommon.lib_common.ext

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.pcommon.lib_common.R
import com.pcommon.lib_utils.AnimUtil

/**
 * 所有对Activity扩展的方法只能在这里定义，好统一管理！
 */
fun Activity.autoScaleViewOnFocus(
    radioX: Float,
    radioY: Float,
    duration: Long,
    @NonNull vararg views: View
) {
    val rx = if (radioX <= 0) 1.0f else radioX
    val ry = if (radioY <= 0) 1.0f else radioY
    for (view in views) {
        view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                AnimUtil.scaleAnim(v, rx, ry, duration)
            } else {
                AnimUtil.scaleAnim(v, 1.0f, 1.0f, duration)
            }
        }
    }
}


fun Activity.startActivityExt(
    activity: Class<out Activity>,
    isFinishCurrentActivity: Boolean = false,
    bundle: Bundle? = null
) {
    startActivity(Intent(this, activity).apply {
        if (bundle != null)
            putExtras(bundle)
    })
    if (isFinishCurrentActivity) finish()
}


fun Activity.defaultScaleViewOnFocus(ratio: Float, @NonNull vararg views: View) {
    autoScaleViewOnFocus(1 + ratio, 1 + ratio, 200, *views)
}

fun Activity.defaultScaleViewXOnFocus(ratio: Float, @NonNull vararg views: View) {
    autoScaleViewOnFocus(1 + ratio, 1f, 200, *views)
}

fun Activity.defaultScaleViewYOnFocus(ratio: Float, @NonNull vararg views: View) {
    autoScaleViewOnFocus(1f, 1 + ratio, 200, *views)
}


fun Activity.setViewsGone(@NonNull vararg views: View) {
    setViewVisibility(View.GONE, *views)
}

fun Activity.setViewsVisible(vararg views: View?) {
    setViewVisibility(View.VISIBLE, *views)
}

@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
fun Activity.setViewsAlpha(alpha: Float, vararg views: View?) {
    views.forEach {
        it?.alpha = alpha
    }
}

fun Activity.setViewsClickable(isClickable: Boolean, vararg views: View?) {
    views.forEach {
        it?.isClickable = isClickable
    }
}

fun Activity.setViewsInvisible(vararg views: View?) {
    setViewVisibility(View.INVISIBLE, *views)
}

fun Activity.setViewsVisible(gone: Boolean, @NonNull vararg views: View) {
    for (view in views) {
        if (gone) {
            setViewsGone(view)
        } else {
            setViewsVisible(view)
        }
    }
}


fun Activity.setViewVisibility(visibility: Int, vararg views: View?) {
    views.forEach {
        if (it != null && it.visibility != visibility) {
            it.visibility = visibility
        }
    }
}


fun Activity.post(action: Runnable) {
    this.window.decorView.post(action)
}

fun Activity.postDelayed(delayMillis: Long, action: Runnable) {
    this.window.decorView.postDelayed(action, delayMillis)
}

fun Activity.postDelayed(action: Runnable, delayMillis: Long) {
    this.window.decorView.postDelayed(action, delayMillis)
}

fun Activity.getContentView(): View {
    return this.findViewById(android.R.id.content)
}


fun Activity.setTextColor(textView: TextView?, @ColorRes color: Int) {
    textView?.setTextColor(ContextCompat.getColor(this, color))
}

fun Activity.getDrawableExt(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}


fun Activity.toast(text: String) {
    toastNormal(text)
}

fun Activity.toast(@StringRes textRes: Int) {
    getString(textRes).run {
        if (isNotBlank()) {
            toastNormal(this)
        }
    }
}

fun Activity.toast(any: Any?) {
    any?.run {
        val s = toString()
        if (s.isNotBlank()) {
            toastNormal(s)
        }
    }
}

fun Activity.toastLong(text: String) {
    toastNormal(text, true)
}

fun Activity.toastLong(@StringRes textRes: Int) {
    getString(textRes).run {
        if (isNotBlank()) {
            toastNormal(this, true)
        }
    }
}

fun Activity.toastNormal(
    text: String?,
    isLong: Boolean = true,
    layoutId: Int = R.layout.common_layout_toast_normal
) {
    if (text.isNullOrEmpty()) {
        return
    }
    val view =
        LayoutInflater.from(applicationContext).inflate(layoutId, null)
    view.findViewById<TextView>(R.id.view_toast_tip).text = text
    ToastUtils.make().setDurationIsLong(isLong).show(view)
}

fun Activity.toastTip(
    text: String? = "", isLong: Boolean = true,
    layoutId: Int = R.layout.common_layout_toast_normal
) {
    if (text.isNullOrEmpty()) {
        return
    }
    val view =
        LayoutInflater.from(applicationContext).inflate(layoutId, null)
    view.findViewById<TextView>(R.id.view_toast_tip).text = text

    ToastUtils.make().setDurationIsLong(isLong).show(view)
}

fun Activity.toastOk(
    text: String? = "",
    isLong: Boolean = true,
    layoutId: Int = R.layout.common_layout_toast_ok
) {
    if (text.isNullOrEmpty()) {
        return
    }
    val view =
        LayoutInflater.from(applicationContext).inflate(layoutId, null)
    view.findViewById<TextView>(R.id.view_toast_tip).text = text
    ToastUtils.make().setDurationIsLong(isLong).show(view)
}

fun Activity.toastFail(
    text: String? = "",
    isLong: Boolean = true,
    layoutId: Int = R.layout.common_layout_toast_fail
) {
    if (text.isNullOrEmpty()) {
        return
    }
    val view =
        LayoutInflater.from(applicationContext).inflate(layoutId, null)
    view.findViewById<TextView>(R.id.view_toast_tip).text = text
    ToastUtils.make().setDurationIsLong(isLong).show(view)
}


fun Activity.getColorExt(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Activity.setChildViewEnable(viewGroup: ViewGroup?, enable: Boolean) {
    viewGroup?.children?.forEach {
        it.isEnabled = enable
    }
}


fun Activity.setChildViewClickable(viewGroup: ViewGroup?, clickable: Boolean) {
    viewGroup?.children?.forEach {
        it.isClickable = clickable
    }
}


fun Activity.dp2px(dp: Float): Int = SizeUtils.dp2px(dp)
fun Activity.px2dp(px: Float): Int = SizeUtils.px2dp(px)








