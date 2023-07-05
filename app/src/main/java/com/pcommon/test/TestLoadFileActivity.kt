package com.pcommon.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.pcommon.lib_utils.LoadLocationDataUtils
import kotlinx.android.synthetic.main.activity_test_load_file.*

data class TestBean(val name: String, val time: Long)


class TestLoadFileActivity : AppCompatActivity() {
   private var isZip=false
    private val TAG = "TestLoadFileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_load_file)
        PermissionUtils.permission(PermissionConstants.STORAGE)
            .callback { isAllGranted, granted, deniedForever, denied ->
                Log.d(
                    TAG,
                    "callback() called with: isAllGranted = $isAllGranted, granted = $granted, deniedForever = $deniedForever, denied = $denied"
                )
            }.request()
    }

    private val dest = "/sdcard/testBean.bak"

    fun onWriteClick(view: View) {
        val testBean = TestBean("testBean", System.currentTimeMillis())
        LoadLocationDataUtils.saveObjectToSD(testBean, dest, isZip)
    }

    fun onReadClick(view: View) {
        LoadLocationDataUtils.loadData(dest, TestBean::class.java, isZip)?.run {
            Log.d(TAG, "onReadClick() called read:$this")
            tvText.text = this.toString()
        }
    }
}