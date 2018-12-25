package com.icen.icenlibs;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.icen.blelibrary.activity.BleBaseActivity;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.icenlibs.adapter.CharacteristicAdapter;
import com.icen.icenlibs.adapter.ServicesAdapter;
import com.icen.icenlibs.util.FileUtils;

import java.util.Arrays;

public class BleInformationActivity extends BleBaseActivity
                                    implements View.OnClickListener {

    public static final int REQUEST_CODE = 9001;
    public static final String KEY_TARGET_ADDRESS = "key_target_address";
    public static final String KEY_TARGET_NAME = "key_target_name";
    private static final String INTENT_STRING = "com.icen.icenlibs.BLE_INFO_DEMO";

    public static final boolean startMySelfWithResult(Activity ctx, String target_address, String target_name){
        Intent start_intent = new Intent(INTENT_STRING);
        start_intent.addCategory(Intent.CATEGORY_DEFAULT);
        start_intent.putExtra(KEY_TARGET_NAME, target_name);
        start_intent.putExtra(KEY_TARGET_ADDRESS, target_address);
        boolean is_success = false;
        try {
            ctx.startActivityForResult(start_intent, REQUEST_CODE);
            is_success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is_success;
    }

    public static final boolean startMySelf(Activity ctx, String target_address, String target_name){
        Intent start_intent = new Intent(INTENT_STRING);
        start_intent.addCategory(Intent.CATEGORY_DEFAULT);
        start_intent.putExtra(KEY_TARGET_NAME, target_name);
        start_intent.putExtra(KEY_TARGET_ADDRESS, target_address);
        boolean is_success = false;
        try {
            ctx.startActivity(start_intent);
            is_success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is_success;
    }

    private AdapterView.OnItemSelectedListener mRWServiceListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AppLogUtils.outputActivityLog("BleInformationActivity::onItemSelected----1=====" + position);
            Bundle current_service = (Bundle) mRWServiceAdapter.getItem(position);
            String service_uuid = current_service.getString(BleLibsConfig.LE_SERVICE_UUID);
            mCurrentRWServiceUUID = service_uuid;
            Bundle[] ch_list = mBleManager.getAllCharacteristicInService(service_uuid);
            if (null != ch_list){
                updateRWChList(ch_list);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mRWCharacteristicListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AppLogUtils.outputActivityLog("BleInformationActivity::onItemSelected----2=====" + position);
            Bundle current_ch = (Bundle)mRWChAdapter.getItem(position);
            mCurrentRWChUUID = current_ch.getString(BleLibsConfig.LE_CHARACTERISTIC_UUID);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mNServiceListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AppLogUtils.outputActivityLog("BleInformationActivity::onItemSelected----3=====" + position);
            Bundle current_service = (Bundle) mNotificationServiceAdapater.getItem(position);
            String service_uuid = current_service.getString(BleLibsConfig.LE_SERVICE_UUID);
            mCurrentNServiceUUID = service_uuid;
            Bundle[] ch_list = mBleManager.getAllCharacteristicInService(service_uuid);
            if (null != ch_list){
                updateNChList(ch_list);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mNCharacteristicListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AppLogUtils.outputActivityLog("BleInformationActivity::onItemSelected----4=====" + position);
            Bundle current_ch = (Bundle)mNotificationChAdapater.getItem(position);
            mCurrentNChUUID = current_ch.getString(BleLibsConfig.LE_CHARACTERISTIC_UUID);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private TextView mTVDeviceName, mTVDeviceMac;
    private View mVOperationRoot, mVNotificationRoot;
    private EditText mETContent;
    private TextView mTVRWResult, mTVNotificationResult;
    private Button   mBTRead, mBTWrite, mBTNSettings, mBTBattery, mBTRSSI;
    private Spinner  mSPRWService, mSPRWCh, mSPNService, mSPNCh;

    private ServicesAdapter mRWServiceAdapter, mNotificationServiceAdapater;
    private CharacteristicAdapter mRWChAdapter, mNotificationChAdapater;

    private String mTargetName, mTargetAddress;
    private String mCurrentRWServiceUUID, mCurrentNServiceUUID;
    private String mCurrentRWChUUID, mCurrentNChUUID;
    private Bundle[] mServicesList;

    private FileUtils mFileUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_information);

        mTargetName = getIntent().getStringExtra(KEY_TARGET_NAME);
        mTargetAddress = getIntent().getStringExtra(KEY_TARGET_ADDRESS);
        mFileUtils = new FileUtils(this);
        if (TextUtils.isEmpty(mTargetAddress)) {
            Toast.makeText(this, "Invalid address or name!", Toast.LENGTH_LONG).show();
            finish();
        }

        mTVDeviceName = (TextView) findViewById(R.id.b_i_title_name);
        mTVDeviceName.setText(mTargetName);
        mTVDeviceMac = (TextView) findViewById(R.id.b_i_title_address);
        mTVDeviceMac.setText(mTargetAddress);

        mETContent = (EditText) findViewById(R.id.b_i_rw_content);
        mTVRWResult = (TextView) findViewById(R.id.b_i_rw_result);
        mTVNotificationResult = (TextView) findViewById(R.id.b_i_n_result);

        mBTRead = (Button) findViewById(R.id.b_i_rw_read);
        mBTRead.setOnClickListener(this);
        mBTWrite = (Button) findViewById(R.id.b_i_rw_write);
        mBTWrite.setOnClickListener(this);
        mBTNSettings = (Button) findViewById(R.id.b_i_n_apply);
        mBTNSettings.setOnClickListener(this);
        mBTBattery = (Button) findViewById(R.id.b_i_battery);
        mBTBattery.setOnClickListener(this);
        mBTRSSI = (Button) findViewById(R.id.b_i_rssi);
        mBTRSSI.setOnClickListener(this);

        mVOperationRoot = findViewById(R.id.b_i_rw_root);
        mVNotificationRoot = findViewById(R.id.b_i_n_root);

        mSPRWService  = (Spinner) findViewById(R.id.b_i_rw_service);
        mSPRWService.setOnItemSelectedListener(mRWServiceListener);
        mSPRWCh       = (Spinner) findViewById(R.id.b_i_rw_characteristic);
        mSPRWCh.setOnItemSelectedListener(mRWCharacteristicListener);

        mSPNService  = (Spinner) findViewById(R.id.b_i_n_service);
        mSPNService.setOnItemSelectedListener(mNServiceListener);
        mSPNCh       = (Spinner) findViewById(R.id.b_i_n_characteristic);
        mSPNCh.setOnItemSelectedListener(mNCharacteristicListener);
    }

    @Override
    public void onInitialManager(boolean is_success) {
        AppLogUtils.outputActivityLog("BleInformationActivity::onInitialManager::result= " + is_success);
        if (!is_success)
            finish();
        else {
            if (mBleManager.isLeEnabled()) {
                mTVDeviceMac.setText(R.string.ble_common_connecting);
                mBleManager.connectToDevice(true, mTargetAddress);
            } else {
                mTVDeviceMac.setText(R.string.ble_common_le_not_enable);
            }
        }
    }

    @Override
    public void onConnectDevice(boolean is_success, final String device_name, final String device_mac) {
        AppLogUtils.outputActivityLog("BleInformationActivity::onBackPressed::is_success= " + is_success +
                                    " device_name = " + device_name + " device_mac= " + device_mac );
        if (is_success){
            mServicesList = null;
            mServicesList = mBleManager.getDeviceService();
            if (null != mServicesList && mServicesList.length > 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String success_result = getResources().getString(R.string.ble_common_connect_success, device_name, device_mac);
                        Toast.makeText(BleInformationActivity.this, success_result, Toast.LENGTH_LONG).show();
                        mTVDeviceMac.setText(device_mac);
                        mTVDeviceName.setText(device_name);
                        updateNServiceList();
                        updateRWServiceList();

                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String false_result = getResources().getString(R.string.ble_common_connect_false, device_name, device_mac);
                        Toast.makeText(BleInformationActivity.this, false_result, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String false_result = getResources().getString(R.string.ble_common_connect_false, device_name, device_mac);
                    Toast.makeText(BleInformationActivity.this, false_result, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

        }
    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid) {
        if (is_success)
            mTVNotificationResult.setEnabled(true);
        else
            mTVNotificationResult.setEnabled(false);
    }

    @Override
    public void onReadCh(int read_step, boolean is_success, String read_uuid, byte[] respond_data) {
        if (is_success) {
            mTVRWResult.setText(Arrays.toString(respond_data));
        } else {
            mTVRWResult.setText("False");
        }
    }

    @Override
    public void onWriteCh(int write_step, boolean is_success, String write_uuid, byte[] respond_data) {
        if (is_success) {
            mTVRWResult.setText(Arrays.toString(respond_data));
        } else {
            mTVRWResult.setText("False");
        }
    }

    @Override
    public void onChChange(boolean is_success, String ch_uuid, final byte[] ble_value) {
        if (is_success) {
            mTVNotificationResult.setText(Arrays.toString(ble_value));
            AppLogUtils.outputActivityLog("===onChChange===::ble_value= " + ble_value.length);
            if (null != ble_value && ble_value.length > 127){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFileUtils.processWriteFile(ble_value);
                    }
                });

            }
        } else {
            mTVNotificationResult.setText("false");
        }
    }

    @Override
    public void onReadRSSI(boolean is_success, final int current_rssi){
        if (is_success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BleInformationActivity.this, "rssi= " + current_rssi, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onReadBattery(boolean is_success, final byte[] ble_value){
        if (is_success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BleInformationActivity.this, "rssi= " + Arrays.toString(ble_value), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int view_id = v.getId();
        switch (view_id) {
            case R.id.b_i_rw_read:
                if (!TextUtils.isEmpty(mCurrentRWChUUID))
                    mBleManager.readCharacteristic(mCurrentRWChUUID);
                break;
            case R.id.b_i_rw_write:
                String write_content = mETContent.getText().toString();
                if(!TextUtils.isEmpty(write_content) && "a".equalsIgnoreCase(write_content)){//开启功能
                    AppLogUtils.outputActivityLog("BleInformationActivity::onClick::path= " + FileUtils.FILE_PATH);
                    mBleManager.writeCharacteristic(mCurrentRWChUUID, 31, BluetoothGattCharacteristic.FORMAT_UINT8);
                    mFileUtils.beginWriteFile();
                } else if(!TextUtils.isEmpty(write_content) && "b".equalsIgnoreCase(write_content)) {//关闭功能
                    mBleManager.writeCharacteristic(mCurrentRWChUUID, 23, BluetoothGattCharacteristic.FORMAT_UINT8);
                    mFileUtils.endWriteFile();
                } else {
                    if (!TextUtils.isEmpty(mCurrentRWChUUID) && !TextUtils.isEmpty(write_content)) {
                        mBleManager.writeCharacteristic(mCurrentRWChUUID, write_content);
                    }
                }
                break;
            case R.id.b_i_n_apply:
                if (!TextUtils.isEmpty(mCurrentNChUUID))
                    mBleManager.initialNotification(mCurrentNChUUID);
                break;
            case R.id.b_i_battery:
                if (! mBleManager.readBattery()) {
                    Toast.makeText(this, "Operation false_1", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.b_i_rssi:
                if (! mBleManager.readRSSI()) {
                    Toast.makeText(this, "Operation false_2", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void updateRWServiceList(){
        AppLogUtils.outputActivityLog("BleInformationActivity::updateRWServiceList::=====");
        if (null != mServicesList && mServicesList.length > 0) {
            mRWServiceAdapter = new ServicesAdapter(this, mServicesList);
            mSPRWService.setAdapter(mRWServiceAdapter);
        }
    }

    private void updateRWChList(@NonNull  Bundle[] ch_list){
        AppLogUtils.outputActivityLog("BleInformationActivity::updateRWChList::=====");
        if (null != ch_list && ch_list.length > 0) {
            mRWChAdapter = new CharacteristicAdapter(this, ch_list);
            mSPRWCh.setAdapter(mRWChAdapter);
        }
    }

    private void updateNServiceList(){
        AppLogUtils.outputActivityLog("BleInformationActivity::updateNServiceList::=====");
        if (null != mServicesList && mServicesList.length > 0) {
            mNotificationServiceAdapater = new ServicesAdapter(this, mServicesList);
            mSPNService.setAdapter(mNotificationServiceAdapater);
        }
    }

    private void updateNChList(@NonNull  Bundle[] ch_list){
        AppLogUtils.outputActivityLog("BleInformationActivity::updateNChList::=====");
        if (null != ch_list && ch_list.length > 0) {
            mNotificationChAdapater = new CharacteristicAdapter(this, ch_list);
            mSPNCh.setAdapter(mNotificationChAdapater);
        }
    }
}
