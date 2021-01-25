package com.pcommon.lib_common.update

import android.content.Context
import com.pcommon.lib_common.base.BaseFragmentDialog
import kotlinx.android.synthetic.main.common_layout_dialog_update.*
import com.pcommon.lib_common.R

class UpdateAppDialog(ct: Context, var title: String) : BaseFragmentDialog(layoutId = R.layout.common_layout_dialog_update, widthPercent = 0.5f, heightPercent = 0.6f, ct = ct) {
    var callback: DismissCallback? = null
    override fun initView() {
        tvVersionName.setText(title)
    }

    fun setProgessTip(text: String?) {
        tvText.setText(text)
    }

    override fun onDismiss() {
        callback?.onDismiss()
    }


    interface DismissCallback {
        fun onDismiss()
    }
}