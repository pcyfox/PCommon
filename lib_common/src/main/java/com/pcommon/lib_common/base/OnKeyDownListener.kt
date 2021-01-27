package com.pcommon.lib_common.base

import android.view.KeyEvent
import androidx.annotation.Keep

@Keep
interface OnKeyDownListener {
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}