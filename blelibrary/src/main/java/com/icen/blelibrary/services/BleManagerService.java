package com.icen.blelibrary.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.icen.blelibrary.IBleOp;
import com.icen.blelibrary.IBleOpCallback;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.config.ConnectBleDevice;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Alx Slash on 2017/10/29.
 * Author: alxslashtraces@gmail.com
 */

public class BleManagerService extends Service {

    private BleOpImpl mBleOpImpl;
    private IBleOpCallback mBleOpCallback;

    private BluetoothAdapter mBleAdapter;

    private HashMap<String, ConnectBleDevice.BleBroadcastRecordMessage> mCurrentDeviceMap;

    private HashMap<String, String>  mSourceDeviceMapByMac;
    private boolean mAutoConnect, mIsScanning;
    private long mScanOvertime;

    private Handler mServiceHandler = new Handler();

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            BleLogUtils.outputServiceLog("BleManagerService::onLeScan::mac= " + bluetoothDevice.getAddress() +
                    " i= " + i + " content= " + ((null == bytes || bytes.length <= 0) ? "No Record" : Arrays.toString(bytes)));

            ConnectBleDevice.BleBroadcastRecordMessage.Builder device_builder = ConnectBleDevice.BleBroadcastRecordMessage.newBuilder();
            device_builder.setDeviceName(bluetoothDevice.getName());
            device_builder.setDeviceMac(bluetoothDevice.getAddress().toLowerCase(Locale.getDefault()));
            device_builder.setDeviceClass(bluetoothDevice.getBluetoothClass().toString());
            device_builder.setBroadcastContent(ByteString.copyFrom(bytes));

            mCurrentDeviceMap.put(bluetoothDevice.getAddress().toLowerCase(Locale.getDefault()), device_builder.build());

            if (null != mBleOpCallback) {
                try {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.SCAN_PROCESS_SCANNING, bluetoothDevice.getName(),
                            bluetoothDevice.getAddress(), bluetoothDevice.getClass().toString(), bytes);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBleAdapter.cancelDiscovery();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initialSystemConfig(){
        mSourceDeviceMapByMac = BleLibsConfig.getDeviceListInFile(BleManagerService.this);
        mAutoConnect = BleLibsConfig.getAutoConnectInFile(BleManagerService.this);
        mScanOvertime = BleLibsConfig.getScanOvertime(BleManagerService.this);
        mIsScanning = false;
    }

    private class BleOpImpl extends IBleOp.Stub{

        @Override
        public Bundle[] getServices() throws RemoteException {
            return new Bundle[0];
        }

        @Override
        public Bundle[] getCharacteristic(String service_uuid) throws RemoteException {
            return new Bundle[0];
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
            boolean is_success = false;
            if (null != mBleAdapter) {
                mCurrentDeviceMap = new HashMap<>();
                if (mIsScanning) {
                    stopDiscoveryDevice();
                    mIsScanning = false;
                }
                is_success = mBleAdapter.startLeScan(mLeScanCallback);
                if (is_success)
                    mIsScanning = false;
            }

            BleLogUtils.outputServiceLog("BleOpImpl::startDiscoveryDevice::result= " + is_success);
            if (is_success){
                if (null != mBleOpCallback) {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.SCAN_PROCESS_BEGIN, null, null, null,
                            null);
                }
                //指定时间后停止扫描
                mServiceHandler.postAtTime(new Runnable() {
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
            }
            return is_success;
        }

        @Override
        public void stopDiscoveryDevice() throws RemoteException {
            if (null != mBleAdapter && mIsScanning) {
                mBleAdapter.stopLeScan(mLeScanCallback);
                mBleAdapter.cancelDiscovery();
                mIsScanning = false;
            }

            BleLogUtils.outputServiceLog("BleOpImpl::stopDiscoveryDevice=================");
            if (null != mBleOpCallback)
                mBleOpCallback.onDeviceScan(BleLibsConfig.SCAN_PROCESS_SCAN_END, null, null, null,
                        null);

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
