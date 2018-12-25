package com.icen.blelibrary.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.Arrays;

public class BleBaseActivity extends AppCompatActivity
                                      implements BleManagerCallBack {

    private static String[] REQUEST_PERMISSION = new String[]{
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int PERMISSION_REQUEST_CODE = 9000;

    protected BleManager mBleManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBleManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, PERMISSION_REQUEST_CODE);
            } else {
                startBleManager();
            }
        } else {
            startBleManager();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reInitialManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyBleManager();
    }

    @Override
    public void onBackPressed() {
        destroyBleManager();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        destroyBleManager();
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
            startBleManager();
        } else {
            BleLogUtils.outputActivityLog("BleBaseActivity::onRequestPermissionsResult::Error permission denied");
            finish();
        }
    }

    @Override
    public void onInitialManager(boolean is_success){
        BleLogUtils.outputActivityLog("BleBaseActivity::onInitialManager::result= " + is_success);
    }

    @Override
    public void onLESwitch(int current_state){
        BleLogUtils.outputActivityLog("BleBaseActivity::onLESwitch::current_state= " + current_state);
    }

    @Override
    public void onLEScan(int scan_process, String device_name,
                                  String device_class,
                                  String device_mac,
                                  int device_rssi,
                                  byte[] broadcast_content){
        BleLogUtils.outputActivityLog("BleBaseActivity::onLEScan::step= " + scan_process +
                                    " name= " + device_name + " mac= " + device_mac + " rssi= " + device_rssi +
                                    "class= " + device_class + " content= " + Arrays.toString(broadcast_content));
    }

    @Override
    public void onConnectDevice(boolean is_success, String device_name, String device_mac){
        BleLogUtils.outputActivityLog("BleBaseActivity::onConnectDevice::result= " + is_success +
                                    " name= " + device_name + " mac= " + device_mac);
    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid){
        BleLogUtils.outputActivityLog("BleBaseActivity::onInitialNotification::result= " + is_success +
                " uuid= " + notification_uuid);
    }

    @Override
    public void onReadCh(int read_step, boolean is_success, String read_uuid,
                                  byte[] respond_data){
        BleLogUtils.outputActivityLog("BleBaseActivity::onReadCh::result= " + is_success +
                " uuid= " + read_uuid + " data= " + Arrays.toString(respond_data));
    }

    @Override
    public void onWriteCh(int write_step, boolean is_success, String write_uuid,
                                   byte[] respond_data){
        BleLogUtils.outputActivityLog("BleBaseActivity::onWriteCh::result= " + is_success +
                " uuid= " + write_uuid + " data= " + Arrays.toString(respond_data));
    }

    @Override
    public void onChChange(boolean is_success, String ch_uuid, byte[] ble_value) {
        BleLogUtils.outputActivityLog("BleBaseActivity::onChChange::result= " + is_success +
                " uuid= " + ch_uuid + " data= " + Arrays.toString(ble_value));
    }

    @Override
    public void onReadRSSI(boolean is_success, int current_rssi) {
        BleLogUtils.outputActivityLog("BleBaseActivity::onReadRSSI::result= " + is_success +
                " value= " + current_rssi);
    }

    @Override
    public void onReadBattery(boolean is_success, byte[] ble_value) {
        BleLogUtils.outputActivityLog("BleBaseActivity::onReadBattery::result= " + is_success +
                " value= " + Arrays.toString(ble_value));
    }

    /**
     * 启动管理器
     */
    protected void startBleManager(){
        BleLogUtils.outputActivityLog("BleBaseActivity::startBleManager===========");
        mBleManager = null;
        mBleManager = new BleManager(this);
        mBleManager.setManagerCallback(this);
        mBleManager.startManager();
    }

    /**
     * 重新设置BLE-MANAGER
     */
    protected void reInitialManager() {
        BleLogUtils.outputActivityLog("BleBaseActivity::re_init::System Ready= "
                + (null != mBleManager && mBleManager.isReady()));
        if (null != mBleManager && mBleManager.isReady()) {
            mBleManager.reInitialManager();
        }
    }

    /**
     * 销毁管理器
     */
    protected void destroyBleManager(){
        if (null != mBleManager) {
            try {
                mBleManager.destroyManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mBleManager = null;
        }
    }
}
