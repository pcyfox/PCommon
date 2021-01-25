package com.pcommon.lib_glide

import androidx.annotation.Keep
import com.pcommon.lib_glide.RespCode.OK

@Keep
open class BaseRespEntity<D>(var resultCode: Int = -1,
                             var message: String = "",
                             var data: D? = null) {
    fun isOK() = resultCode == OK
}

object RespCode {
    const val OK = 0
}