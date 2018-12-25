package com.icen.icenlibs.util;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.icen.icenlibs.AppLogUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "aaa.pcm";
    public static final String WAV_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "aaa.wav";

    public static final int MESSAGE_FILE_BEGIN = 0;
    public static final int MESSAGE_FILE_PROCESS = MESSAGE_FILE_BEGIN + 1;
    public static final int MESSAGE_FILE_END = MESSAGE_FILE_BEGIN + 2;

    private Context mContext;
    private BufferedOutputStream bos = null;
    private FileOutputStream fos = null;
    private File file = null;
    private File wav_file = null;

    private Handler mFileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_FILE_BEGIN:
                    AppLogUtils.outputActivityLog("===beginWriteFile===");
                    try {
                        File dir = new File(FILE_PATH);
                        if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                            dir.mkdirs();
                        }
                        file = new File(FILE_PATH);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();

                        wav_file = new File(WAV_FILE_PATH);
                        if (wav_file.exists()) {
                            wav_file.delete();
                        }
                        wav_file.createNewFile();

                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLogUtils.outputActivityLog("===beginWriteFile errrrrrrrrrrrrrrrrr===");
                    }
                    break;
                case MESSAGE_FILE_PROCESS:
                    AppLogUtils.outputActivityLog("===processWriteFile===path= " + FILE_PATH);
                    byte[] content = msg.getData().getByteArray("content");
                    if (null != bos && null != content) {
                        try {
                            bos.write(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                            AppLogUtils.outputActivityLog("===processWriteFile errrrrrrrrrrrrrrrrr===");
                        }
                    }
                    break;
                case MESSAGE_FILE_END:
                    AppLogUtils.outputActivityLog("===endWriteFile===");
                    if (null != bos) {
                        try {
                            bos.close();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    pcm2wav();
                    break;
            }
        }
    };

    public FileUtils(Context ctx) {
        mContext = ctx;
    }

    public void beginWriteFile() {
        mFileHandler.sendEmptyMessage(MESSAGE_FILE_BEGIN);
    }

    public void processWriteFile(byte[] content) {
        Message msg = mFileHandler.obtainMessage(MESSAGE_FILE_PROCESS);
        Bundle data = new Bundle();
        data.putByteArray("content", content);
        msg.setData(data);
        mFileHandler.dispatchMessage(msg);
    }

    public void endWriteFile() {
        mFileHandler.sendEmptyMessage(MESSAGE_FILE_END);
    }

    private void pcm2wav(){
        try {
            FileInputStream fis = new FileInputStream(FILE_PATH);
            FileOutputStream fos = new FileOutputStream(WAV_FILE_PATH);
            int PCMSize = 0;
            byte[] buf = new byte[1024 * 4];
            int size = fis.read(buf);
            while (size != -1) {
                PCMSize += size;
                size = fis.read(buf);
            }
            fis.close();
            WaveHeader header = new WaveHeader(PCMSize + (44 - 8));
            byte[] h = header.getHeader();
            fos.write(h, 0, h.length);
            fis = new FileInputStream(FILE_PATH);
            size = fis.read(buf);
            while (size != -1) {
                fos.write(buf, 0, size);
                size = fis.read(buf);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
