package com.pcommon.lib_utils

import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ZipUtils
import com.google.gson.Gson
import java.io.*

/**
 * 通过json文件创建java对象
 */
object LoadLocationDataUtils {
    private const val TAG = "LoadLocationDataUtils"
    fun <T> loadData(path: String, clazz: Class<T>, isUnZip: Boolean = false): T? {
        var reader: BufferedReader? = null
        try {
            var inputFile = File(path)
            if (!inputFile.isFile || !inputFile.exists()) {
                Log.d(TAG, "loadData() called fail,can not read fail,with: path = $path")
                return null
            }
            val isZip = isUnZip || inputFile.name.endsWith("zip") || inputFile.name.endsWith("zip");
            val tempFile = File(inputFile.parentFile.absolutePath + "/temp")
            if (isZip) {
                if (!tempFile.exists()) tempFile.mkdirs()
                ZipUtils.unzipFile(inputFile, tempFile)?.firstOrNull()?.run {
                    inputFile = this
                }
            }
            val input: InputStream = FileInputStream(inputFile)
            reader = BufferedReader(InputStreamReader(input))
            val jsonString = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonString.append(line)
            }
            if (tempFile.exists()) tempFile.delete()
            return Gson().fromJson(jsonString.toString(), clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun saveObjectToSD(
        data: Any?,
        path: String,
        isZipFile: Boolean = false,
        isDeleteOld: Boolean = true
    ): Boolean {
        if (BuildConfig.DEBUG) Log.d(
            TAG,
            "saveObjectToSD() called with: data = $data, path = $path, isZipFile = $isZipFile"
        )
        val file = File(path)
        var fWriter: FileWriter? = null
        try {
            if (file.exists() && file.canRead()) {
                if (isDeleteOld) file.delete() else return false
            }
            fWriter = FileWriter(path, false)
            val json = GsonUtils.toJson(data)
            fWriter.write(json)
            fWriter.flush()
            if (isZipFile && file.parentFile != null) {
                val tempDir = File(file.parentFile.absolutePath + "/zip/")
                if (!tempDir.exists()) tempDir.mkdirs()
                val zipFile = File(tempDir, file.name)
                val isOk = ZipUtils.zipFile(file, zipFile)
                if (isOk) {
                    FileUtils.copy(zipFile, file)
                    tempDir.delete()
                } else {
                    return false
                }
            }
            return true
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        } finally {
            try {
                fWriter?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

}