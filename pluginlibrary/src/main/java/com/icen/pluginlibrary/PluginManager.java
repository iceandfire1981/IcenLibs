package com.icen.pluginlibrary;

import android.content.Context;

public final class PluginManager {

    /**
     * 插件基础路径
     */
    private String  mPluginBasePath;

    private Context mContext;

    public PluginManager(Context context, String base_path) {
        mContext = context;

    }
}
