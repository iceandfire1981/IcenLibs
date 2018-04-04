package com.icen.icenlibs;

public final class AppLogUtils {
    private static final boolean ENABLE_ACTIVITY_LOG = true;
    private static final String  ACTIVITY_LOG_TAG = "demo_activity";
    public static void outputActivityLog(String output_message){
        if (ENABLE_ACTIVITY_LOG)
            android.util.Log.e(ACTIVITY_LOG_TAG, output_message);
    }
}
