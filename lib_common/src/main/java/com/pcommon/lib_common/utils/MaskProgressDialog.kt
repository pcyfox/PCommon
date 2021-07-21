package com.pcommon.lib_common.utils

import android.content.Context
import android.view.View
import com.pcommon.lib_common.R
import com.pcommon.lib_common.base.BaseFragmentDialog
import com.pcommon.lib_common.ext.getColorExt
import kotlinx.android.synthetic.main.common_layout_progress.*


class MaskProgressDialog constructor(ct: Context, var listener: DialogListener?) :
    BaseFragmentDialog(layoutId = R.layout.common_layout_progress, ct = ct) {

    override fun initView() {
        avi_loading.setIndicatorColor(activity!!.getColorExt(R.color.common_colorPrimaryDark))
        avi_loading.setIndicator("com.wang.avi.indicators.BallSpinFadeLoaderIndicator")
        teach_close_progress.setOnClickListener {
            listener?.onCancelClick()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        avi_loading.show()
        avi_loading.visibility = View.VISIBLE
    }


    override fun onDismiss() {
        avi_loading?.hide()
        listener?.onDismiss()
        avi_loading.visibility = View.GONE
    }


    fun setTips(tips: String?) {
        tv_dialog_tips.run {
            visibility = if (tips.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            text = tips
        }
    }

    interface DialogListener {
        fun onDismiss()
        fun onCancelClick()
    }


}