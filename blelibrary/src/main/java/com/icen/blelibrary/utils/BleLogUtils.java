package com.icen.blelibrary.utils;

/**
 * Created by icean on 2017/10/29.
 */

public final class BleLogUtils {
    private static final boolean ENABLE_SERVICE_LOG = true;
    private static final String TAG_SERVICE_LOG = "ble_lib_service";
    public static final void outputServiceLog(String output_message){
        if (ENABLE_SERVICE_LOG)
            android.util.Log.e(TAG_SERVICE_LOG, output_message);
    }

    private static final boolean ENABLE_MANAGER_LOG = true;
    private static final String TAG_MANAGER_LOG = "ble_lib_manager";
    public static final void outputManagerLog(String output_message){
        if (ENABLE_MANAGER_LOG)
            android.util.Log.e(TAG_MANAGER_LOG, output_message);
    }

    private static final boolean ENABLE_UTIL_LOG = true;
    private static final String TAG_UTIL_LOG = "ble_lib_utils";
    public static final void outputUtilLog(String output_message){
        if (ENABLE_UTIL_LOG)
            android.util.Log.e(TAG_UTIL_LOG, output_message);
    }

    private static final boolean ENABLE_ACTIVITY_LOG = true;
    private static final String TAG_ACTIVITY_LOG = "base_activity";
    public static final void outputActivityLog(String output_message) {
        if (ENABLE_ACTIVITY_LOG)
            android.util.Log.e(TAG_ACTIVITY_LOG, output_message);
    }

    private static final boolean ENABLE_APPLICATION_LOG = true;
    private static final String  TAG_APPLICATION_LOG = "base_application";
    public static final void  outputApplicationLog(String output_message){
        if (ENABLE_APPLICATION_LOG)
            android.util.Log.e(TAG_APPLICATION_LOG, output_message);
    }
}
