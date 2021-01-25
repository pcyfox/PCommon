package com.pcommon.lib_common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner


/**
 * ViewModel 有三个功能,
 * 第一,可以使 ViewModel 以及 ViewModel 中的数据在屏幕旋转或配置更改引起的 Activity 重建时存活下来,重建后数据可继续使用,
 * 第二,功能可以帮助开发者轻易实现 Fragment 与 Fragment 之间, Activity 与 Fragment 之间的通讯以及共享数据
 * 第三,可以用于与Xml间实现DataBinding
 * @author LN
 */

open class BaseViewModel(application: Application) : AndroidViewModel(application), BaseLifecycleObserver, Observable {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    override fun onAny(owner: LifecycleOwner, event: Lifecycle.Event) {

    }

    override fun onCreate() {

    }

    override fun onDestroy() {

    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    @Suppress("unused")
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }


    fun startActivityInNewTask(ct: Context, to: Class<out Activity>) {
        ct.startActivity(Intent().run {
            setClass(ct, to)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }




}
