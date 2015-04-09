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

package omron.SimpleDemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.izumin.android.hvc_c.simpledemo.R;
import omron.HVC.BleDeviceSearch;
import omron.HVC.HVC;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;
import omron.HVC.HVCBleCallback;

public class MainActivity extends Activity {
    public static final int EXECUTE_STOP = 0;
    public static final int EXECUTE_START = 1;
    public static final int EXECUTE_END = -1;

    private HVC_BLE hvcBle = null;
    private HVC_PRM hvcPrm = null;
    private HVC_RES hvcRes = null;

    private HVCDeviceThread hvcThread = null;

    private static int isExecute = 0;
    private static int nSelectDeviceNo = -1;
    private static List<BluetoothDevice> deviceList = null;
    private static DeviceDialogFragment newFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        hvcBle = new HVC_BLE();
        hvcPrm = new HVC_PRM();
        hvcRes = new HVC_RES();

        hvcBle.setCallBack(hvcCallback);
        hvcThread = new HVCDeviceThread();
        hvcThread.start();
    }

    @Override
    public void onDestroy() {
        isExecute = EXECUTE_END;
        while ( isExecute == EXECUTE_END );
        if ( hvcBle != null ) {
            try {
                hvcBle.finalize();
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        hvcBle = null;
        super.onDestroy();
    }

    private class HVCDeviceThread extends Thread {
        @Override
        public void run()
        {
            isExecute = EXECUTE_STOP;
            while (isExecute != EXECUTE_END) {
                BluetoothDevice device = SelectHVCDevice("OMRON_HVC.*|omron_hvc.*");
                if ( (device == null) || (isExecute != EXECUTE_START) ) {
                    continue;
                }

                hvcBle.connect(getApplicationContext(), device);
                wait(15);

                hvcPrm.cameraAngle = HVC_PRM.HVC_CAMERA_ANGLE.HVC_CAMERA_ANGLE_0;
                hvcPrm.face.MinSize = 100;
                hvcPrm.face.MaxSize = 400;
                hvcBle.setParam(hvcPrm);
                wait(15);

                while ( isExecute == EXECUTE_START ) {
                    int nUseFunc = HVC.HVC_ACTIV_BODY_DETECTION |
                                   HVC.HVC_ACTIV_HAND_DETECTION |
                                   HVC.HVC_ACTIV_FACE_DETECTION |
                                   HVC.HVC_ACTIV_FACE_DIRECTION |
                                   HVC.HVC_ACTIV_AGE_ESTIMATION |
                                   HVC.HVC_ACTIV_GENDER_ESTIMATION |
                                   HVC.HVC_ACTIV_GAZE_ESTIMATION |
                                   HVC.HVC_ACTIV_BLINK_ESTIMATION |
                                   HVC.HVC_ACTIV_EXPRESSION_ESTIMATION;
                    hvcBle.execute(nUseFunc, hvcRes);
                    wait(30);
                }
                hvcBle.disconnect();
            }
            isExecute = EXECUTE_STOP;
        }

        public void wait(int nWaitCount)
        {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if ( !hvcBle.IsBusy() ) {
                    return;
                }
                nWaitCount--;
            } while ( nWaitCount > 0 );
        }
    }

    private BluetoothDevice SelectHVCDevice(String regStr) {
        if ( nSelectDeviceNo < 0 ) {
            if ( newFragment != null ) {
                BleDeviceSearch bleSearch = new BleDeviceSearch(getApplicationContext());
                // Show toast
                showToast("You can select a device");
                while ( newFragment != null ) {
                    deviceList = bleSearch.getDevices();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                bleSearch.stopDeviceSearch(getApplicationContext());
            }

            if ( nSelectDeviceNo > -1 ) {
                // Generate pattern to determine
                Pattern p = Pattern.compile(regStr);
                Matcher m = p.matcher(deviceList.get(nSelectDeviceNo).getName());
                if ( m.find() ) {
                    // Find HVC device
                    return deviceList.get(nSelectDeviceNo);
                }
                nSelectDeviceNo = -1;
            }
            return null;
        }
        return deviceList.get(nSelectDeviceNo);
    }

    private final HVCBleCallback hvcCallback = new HVCBleCallback() {
        @Override
        public void onConnected() {
            // Show toast
            showToast("Selected device has connected");
        }

        @Override
        public void onDisconnected() {
            // Show toast
            showToast("Selected device has disconnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button bt = (Button) findViewById(R.id.button2);
                    bt.setText(R.string.buttonS);
                }
            });
            isExecute = EXECUTE_STOP;
        }

        @Override
        public void onPostSetParam(int nRet, byte outStatus) {
            // Show toast
            String str = "Set parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
            showToast(str);
        }

        @Override
        public void onPostGetParam(int nRet, byte outStatus) {
            // Show toast
            String str = "Get parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
            showToast(str);
        }

        @Override
        public void onPostExecute(int nRet, byte outStatus) {
            if ( nRet != HVC.HVC_NORMAL || outStatus != 0 ) {
                String str = "Execute : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
                showToast(str);
            } else {
                String str = "Body Detect = " + String.format("%d\n", hvcRes.body.size());
                for (DetectionResult bodyResult : hvcRes.body) {
                    int size = bodyResult.size;
                    int posX = bodyResult.posX;
                    int posY = bodyResult.posY;
                    int conf = bodyResult.confidence;
                    str += String.format("  [Body Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                }
                str += "Hand Detect = " + String.format("%d\n", hvcRes.hand.size());
                for (DetectionResult handResult : hvcRes.hand) {
                    int size = handResult.size;
                    int posX = handResult.posX;
                    int posY = handResult.posY;
                    int conf = handResult.confidence;
                    str += String.format("  [Hand Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                }
                str += "Face Detect = " + String.format("%d\n", hvcRes.face.size());
                for (FaceResult faceResult : hvcRes.face) {
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_FACE_DETECTION) != 0 ) {
                        int size = faceResult.size;
                        int posX = faceResult.posX;
                        int posY = faceResult.posY;
                        int conf = faceResult.confidence;
                        str += String.format("  [Face Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0 ) {
                        str += String.format("  [Face Direction] : yaw = %d, pitch = %d, roll = %d, conf = %d\n", 
                                                    faceResult.dir.yaw, faceResult.dir.pitch, faceResult.dir.roll, faceResult.dir.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0 ) {
                        str += String.format("  [Age Estimation] : age = %d, conf = %d\n", 
                                                    faceResult.age.age, faceResult.age.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0 ) {
                        str += String.format("  [Gender Estimation] : gender = %s, confidence = %d\n", 
                                                    faceResult.gen.gender == HVC.HVC_GEN_MALE ? "Male" : "Female", faceResult.gen.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0 ) {
                        str += String.format("  [Gaze Estimation] : LR = %d, UD = %d\n", 
                                                    faceResult.gaze.gazeLR, faceResult.gaze.gazeUD);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0 ) {
                        str += String.format("  [Blink Estimation] : ratioL = %d, ratioR = %d\n", 
                                                    faceResult.blink.ratioL, faceResult.blink.ratioR);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0 ) {
                        str += String.format("  [Expression Estimation] : expression = %s, score = %d, degree = %d\n", 
                                                    faceResult.exp.expression == HVC.HVC_EX_NEUTRAL ? "Neutral" :
                                                    faceResult.exp.expression == HVC.HVC_EX_HAPPINESS ? "Happiness" :
                                                    faceResult.exp.expression == HVC.HVC_EX_SURPRISE ? "Surprise" :
                                                    faceResult.exp.expression == HVC.HVC_EX_ANGER ? "Anger" :
                                                    faceResult.exp.expression == HVC.HVC_EX_SADNESS ? "Sadness" : "" ,
                                                    faceResult.exp.score, faceResult.exp.degree);
                    }
                }
                final String viewText = str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvVer = (TextView) findViewById(R.id.textView1);
                        tvVer.setText(viewText);
                    }
                });
            }
        }
    };

    public void onClick1(View view) {
        switch (view.getId()){
            case R.id.button1:
                if ( isExecute == EXECUTE_START ) {
                    // Show toast
                    Toast.makeText(this, "You are executing now", Toast.LENGTH_SHORT).show();
                    break;
                }
                nSelectDeviceNo = -1;
                newFragment = new DeviceDialogFragment();
                newFragment.setCancelable(false);
                newFragment.show(getFragmentManager(), "Bluetooth Devices");
                break;
        }
    }

    public void onClick2(View view) {
        switch (view.getId()){
            case R.id.button2:
                if ( nSelectDeviceNo == -1 ) {
                    // Show toast
                    Toast.makeText(this, "You must select device", Toast.LENGTH_SHORT).show();
                    break;
                }
                if ( isExecute == EXECUTE_STOP ) {
                    Button bt = (Button) findViewById(R.id.button2);
                    bt.setText(R.string.buttonE);
                    isExecute = EXECUTE_START;
                } else
                if ( isExecute == EXECUTE_START ) {
                    Button bt = (Button) findViewById(R.id.button2);
                    bt.setText(R.string.buttonS);
                    isExecute = EXECUTE_STOP;
                }
                break;
        }
    }

    public void showToast(final String str) {
        // Show toast
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class DeviceDialogFragment extends DialogFragment {
        String[] deviceNameList = null;
        ArrayAdapter<String> ListAdpString = null;

        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.devices, null);
            builder.setView(content);

            ListView listView = (ListView)content.findViewById(R.id.devices);
            // Set adapter
            ListAdpString = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice); 
            listView.setAdapter(ListAdpString);

            // Set the click event in the list view
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                /**
                 * It is called when you click on an item
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    nSelectDeviceNo = position;
                    newFragment = null;
                    dismiss();
                }
            });

            DeviceDialogThread dlgThread = new DeviceDialogThread();
            dlgThread.start();

            builder.setMessage(getString(R.string.button1))
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    newFragment = null;
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        private class DeviceDialogThread extends Thread {
            @Override
            public void run()
            {
                do {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ( ListAdpString != null ) {
                                ListAdpString.clear();
                                if ( deviceList == null ) {
                                    deviceNameList = new String[] { "null" };
                                } else {
                                    synchronized (deviceList) {
                                        deviceNameList = new String[deviceList.size()];

                                        int nIndex = 0;
                                        for (BluetoothDevice device : deviceList) {
                                            if (device.getName() == null ) {
                                                deviceNameList[nIndex] = "no name";
                                            } else {
                                                deviceNameList[nIndex] = device.getName();
                                            }
                                            nIndex++;
                                        }
                                    }
                                }
                                ListAdpString.addAll(deviceNameList);
                                ListAdpString.notifyDataSetChanged();
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } while(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.popup_title)
        .setMessage(R.string.popup_message)
        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    finish();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        })
        .setNegativeButton(R.string.popup_no, null)
        .show();
    }
}
