package com.pcommon.lib_network.log;

import okhttp3.internal.platform.Platform;

import static okhttp3.internal.platform.Platform.INFO;

public interface Logger {
    void log(String message);

    /**
     * A {@link Logger} defaults output appropriate for the current platform.
     */
    Logger DEFAULT = new Logger() {
        @Override
        public void log(String message) {
            Platform.get().log(message, INFO, null);
        }
    };
}
