package com.icen.pluginlibrary.callback;

public interface ZipCallBack {
    /**
     * 解压进度回调
     * @param zip_file_name       当前正在处理的文件名（全路径形式）
     * @param process_step       当前处理步骤：开始，正在处理，结束，异常
     * @param entry_total        压缩文件顶层包含文件（夹）数量
     * @param current_process    当前进度
     * @param current_file_name  当前处理的文件名称
     * @param is_direct          当前是否是文件夹
     */
    void onUnZipFile(String zip_file_name, int process_step, int entry_total, int current_process,
                     String current_file_name, boolean is_direct);
}
