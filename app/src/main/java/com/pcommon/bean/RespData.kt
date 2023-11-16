package com.pcommon.bean

class RespData<T> {
    var code: Int = -1
    var message: String? = ""
    var data: T? = null

    fun isOk() = code == 200
}