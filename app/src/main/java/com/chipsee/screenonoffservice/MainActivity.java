package com.chipsee.screenonoffservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startIntent = new Intent(MainActivity.this,ScreenOnOffService.class);
        startService(startIntent);
        // Don't need Activity, need also set android:theme="@android:style/Theme.NoDisplay" in
        // AndroidManifest.xml and must add finish(), only service work.
        //setContentView(R.layout.activity_main);
        finish();
    }
}
