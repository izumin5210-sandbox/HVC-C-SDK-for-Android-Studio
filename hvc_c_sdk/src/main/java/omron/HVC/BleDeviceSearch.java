/*
 * Copyright (C) 2014-2015 OMRON Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package omron.HVC;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
Bluetooth Device Search
*/
public class BleDeviceSearch {
    private List<BluetoothDevice> deviceList = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothReceiver mBluetoothReceiver = null;

    public BleDeviceSearch(Context context) {
        deviceList = new ArrayList<BluetoothDevice>();

        // Step 1: Enable Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Bluetooth supported
            if (mBluetoothAdapter.isEnabled()) {
                discoverDevices(context);
            }
        }
    }

    // HVC_BLE Device Search
    private void discoverDevices(Context context) {
        // Step 4: Scan for Bluetooth device
        mBluetoothReceiver = new BluetoothReceiver();
        context.registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mBluetoothAdapter.startDiscovery();

    }

    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                for (BluetoothDevice listDev : deviceList) {
                    if (listDev.getAddress().equals(device.getAddress())) {
                        return;
                    }
                }

                deviceList.add(device);
            }
        }
    }

    public void stopDeviceSearch(Context context) {
        if (mBluetoothAdapter != null &&
                mBluetoothReceiver != null) {
            mBluetoothAdapter.cancelDiscovery();
            context.unregisterReceiver(mBluetoothReceiver);
        }
    }

    // Obtain list of detected HVC_BLE devices
    public List<BluetoothDevice> getDevices() {
        return deviceList;
    }
}
