package com.icen.icenlibs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.icen.blelibrary.activity.BleBaseActivity;
import com.icen.icenlibs.adapter.ServicesAdapter;

public class BleInformationActivity extends BleBaseActivity
                                    implements AdapterView.OnItemClickListener, View.OnClickListener {

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

    private TextView mTVDeviceName, mTVDeviceMac;
    private View mVOperationRoot, mVNotificationRoot;
    private EditText mETContent;
    private TextView mTVRWResult, mTVNotificationResult;
    private Button   mBTRead, mBTWrite;
    private Spinner  mSPService, mSPCh, mSPService1, mSPCh1;

    private ServicesAdapter mServiceAdapter;

    private String mTargetName, mTargetAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_information);

        mTargetName = getIntent().getStringExtra(KEY_TARGET_NAME);
        mTargetAddress = getIntent().getStringExtra(KEY_TARGET_ADDRESS);

        if (TextUtils.isEmpty(mTargetAddress)) {
            Toast.makeText(this, "Invalid address or name!", Toast.LENGTH_LONG).show();
            finish();
        }

        mTVDeviceName = (TextView) findViewById(R.id.b_i_title_name);
        mTVDeviceName.setText(mTargetName);
        mTVDeviceMac = (TextView) findViewById(R.id.b_i_title_address);
        mTVDeviceMac.setText(mTargetAddress);

        mVOperationRoot = findViewById(R.id.b_i_rw_root);
        mVOperationRoot.setEnabled(false);
        mVNotificationRoot = findViewById(R.id.b_i_n_root);
        mVNotificationRoot.setEnabled(false);

        mSPService  = (Spinner) findViewById(R.id.b_i_rw_service);
        mSPCh       = (Spinner) findViewById(R.id.b_i_rw_characteristic);

        mSPService1 = (Spinner) findViewById(R.id.b_i_n_service);
        mSPCh       = (Spinner) findViewById(R.id.b_i_n_characteristic);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

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
    public void onConnectDevice(boolean is_success, String device_name, String device_mac) {
        AppLogUtils.outputActivityLog("BleInformationActivity::onBackPressed::is_success= " + is_success +
                                    " device_name = " + device_name + " device_mac= " + device_mac );
        if (is_success){
            Bundle[] service_list = mBleManager.getDeviceService();
            if (null != service_list && service_list.length > 0) {
                String success_result = getResources().getString(R.string.ble_common_connect_success, device_name, device_mac);
                Toast.makeText(this, success_result, Toast.LENGTH_LONG).show();
                mTVDeviceMac.setText(mTargetAddress);
                mServiceAdapter = new ServicesAdapter(this, service_list);
            } else {
                String false_result = getResources().getString(R.string.ble_common_connect_false, device_name, device_mac);
                Toast.makeText(this, false_result, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            String false_result = getResources().getString(R.string.ble_common_connect_false, device_name, device_mac);
            Toast.makeText(this, false_result, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid) {

    }

    @Override
    public void onReadCh(int read_step, boolean is_success, String read_uuid, byte[] respond_data) {

    }

    @Override
    public void onWriteCh(int write_step, boolean is_success, String write_uuid, byte[] respond_data) {

    }

    @Override
    public void onClick(View v) {

    }
}
