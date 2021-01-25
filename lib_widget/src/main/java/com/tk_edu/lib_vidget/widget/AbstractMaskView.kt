package com.tk_edu.lib_vidget.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.pcommon.lib_utils.MaskUtils


abstract class AbstractMaskView(protected var activity: Activity, private var viewId: Int) {
    var dismissListener: OnDismissListener? = null
    var view: View? = null
        private set

    var isShowing = false
        private set

    fun show(): AbstractMaskView {
        if (view == null) {
            view = LayoutInflater.from(activity).inflate(viewId, null)
        }
        if (isShowing) {
            return this
        }
        MaskUtils.show(activity, view, this)
        isShowing = true
        onShowView(view!!)
        return this
    }

   open fun remove(): AbstractMaskView {
        if (!isShowing || view == null) {
            return this
        }
        onRemoveView(view!!)
        MaskUtils.hide(activity, this)
        isShowing = false
        dismissListener?.onDismiss()
        return this
    }

    fun callClickByFocus() {
        val currentFocus = activity.currentFocus ?: return
        currentFocus.callOnClick()
    }

    abstract fun onShowView(view: View)
    open fun onRemoveView(view: View){}

    interface OnDismissListener {
        fun onDismiss()
    }
}