package com.pcommon.lib_network

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

class OkHttpDns constructor(private val timeout:Long): Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            val task = FutureTask(
                    Callable { listOf(*InetAddress.getAllByName(hostname)) })
            Thread(task).start()
            task.get(timeout, TimeUnit.SECONDS)
        } catch (var4: Exception) {
            val unknownHostException = UnknownHostException("Broken system behaviour for dns lookup of $hostname")
            unknownHostException.initCause(var4)
            throw unknownHostException
        }
    }
}