package com.pcommon.lib_common.manager

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.SPUtils

class SettingManager private constructor() : Cloneable {
    private val tag = "SettingManager"
    var settingManagerData = MutableLiveData<SettingManager>()
    var changeType = ""
    var isNotifyDataAfterSetData = true//当数据设置后是否发出通知

    init {
        SPStaticUtils.setDefaultSPUtils(SPUtils.getInstance("xtv"))
    }

    @Throws(CloneNotSupportedException::class)// 克隆失败抛出异常
    override
    fun clone(): SettingManager {
        return super.clone() as SettingManager // 类型强制转换
    }

}
