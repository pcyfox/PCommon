package com.pcommon.lib_utils

import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.Gson
import java.io.*

/**
 * 通过json文件创建java对象
 */
object LoadLocationDataUtils {
    fun <T> loadData(path: String, clazz: Class<T>): T? {
        var reader: BufferedReader? = null
        try {
            val input: InputStream = FileInputStream(File(path))
            reader = BufferedReader(InputStreamReader(input))
            val jsonString = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonString.append(line)
            }
            val data = Gson().fromJson(jsonString.toString(), clazz)
            if (data != null) {
                return data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    fun saveObjectToSD(data: Any?, path: String) {
        val file = File(path)
        if (file.exists() && file.canRead()) {
            file.delete()
        }
        var fWriter: FileWriter? = null
        try {
            fWriter = FileWriter(path, false)
            val json = GsonUtils.toJson(data)
            fWriter.write(json)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                if (fWriter != null) {
                    fWriter.flush()
                    fWriter.close()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

}