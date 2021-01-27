package com.pcommon.lib_common.application


import android.app.Application
import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import java.util.*

@Keep
abstract class BaseAbstractApplication : Application() {
    private var logicClasses: MutableList<Class<out AbstractApplication>>? = null
    private var logics: MutableList<AbstractApplication>? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        logicClasses = ArrayList()
        logics = ArrayList()
        initModuleApplication()
        logicAttach(base)
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        application = this
        logicCreate()
    }

    override fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        super.registerActivityLifecycleCallbacks(callback)
        for (aClass in logics!!) {
            aClass.registerActivityLifecycleCallbacks(callback)
        }
    }

    /**
     * 在主Module的application中实现
     * 在实现的方法中调用registerBaseApplicationLogic（）以注册各个Module中的Application到logicClasses中
     */
    protected abstract fun initModuleApplication()

    fun registerApplicationLogic(vararg logicClass: Class<out AbstractApplication>) {
        logicClasses!!.addAll(logicClass)
    }

    fun registerApplicationLogic(vararg logicClassName: String) {
        try {
            for (name in logicClassName) {
                val clz = Class.forName(name)

                if (clz.superclass == AbstractApplication::class.java) {
                    try {
                        registerApplicationLogic(clz as Class<AbstractApplication>)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    throw Exception("this class is not direct child of AbstractApplication class")
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun logicCreate() {
        for (aClass in logics!!) {
            aClass.setApplication(this)
            aClass.onCreate()
        }
    }

    private fun logicAttach(base: Context) {
        for (aClass in logicClasses!!) {
            try {
                val baseApplicationLogic = aClass.newInstance()
                baseApplicationLogic.attachBaseContext(base)
                logics!!.add(baseApplicationLogic)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        lateinit var application: Context
            private set
    }
}
