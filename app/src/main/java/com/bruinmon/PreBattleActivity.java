package com.bruinmon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class PreBattleActivity extends AppCompatActivity {

    private final PreBattleActivity activity = this;
    private DeviceListAdapter devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_battle);

        ListView listView = findViewById(R.id.paired_devices);
        devices = new DeviceListAdapter(new ArrayList<BluetoothDevice>(), getApplicationContext());
        listView.setAdapter(devices);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Challenge/connect to the tapped on device from the device list
                BluetoothDevice device = devices.getItem(position);
                Intent intent = new Intent(activity, ChooseBruinActivity.class);
                intent.putExtra("is_ai_battle", false);
                intent.putExtra("is_hosting", false);
                intent.putExtra("opponent_device", device);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.button_await_challenge).setVisibility(View.GONE);
        findViewById(R.id.paired_devices_title).setVisibility(View.GONE);
        findViewById(R.id.paired_devices).setVisibility(View.GONE);
    }

    /** Called when the user touches the Battle vs AI button **/
    public void battleAI(View view) {
        Intent intent = new Intent(this, ChooseBruinActivity.class);
        intent.putExtra("is_ai_battle", true);
        startActivity(intent);
    }

    /** Called when the user touches the Battle vs Player button **/
    public void battlePlayer(View view) {
        // Show the battle player GUI elements
        findViewById(R.id.button_await_challenge).setVisibility(View.VISIBLE);
        findViewById(R.id.paired_devices_title).setVisibility(View.VISIBLE);
        findViewById(R.id.paired_devices).setVisibility(View.VISIBLE);

        // Turn Bluetooth on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported by device", Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return;
        }

        // Update list of paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() < 1) {
            Toast.makeText(getApplicationContext(), "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
            return;
        }
        devices.clear();
        for (BluetoothDevice device : pairedDevices) {
            devices.add(device);
        }
    }

    /** Called when the user touches the Await Challenger button **/
    public void awaitChallenge(View view) {
        Intent intent = new Intent(this, ChooseBruinActivity.class);
        intent.putExtra("is_ai_battle", false);
        intent.putExtra("is_hosting", true);
        startActivity(intent);
    }
}
