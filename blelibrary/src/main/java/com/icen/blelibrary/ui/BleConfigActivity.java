package com.icen.blelibrary.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;

import com.icen.blelibrary.R;
import com.icen.blelibrary.config.BleLibsConfig;

public class BleConfigActivity extends AppCompatActivity {

    private EditText mETOvertime;
    private Switch   mSHAutoConnect;

    private long mOvertime;
    private boolean mAutoConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_config);
        mETOvertime = (EditText) findViewById(R.id.config_over_time);
        mSHAutoConnect = (Switch) findViewById(R.id.config_auto_connect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAutoConnect = BleLibsConfig.getAutoConnectInFile(this);
        mOvertime = BleLibsConfig.getScanOvertime(this);
        mETOvertime.setText(String.valueOf(mOvertime));
        mSHAutoConnect.setChecked(mAutoConnect);
    }

    @Override
    public void finish() {
        BleLibsConfig.saveAutoConnectInFile(this, mSHAutoConnect.isChecked());
        BleLibsConfig.saveScanOvertime(this, Long.parseLong(mETOvertime.getText().toString()));
        super.finish();
    }
}
