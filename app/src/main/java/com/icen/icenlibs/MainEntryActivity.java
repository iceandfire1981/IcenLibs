package com.icen.icenlibs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainEntryActivity extends AppCompatActivity {

    public static final String ACTION_BLE_CONFIG = "com.icen.icenlibs.BLE_CONFIG";
    public static final String ACTION_BLE_DEMO = "com.icen.icenlibs.BLE_DEMO";

    /** Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_entry);

    }

    public void onClick(View click_view) {
        Intent start_intent = new Intent();
        start_intent.setAction(ACTION_BLE_DEMO);
        switch (click_view.getId()){
            case R.id.entry_ble:
                start_intent.setAction(ACTION_BLE_DEMO);
                break;
            case R.id.entry_ble_config:
                start_intent.setAction(ACTION_BLE_CONFIG);
                break;
            default:
                break;
        }
        start_intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(start_intent);

    }
}
