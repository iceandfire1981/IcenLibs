package com.icen.blelibrary.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.icen.blelibrary.IBleOp;
import com.icen.blelibrary.IBleOpCallback;
import com.icen.blelibrary.R;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.config.ConnectBleDevice;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static com.icen.blelibrary.config.BleLibsConfig.DEFAULT_RSSI;

/**
 * Created by Alx Slash on 2017/10/29.
 * Author: alxslashtraces@gmail.com
 */

public class BleManagerService extends Service {

    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    static {
        INTENT_FILTER.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        INTENT_FILTER.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    }

    private BleOpImpl mBleOpImpl;
    private IBleOpCallback mBleOpCallback;

    private BluetoothAdapter mBleAdapter;

    private HashMap<String, ConnectBleDevice.BleBroadcastRecordMessage> mCurrentDeviceMap;

    private HashMap<String, String>  mSourceDeviceMapByMac;
    private boolean mAutoConnect, mIsScanning;
    private long mScanOvertime;

    private Handler mServiceHandler = new Handler();

    /**
     * 用于监听移动端设备蓝牙开关状态变化以及连接状态
     */
    private BroadcastReceiver mBleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String current_action = (null == intent) ? null : intent.getAction();

            BleLogUtils.outputServiceLog("mBleStateReceiver::onReceive::Action= " +
                    (!TextUtils.isEmpty(current_action) ? current_action : "No Action Here"));
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(current_action)) {//处理连接状态的ACTION

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(current_action)) {//处理蓝牙开启状态的ACTION
                int current_stats = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                BleLogUtils.outputServiceLog("mBleStateReceiver::onReceive::current_stats= " + current_stats);
                if (BluetoothAdapter.STATE_ON == current_stats) {//最终状态为关闭
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_ON);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_OFF == current_stats) {//最终状态为打开
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_OFF);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_TURNING_ON == current_stats) {//中间状态：正在打开
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_OPENING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_TURNING_OFF == current_stats) {//中间状态：正在关闭
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_CLOSING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.ERROR == current_stats){//失败状态
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_ERROR);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            String device_mac = bluetoothDevice.getAddress().toLowerCase(Locale.getDefault());
            String device_name = (TextUtils.isEmpty(bluetoothDevice.getName())) ?
                        getString(R.string.default_device_name) : bluetoothDevice.getName().trim();
            String device_class = bluetoothDevice.getBluetoothClass().toString();

            BleLogUtils.outputServiceLog("BleManagerService::onLeScan::mac= " + device_mac +
                    " name= " + device_name + " class= " + device_class +
                    " rssi= " + rssi + " content= " + ((null == bytes || bytes.length <= 0) ? "No Record" : Arrays.toString(bytes)));

            ConnectBleDevice.BleBroadcastRecordMessage.Builder device_builder = ConnectBleDevice.BleBroadcastRecordMessage.newBuilder();
            device_builder.setDeviceName(device_name);
            device_builder.setDeviceMac(device_mac);
            device_builder.setDeviceClass(device_class);
            device_builder.setDeviceRssi(rssi);
            device_builder.setBroadcastContent(ByteString.copyFrom(bytes));

            mCurrentDeviceMap.put(device_mac, device_builder.build());

            if (null != mBleOpCallback) {
                try {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_DOING, bluetoothDevice.getName(),
                            bluetoothDevice.getAddress(), bluetoothDevice.getClass().toString(), rssi, bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

     private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

         @Override
         public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
             super.onPhyUpdate(gatt, txPhy, rxPhy, status);
         }

         @Override
         public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
             super.onPhyRead(gatt, txPhy, rxPhy, status);
         }

         @Override
         public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
             super.onConnectionStateChange(gatt, status, newState);
         }

         @Override
         public void onServicesDiscovered(BluetoothGatt gatt, int status) {
             super.onServicesDiscovered(gatt, status);
         }

         @Override
         public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             super.onCharacteristicRead(gatt, characteristic, status);
         }

         @Override
         public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             super.onCharacteristicWrite(gatt, characteristic, status);
         }

         @Override
         public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
             super.onCharacteristicChanged(gatt, characteristic);
         }

         @Override
         public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
             super.onDescriptorRead(gatt, descriptor, status);
         }

         @Override
         public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
             super.onDescriptorWrite(gatt, descriptor, status);
         }

         @Override
         public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
             super.onReliableWriteCompleted(gatt, status);
         }

         @Override
         public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
             super.onReadRemoteRssi(gatt, rssi, status);
         }

         @Override
         public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
             super.onMtuChanged(gatt, mtu, status);
         }
     };

    @Override
    public void onCreate() {
        super.onCreate();
        mBleAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        initialSystemConfig();
        //注册广播监听器
        try {
            registerReceiver(mBleStateReceiver, INTENT_FILTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBleAdapter.cancelDiscovery();
        try {
            unregisterReceiver(mBleStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        BleLogUtils.outputServiceLog("onBind::info= " + intent);
        if (null == mBleOpImpl)
            mBleOpImpl = new BleOpImpl();
        return mBleOpImpl;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        BleLogUtils.outputServiceLog("onUnbind::info= " + intent);
        return super.onUnbind(intent);
    }

    private void initialSystemConfig(){
        mSourceDeviceMapByMac = BleLibsConfig.getDeviceListInFile(BleManagerService.this);
        mAutoConnect = BleLibsConfig.getAutoConnectInFile(BleManagerService.this);
        mScanOvertime = BleLibsConfig.getScanOvertime(BleManagerService.this);
        mIsScanning = false;
    }

    private class BleOpImpl extends IBleOp.Stub{

        @Override
        public Bundle[] getDeviceInfo(){
            BleLogUtils.outputServiceLog("getDeviceInfo::size= " + (null != mCurrentDeviceMap ? String.valueOf(mCurrentDeviceMap.size()) : "-1"));
            if (null == mCurrentDeviceMap || mCurrentDeviceMap.size() <= 0) {
                BleLogUtils.outputServiceLog("getDeviceInfo::info::There are no devices here");
                return null;
            } else {
                Bundle[] device_bundles = new Bundle[mCurrentDeviceMap.size()];
                int device_index = 0;
                Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                while (device_iterator.hasNext()){
                    Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage> device_entry =
                            (Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage>) device_iterator.next();
                    BleLogUtils.outputServiceLog("getDeviceInfo::key= " + device_entry.getKey());
                    Bundle device_info = new Bundle();
                    device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, device_entry.getValue().getDeviceName());
                    device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, device_entry.getValue().getDeviceMac());
                    device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, device_entry.getValue().getDeviceClass());
                    device_info.putLong(BleLibsConfig.BROADCAST_INFO_SIGNAL, device_entry.getValue().getDeviceRssi());
                    byte[] content_bytes = device_entry.getValue().getBroadcastContent().toByteArray();
                    device_entry.getValue().getBroadcastContent().copyTo(content_bytes, 0);
                    device_info.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, content_bytes);
                    device_bundles[device_index] = device_info;
                    device_index = device_index + 1;
                }

                return device_bundles;
            }
        }

        @Override
        public Bundle[]   getDeviceInfoByAddress(String device_mac){
            if (null != mCurrentDeviceMap || mCurrentDeviceMap.size() > 0) {
                if (!TextUtils.isEmpty(device_mac)) {
                    Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                    while (device_iterator.hasNext()) {
                        Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage> entry =
                                (Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage>) device_iterator.next();
                        String current_mac = entry.getValue().getDeviceMac();
                        if (device_mac.equalsIgnoreCase(current_mac)) {
                            Bundle device_info = new Bundle();
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, entry.getValue().getDeviceName());
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, entry.getValue().getDeviceMac());
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, entry.getValue().getDeviceClass());
                            device_info.putLong(BleLibsConfig.BROADCAST_INFO_SIGNAL, entry.getValue().getDeviceRssi());
                            byte[] content_bytes = null;
                            entry.getValue().getBroadcastContent().copyTo(content_bytes, 0);
                            device_info.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, content_bytes);
                            Bundle[] result_device_list = new Bundle[1];
                            result_device_list[0] = device_info;
                            return result_device_list;
                        }
                    }
                } else {
                    Bundle[] le_device_list = new Bundle[mCurrentDeviceMap.size()];
                    int le_devices_index = 0;
                    Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                    while (device_iterator.hasNext()) {
                        Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage> entry =
                                (Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage>) device_iterator.next();
                        Bundle device_info = new Bundle();
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, entry.getValue().getDeviceName());
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, entry.getValue().getDeviceMac());
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, entry.getValue().getDeviceClass());
                        device_info.putLong(BleLibsConfig.BROADCAST_INFO_SIGNAL, entry.getValue().getDeviceRssi());
                        byte[] content_bytes = null;
                        entry.getValue().getBroadcastContent().copyTo(content_bytes, 0);
                        device_info.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, content_bytes);
                        le_device_list[le_devices_index] = device_info;
                        le_devices_index = le_devices_index + 1;
                    }
                    return le_device_list;
                }
                return null;
            }
            return null;
        }

        @Override
        public Bundle[] getDeviceInfoByName(String device_name){
            ArrayList<Bundle> device_list = new ArrayList();
            if (null != mCurrentDeviceMap || mCurrentDeviceMap.size() > 0) {
                Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                if (TextUtils.isEmpty(device_name)){
                    while (device_iterator.hasNext()) {
                        Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage> entry =
                                (Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage>) device_iterator.next();
                        Bundle device_info = new Bundle();
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, entry.getValue().getDeviceName());
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, entry.getValue().getDeviceMac());
                        device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, entry.getValue().getDeviceClass());
                        device_info.putLong(BleLibsConfig.BROADCAST_INFO_SIGNAL, entry.getValue().getDeviceRssi());
                        byte[] content_bytes = null;
                        entry.getValue().getBroadcastContent().copyTo(content_bytes, 0);
                        device_info.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, content_bytes);
                        device_list.add(device_info);
                    }
                } else {
                    while (device_iterator.hasNext()) {
                        Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage> entry =
                                (Map.Entry<String, ConnectBleDevice.BleBroadcastRecordMessage>) device_iterator.next();
                        String current_name = entry.getValue().getDeviceName();
                        if (device_name.equalsIgnoreCase(current_name)){
                            Bundle device_info = new Bundle();
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, entry.getValue().getDeviceName());
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, entry.getValue().getDeviceMac());
                            device_info.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, entry.getValue().getDeviceClass());
                            device_info.putLong(BleLibsConfig.BROADCAST_INFO_SIGNAL, entry.getValue().getDeviceRssi());
                            byte[] content_bytes = null;
                            entry.getValue().getBroadcastContent().copyTo(content_bytes, 0);
                            device_info.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, content_bytes);
                            device_list.add(device_info);
                        }
                    }
                }
            }

            if (null == device_list || device_list.size() <= 0) {
                return null;
            } else {
                Bundle[] all_devices = new Bundle[device_list.size()];
                for (int index = 0; index < device_list.size(); index ++) {
                    all_devices[index] = device_list.get(index);
                }
                return all_devices;
            }
        }

        @Override
        public Bundle[] getServices() throws RemoteException {
            return null;
        }

        @Override
        public Bundle[] getCharacteristic(String service_uuid) throws RemoteException {
            return null;
        }

        @Override
        public boolean isSupportLE(){
            boolean is_support = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            BleLogUtils.outputServiceLog("isSupportLE::result= " + is_support);
            return is_support;
        }

        @Override
        public boolean leIsEnable() throws RemoteException {
            boolean is_enable = false;
            if (null != mBleAdapter) {
                is_enable = mBleAdapter.isEnabled();
            }
            BleLogUtils.outputServiceLog("BleOpImpl::leIsEnable::result= " + is_enable);
            return is_enable;
        }

        @Override
        public boolean bleSwitcher(boolean enabled) throws RemoteException {
            boolean is_success = false;

            if (null != mBleAdapter) {
                stopDiscoveryDevice();
                if (enabled)
                    is_success = mBleAdapter.enable();
                else {
                    is_success = mBleAdapter.disable();
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::bleSwitcher::enabled= " + enabled + " result= " + is_success);
            return is_success;
        }

        @Override
        public boolean startDiscoveryDevice() throws RemoteException {
            //扫描开始前先确认参数：是否自动重连，扫描超时时长
            mAutoConnect = BleLibsConfig.getAutoConnectInFile(BleManagerService.this);
            mScanOvertime = BleLibsConfig.getScanOvertime(BleManagerService.this);

            boolean is_success = false;
            if (null != mBleAdapter) {
                mCurrentDeviceMap = new HashMap<>();
                if (mIsScanning) {//当前如果正在扫描需要先停止
                    stopDiscoveryDevice();
                    mIsScanning = false;
                }
                is_success = mBleAdapter.startLeScan(mLeScanCallback);
            }

            BleLogUtils.outputServiceLog("BleOpImpl::startDiscoveryDevice::result= " + is_success + " auto= " + mAutoConnect + " ot= " + mScanOvertime);
            if (is_success){
                if (null != mBleOpCallback) {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_BEGIN, null, null,
                            null, DEFAULT_RSSI, null);
                }
                mIsScanning = true;
                //指定时间后停止扫描
                mServiceHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BleLogUtils.outputServiceLog("BleOpImpl::startDiscoveryDevice::overtime now" );
                            stopDiscoveryDevice();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, mScanOvertime);
            } else {
                if (null != mBleOpCallback)
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_EXCEPTION, null, null,
                            null, DEFAULT_RSSI, null);
                mIsScanning = false;
            }
            return is_success;
        }

        @Override
        public void stopDiscoveryDevice() throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::stopDiscoveryDevice::mIs= " + mIsScanning);
            if (null != mBleAdapter && mIsScanning) {
                mBleAdapter.stopLeScan(mLeScanCallback);
                mBleAdapter.cancelDiscovery();
                mIsScanning = false;
            }
            if (null != mBleOpCallback)
                mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_END, null, null,
                        null, DEFAULT_RSSI, null);

        }

        @Override
        public boolean connectToDevice(String remote_address) throws RemoteException {
            return false;
        }

        @Override
        public void initialNotification(String notification_uuid) throws RemoteException {

        }

        @Override
        public boolean disconnect() throws RemoteException {
            return false;
        }

        @Override
        public boolean disconnectByName(String device_name) throws RemoteException{
            return false;
        }

        @Override
        public boolean disconnectByMac(String device_mac) throws RemoteException{
            return false;
        }

        @Override
        public boolean readCharacteristic(String read_uuid) throws RemoteException {
            return false;
        }

        @Override
        public boolean writeCharacteristic(String write_uuid, byte[] write_content) throws RemoteException {
            return false;
        }

        @Override
        public boolean writeCharacteristicString(String write_uuid, String write_content) throws RemoteException {
            return false;
        }

        @Override
        public boolean writeCharacteristicInt(String write_uuid, int write_content, int content_format) throws RemoteException {
            return false;
        }

        @Override
        public void setBleOpCallback(IBleOpCallback op_callback) throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::setBleOpCallback:info= " + op_callback);
            mBleOpCallback = op_callback;
        }

        @Override
        public boolean hasConnectToDevice() throws RemoteException {
            return false;
        }
    }
}
