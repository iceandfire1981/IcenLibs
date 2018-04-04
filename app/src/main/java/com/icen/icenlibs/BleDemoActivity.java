package com.icen.icenlibs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.Arrays;

public class BleDemoActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener,
        BleManagerCallBack{

    private View mRootView;
    private Switch mSHLESwitch;

    private BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_demo);
        mRootView = findViewById(R.id.ble_demo_le_root);
        ((Button) findViewById(R.id.ble_demo_le_connect_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.ble_demo_le_scan_button)).setOnClickListener(this);
        mSHLESwitch = (Switch) findViewById(R.id.ble_demo_le_switch);

        mRootView.setEnabled(false);
        mBleManager = new BleManager(this);
        mBleManager.setManagerCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleManager.startManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBleManager.destroyManager();
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onCheckedChanged::check= " + isChecked);
        if (null != mBleManager)
            if (isChecked)
                mBleManager.enableBle();
            else
                mBleManager.disableBle();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ble_demo_le_connect_button:
                break;
            case R.id.ble_demo_le_scan_button:
                mBleManager.startScanDevice();
                break;
        }
    }

    @Override
    public void onInitialManager(boolean is_success) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onInitialManager::result= " + is_success);
        mRootView.setEnabled(is_success);
        if (is_success) {
            mSHLESwitch.setChecked(mBleManager.isLeEnabled());
            Toast.makeText(BleDemoActivity.this, "initial manager success", Toast.LENGTH_LONG).show();
            mSHLESwitch.setOnCheckedChangeListener(this);
        } else {
            Toast.makeText(BleDemoActivity.this, "initial manager false", Toast.LENGTH_LONG).show();
            mSHLESwitch.setOnCheckedChangeListener(null);
            mSHLESwitch.setChecked(false);
        }
    }

    @Override
    public void onLESwitch(boolean op_flag, boolean is_success){
        AppLogUtils.outputActivityLog("BleDemoActivity::onLESwitch::op= " + op_flag + " result= " + is_success);
    }

    @Override
    public void onLEScan(int scan_process, String device_name, String device_class, String device_mac, byte[] broadcast_content) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onLEScan::process= " + scan_process + " name= " + device_name +
                                " class= " + device_class + " mac= " + device_mac + " content= " + Arrays.toString(broadcast_content));
    }
}
