package com.pcommon.lib_common


import android.accounts.NetworkErrorException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.GsonUtils
import com.elvishew.xlog.XLog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcommon.lib_network.entity.BaseRespEntity
import com.pcommon.lib_utils.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException


open class ObserverImpl<T>(data: MutableLiveData<T>, private var clazz: Class<T>) : Observer<T> {
    private val TAG = "ObserverImpl"
    private var liveData: MutableLiveData<T>? = data

    override fun onNext(t: T) {
        liveData?.postValue(t)

    }

    override fun onError(e: Throwable) {
        //  e.printStackTrace()
        XLog.e(
            "ObserverImpl onError() called:" + genName() + "\n Exception:" + Util.getExceptionContent(
                e
            )
        )
        if (checkAuth(e)) {
            liveData?.postValue(buildErrorData(e, clazz))
        }
    }

    override fun onComplete() {}

    override fun onSubscribe(d: Disposable) {}

    private fun genName(): String {
        return javaClass.name + hashCode()
    }


    private fun checkAuth(e: Throwable): Boolean {
        if (e is HttpException) {
            if (e.code() == 401) {
                LiveEventBus.get().with(AUTHORIZATION_EXPIRED).post(AUTHORIZATION_EXPIRED)
                cont++
                return false
            }
        }
        return true
    }

    companion object {
        var cont = 0
        const val AUTHORIZATION_EXPIRED = "AUTHORIZATION_EXPIRED"

        @RequiresApi(Build.VERSION_CODES.ECLAIR)
        private fun <T> buildErrorData(e: Throwable, clazz: Class<T>): T {
            var code = -200
            val tip = when (e) {
                is SocketTimeoutException -> {
                    "连接服务器超时!"
                }
                is ConnectException -> {
                    code = -400
                    "连接服务器失败!"
                }
                is HttpException -> {
                    code = e.code()
                    e.message()
                }
                else -> {
                    e.localizedMessage
                }
            }
            val baseEntity = BaseRespEntity<T>()
            baseEntity.message = tip
            baseEntity.resultCode = code
            return GsonUtils.fromJson<T>(GsonUtils.toJson(baseEntity), clazz)
        }
    }

}