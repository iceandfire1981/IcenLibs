package com.icen.icenlibs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.icenlibs.devices.ServicesAdapter;

import org.w3c.dom.Text;

public class BleInformationActivity extends AppCompatActivity
                                    implements AdapterView.OnItemClickListener,
                                                BleManagerCallBack{

    public static final String KEY_TARGET_ADDRESS = "key_target_address";
    public static final String KEY_TARGET_NAME = "key_target_name";

    private TextView mTVDeviceName, mTVDeviceMac;
    private ListView mLVServiceList;

    private BleManager mBleManager;
    private ServicesAdapter mServiceAdapter;

    private String mTargetName, mTargetAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_information);

        mTargetName = getIntent().getStringExtra(KEY_TARGET_NAME);
        mTargetAddress = getIntent().getStringExtra(KEY_TARGET_ADDRESS);

        if (TextUtils.isEmpty(mTargetAddress)) {
            finish();
        }

        mTVDeviceName = (TextView) findViewById(R.id.b_i_title_name);
        mTVDeviceName.setText(mTargetName);
        mTVDeviceMac = (TextView) findViewById(R.id.b_i_title_address);
        mLVServiceList = (ListView) findViewById(R.id.b_i_services_list);
        mLVServiceList.setOnItemClickListener(this);

        mBleManager = new BleManager(this);
        mBleManager.startManager();
    }

    @Override
    public void onBackPressed() {
        AppLogUtils.outputActivityLog("BleInformationActivity::onBackPressed::System Ready= "
                + (null != mBleManager && mBleManager.isReady()));
        if (null != mBleManager && mBleManager.isReady()) {
            mBleManager.destroyManager();
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        AppLogUtils.outputActivityLog("BleInformationActivity::onBackPressed::System Ready= "
                + (null != mBleManager && mBleManager.isReady()));
        if (null != mBleManager && mBleManager.isReady())
            mBleManager.destroyManager();
        super.finish();
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
                mBleManager.connectToDevice(mTargetAddress);
            } else {
                mTVDeviceMac.setText(R.string.ble_common_le_not_enable);
            }
        }
    }

    @Override
    public void onLESwitch(int current_state) {
        //Do nothing here
    }

    @Override
    public void onLEScan(int scan_process, String device_name, String device_class, String device_mac, int device_rssi, byte[] broadcast_content) {
        //Do nothing here
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
                mLVServiceList.setAdapter(mServiceAdapter);
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
}
