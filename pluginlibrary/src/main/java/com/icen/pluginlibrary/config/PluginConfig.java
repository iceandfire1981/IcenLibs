package com.icen.pluginlibrary.config;

import android.os.Environment;

import java.io.File;

/**
 * 插件系统配置以及默认配置
 */
public final class PluginConfig {

    /**
     * 压缩文件处理过程：开始处理
     */
    public static final int    ZIP_FILE_PROCESS_BEGIN = 0;
    /**
     * 压缩文件处理过程：正在处理
     */
    public static final int    ZIP_FILE_PROCESS_DOING = ZIP_FILE_PROCESS_BEGIN + 1;
    /**
     * 压缩文件处理过程：处理结束
     */
    public static final int    ZIP_FILE_PROCESS_END   = ZIP_FILE_PROCESS_BEGIN + 2;

    /**
     * 插件基础路径
     */
    public static final String BASE_PLUGIN_PATH = Environment.getExternalStorageDirectory() + "plugin";
    public static final String PLUGIN_DOWNLOAD_PATH = BASE_PLUGIN_PATH + File.separator + "download";
}
