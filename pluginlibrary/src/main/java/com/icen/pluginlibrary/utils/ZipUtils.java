package com.icen.pluginlibrary.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.icen.pluginlibrary.callback.ZipCallBack;
import com.icen.pluginlibrary.config.PluginConfig;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 压缩/解压缩工具集合
 * 支持：zip、rar格式
 */
public final class ZipUtils {

    private static final int MESSAGE_PROCESS_BEGIN = 1000;
    private static final int MESSAGE_PROCESS_DOING = MESSAGE_PROCESS_BEGIN + 1;
    private static final int MESSAGE_PROCESS_END   = MESSAGE_PROCESS_BEGIN + 2;
    private static final int MESSAGE_PROCESS_EXCEPTION = MESSAGE_PROCESS_BEGIN + 3;

    private Context mContex;
    private ZipCallBack mZipCallback;

    private ZipFileProcessInfo mProcessInfo;

    private Handler mProcessHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public ZipUtils(Context ctx, ZipCallBack zipCallBack) {
        mContex = ctx;
        mZipCallback = zipCallBack;
    }

    /**
     * 解压文件
     * @param zip_file_path       需要解压的文件，是一个全路径，不能是空，为空则返回失败消息
     * @param target_file_path    解压的目标路径，如果是空则默认为@PluginConfig.BASE_PLUGIN_PATH
     * @param zip_file_encode     设置编码，如果为指定则默认是GBK编码格式
     */
    public void unZipFile(String zip_file_path, String target_file_path,
                          String zip_file_encode) {
        PluginLogUtils.outputUtilsLog("ZipUtils::unZipFile::source_path= " + zip_file_path +
                                    " target_path= " + target_file_path +
                                    " encode= " + zip_file_encode );
        Message exception_message = mProcessHandler.obtainMessage(MESSAGE_PROCESS_EXCEPTION);
        exception_message.obj = zip_file_path;

        if (TextUtils.isEmpty(zip_file_path)) {
            exception_message.sendToTarget();
            return;
        } else {
            File zip_file = new File(zip_file_path);
            if (!zip_file.exists() || zip_file.isDirectory()) {//文件不存在或者是一个文件夹
                exception_message.sendToTarget();
                return;
            }

            if (TextUtils.isEmpty(target_file_path)){//没有设置目标路径
                target_file_path = PluginConfig.BASE_PLUGIN_PATH + File.separator + zip_file.getName();
            }

            if (TextUtils.isEmpty(zip_file_encode)){
                zip_file_encode = "GBK";
            }

            ZipFile current_zip_file = null;
            try {
                current_zip_file = new ZipFile(zip_file, zip_file_encode);
                Enumeration<ZipEntry> zip_file_entries = current_zip_file.getEntries();
                int current_entries_size = Collections.list(zip_file_entries).size();

                Message message_begin = mProcessHandler.obtainMessage(PluginConfig.ZIP_FILE_PROCESS_BEGIN);
                message_begin.arg1 =  current_entries_size;
                message_begin.sendToTarget();

                ZipEntry current_entry = null;
                byte[] buf=new byte[1024];
                int current_process = 1;
                while (zip_file_entries.hasMoreElements()) {
                    current_entry = zip_file_entries.nextElement();
                    if (current_entry.isDirectory()) {
                        String dirstr = target_file_path + current_entry.getName();
                        dirstr.trim();
                        File f=new File(dirstr);
                        f.mkdir();
                        current_process = current_process + 1;
                        continue;
                    }
                    OutputStream os= null;
                    FileOutputStream fos = null;
                    File realFile = getRealFileName(target_file_path, current_entry.getName());
                    fos = new FileOutputStream(realFile);
                    os = new BufferedOutputStream(fos);
                    InputStream is= null;
                    is = new BufferedInputStream(current_zip_file.getInputStream(current_entry));
                    int readLen=0;
                    while ((readLen=is.read(buf, 0, 1024))!=-1) {
                        os.write(buf, 0, readLen);
                    }
                    is.close();
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                exception_message.sendToTarget();
            } finally {
                if (null != current_zip_file)
                    try {
                        current_zip_file.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }

        }
    }

    public static File getRealFileName(String baseDir, String absFileName){
        String[] dirs=absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if(dirs.length>1){
            for (int i = 0; i < dirs.length-1;i++) {
                substr = dirs[i]; ret=new File(ret, substr);
            }
            if(!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length-1];
            ret=new File(ret, substr);
            return ret;
        }else{
            ret = new File(ret,absFileName);
        } return ret;
    }

    /**
     * 描述压缩/解压缩处理过程信息
     */
    public class ZipFileProcessInfo{
        private int    mCurrentAction;//当前动作，0：压缩，1：解压缩
        private String mCurrentFileName;//当前处理的压缩文件名
        private int    mCurrentProcessStep;//当前进度
        private int    mCurrentEntryTotal;//需要处理的文件总数
        private int    mCurrentProcess;//当前已经完成的数量
        private String mCurrentEntryName;//当前正在处理的项的名称

        public int getCurrentAction() {
            return mCurrentAction;
        }

        public void setCurrentAction(int current_action) {
            this.mCurrentAction = current_action;
        }

        public String getCurrentFileName() {
            return mCurrentFileName;
        }

        public void setCurrentFileName(String current_file_name) {
            this.mCurrentFileName = current_file_name;
        }

        public int getCurrentProcessStep() {
            return mCurrentProcessStep;
        }

        public void setCurrentProcessStep(int current_process_step) {
            this.mCurrentProcessStep = current_process_step;
        }

        public int getCurrentEntryTotal() {
            return mCurrentEntryTotal;
        }

        public void setCurrentEntryTotal(int current_entry_total) {
            this.mCurrentEntryTotal = current_entry_total;
        }

        public int getCurrentProcess() {
            return mCurrentProcess;
        }

        public void setCurrentProcess(int current_process) {
            mCurrentProcess = current_process;
        }

        public String getCurrentEntyName() {
            return mCurrentEntryName;
        }

        public void setCurrentEntyName(String current_entry_name) {
            this.mCurrentEntryName = current_entry_name;
        }

        @Override
        public String toString() {
            return "ZipFileProcessInfo{" +
                    "mCurrentAction=" + mCurrentAction +
                    ", mCurrentFileName='" + mCurrentFileName + '\'' +
                    ", mCurrentProcessStep=" + mCurrentProcessStep +
                    ", mCurrentEntryTotal=" + mCurrentEntryTotal +
                    ", mCurrentProcess=" + mCurrentProcess +
                    ", mCurrentEntyName='" + mCurrentEntryName + '\'' +
                    '}';
        }
    }
}
