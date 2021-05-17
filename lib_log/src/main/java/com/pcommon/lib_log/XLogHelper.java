package com.pcommon.lib_log;

import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.pcommon.lib_log.printer.CloudLogPrinter;

public class XLogHelper {
    private static CloudLogPrinter printer;
    private static final String TAG = "XLogHelper";

    /**
     * @param mCloudLogPrinter 自定义打印前
     * @param LogDir           日志存储路径
     * @param tag              标签
     * @param logRprFileName   日志文件名前缀
     */
    public static void initLog(CloudLogPrinter mCloudLogPrinter, String LogDir, String tag, final String logRprFileName) {
        Log.d(TAG, "initLog() called with: mCloudLogPrinter = [" + mCloudLogPrinter + "], LogDir = [" + LogDir + "], tag = [" + tag + "], prFileName = [" + logRprFileName + "]");
        printer = mCloudLogPrinter;
        tag = TextUtils.isEmpty(tag) ? "xLog" : tag;
        LogConfiguration logConfig = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(tag)
                .build();

        Printer filePrinter = new FilePrinter
                .Builder(LogDir)
                .flattener(new ClassicFlattener())
                .fileNameGenerator(new DateFileNameGenerator() {
                    @Override
                    public String generateFileName(int logLevel, long timestamp) {
                        if (logRprFileName == null) {
                            return super.generateFileName(logLevel, timestamp) + ".txt";
                        }
                        return logRprFileName + "_" + super.generateFileName(logLevel, timestamp) + ".txt";
                    }
                })
                .cleanStrategy(new FileLastModifiedCleanStrategy(15 * 24 * 3600 * 1000))
                .build();

        XLog.init(logConfig, filePrinter, mCloudLogPrinter);
    }


    public static void println(int level, String tag, String content) {
        if (printer != null) {
            printer.println(level, tag, content);
        }
    }

    public static CloudLogPrinter getPrinter() {
        return printer;
    }

    public static void uploadCache(long maxSize) {
        if (printer != null) {//确保初始化完成
            CloudLogPrinter.getInstance().uploadCache(maxSize);
        }
    }

    public static void uploadCache() {
        if (printer != null) {//确保初始化完成
            CloudLogPrinter.getInstance().uploadCache();
        }
    }

    public static void updateLogByUser() {
        if (printer != null) {//确保初始化完成
            CloudLogPrinter.getInstance().uploadCurrentLogs();
        }
    }

    public static int getCurrentLogSize() {
        if (printer == null) {
            return -1;
        }
        return CloudLogPrinter.getInstance().getCurrentLogSize();
    }
}
