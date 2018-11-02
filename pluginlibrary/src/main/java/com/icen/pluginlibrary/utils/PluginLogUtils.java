package com.icen.pluginlibrary.utils;

public final class PluginLogUtils {
    private static final boolean ENABLE_UTILS_LOG = true;
    private static final String  TAG_UTILS_LOG = "plugin_utils_log";
    public static void outputUtilsLog(String output_message) {
        if (ENABLE_UTILS_LOG)
            android.util.Log.e(TAG_UTILS_LOG, output_message);
    }
}
