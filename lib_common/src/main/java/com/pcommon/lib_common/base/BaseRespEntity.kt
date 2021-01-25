package com.pcommon.lib_common.base

import androidx.annotation.Keep
import com.pcommon.lib_common.base.RespCode.OK
@Keep
open class BaseRespEntity<D>(var resultCode: Int = -1,
                             var message: String = "",
                             var data: D? = null) {
    fun isOK() = resultCode == OK
}

object RespCode {
    const val OK = 0
    const val FTP_ERROR = -100
    const val NGIX_ERROR = -101
}