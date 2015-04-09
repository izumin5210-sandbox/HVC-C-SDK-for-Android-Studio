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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

interface BleInterface {
    abstract void connect(Context context, BluetoothDevice device);
    abstract void disconnect();
    abstract int setDeviceName(byte[] value);
    abstract int getDeviceName(byte[] value);
}

/**
 * HVC-C BLE Model<br>
 * [Description]<br>
 * HVC subclass, connects HVC to Bluetooth<br>
 * 
 */
public class HVC_BLE extends HVC implements BleInterface
{
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_BUSY = 3;

    private int mStatus = STATE_DISCONNECTED;
    private byte[] mtxName = null;
    private ArrayList<Byte> mtxValue = null;

    private HVCBleCallback mCallback = null;
    private BluetoothDevice mBtDevice = null;
    private BleDeviceService mService = null;

    private static final String TAG = "HVC_BLE";

    /**
     * HVC_BLE constructor<br>
     * [Description]<br>
     * Set HVC_BLE to new to automatically connect to Bluetooth device specified with btDevice<br>
     * @param mainAct Activity object<br>
     */
    public HVC_BLE()
    {
        super();
        mStatus = STATE_DISCONNECTED;
        mtxValue = new ArrayList<Byte>();
    }

    /**
     * HVC_BLE finalizer<br>
     * [Description]<br>
     * MUST be called when ending<br>
     * @throws Throwable <br>
     */
    @Override
    public void finalize() throws Throwable {
        if ( mService != null ) {
            do {
                if ( mStatus <= STATE_CONNECTED ) {
                    break;
                }
            } while ( true );
            mStatus = STATE_DISCONNECTED;
            mService.close();
        }
        mService = null;
        super.finalize();
    }

    /**
     * HVC_BLE verify status<br>
     * [Description]<br>
     * Verify HVC_BLE device status<br>
     * @return boolean true:function executable, false:function non-executable<br>
     */
    @Override
    public boolean IsBusy() {
        if ( mStatus != STATE_CONNECTED ) {
            return true;
        }
        return false;
    }

    /**
     * Execute HVC functions<br>
     * [Description]<br>
     * Execute each HVC function. Store results in HVC_RES<br>
     * @param inExec execution flag<br>
     * @param res results stored<br>
     * @return int execution result error code <br>
     */
    @Override
    public int execute(final int inExec, final HVC_RES res)
    {
        if ( mBtDevice == null ) {
            Log.d(TAG, "execute() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "execute() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "execute() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

        Thread t = new Thread() {
            public void run() {
                int nRet = HVC_NORMAL;
                byte[] outStatus = new byte[1];
                nRet = Execute(30000, inExec, outStatus, res);

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }
                if ( mCallback != null ) {
                    mCallback.onPostExecute(nRet, outStatus[0]);
                }
            }
        };

        t.start();
        Log.d(TAG, "execute() : HVC_NORMAL");
        return HVC_NORMAL;
    }

    /**
     * Set HVC Parameters<br>
     * [Description]<br>
     * Set each HVC parameter. <br>
     * @param prm parameter<br>
     * @return int execution result error code <br>
     */
    @Override
    public int setParam(final HVC_PRM prm)
    {
        if ( mBtDevice == null ) {
            Log.d(TAG, "setParam() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "setParam() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "setParam() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

        Thread t = new Thread() {
            public void run() {
                int nRet = HVC_NORMAL;
                byte[] outStatus = new byte[1];
                nRet = SetCameraAngle(10000, outStatus, prm);
                if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
                    nRet = SetThreshold(10000, outStatus, prm);
                }
                if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
                    nRet = SetSizeRange(10000, outStatus, prm);
                }
                if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
                    nRet = SetFaceDetectionAngle(10000, outStatus, prm);
                }

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }
                if ( mCallback != null ) {
                    mCallback.onPostSetParam(nRet, outStatus[0]);
                }
            }
        };

        t.start();
        Log.d(TAG, "setParam() : HVC_NORMAL");
        return HVC_NORMAL;
    }

    /**
     * Get HVC Parameters<br>
     * [Description]<br>
     * Get each HVC parameter. <br>
     * @param prm parameter stored<br>
     * @return int execution result error code <br>
     */
    public int getParam(final HVC_PRM prm) {
        if ( mBtDevice == null ) {
            Log.d(TAG, "getParam() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "getParam() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "getParam() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

        Thread t = new Thread() {
            public void run() {
                int nRet = HVC_NORMAL;
                byte[] outStatus = new byte[1];
                if ( nRet == HVC_NORMAL ) {
                    nRet = GetCameraAngle(10000, outStatus, prm);
                }
                if ( nRet == HVC_NORMAL ) {
                    nRet = GetThreshold(10000, outStatus, prm);
                }
                if ( nRet == HVC_NORMAL ) {
                    nRet = GetSizeRange(10000, outStatus, prm);
                }
                if ( nRet == HVC_NORMAL ) {
                    nRet = GetFaceDetectionAngle(10000, outStatus, prm);
                }

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }
                if ( mCallback != null ) {
                    mCallback.onPostGetParam(nRet, outStatus[0]);
                }
            }
        };

        t.start();
        Log.d(TAG, "getParam() : HVC_NORMAL");
        return HVC_NORMAL;
    }

    /**
     * Get HVC Version<br>
     * [Description]<br>
     * Get HVC Version. <br>
     * @param ver version stored<br>
     * @return int execution result error code <br>
     */
    @Override
    public int getVersion(final HVC_VER ver) {
        if ( mBtDevice == null ) {
            Log.d(TAG, "getVersion() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "getVersion() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "getVersion() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

        Thread t = new Thread() {
            public void run() {
                int nRet = HVC_NORMAL;
                byte[] outStatus = new byte[1];
                if ( nRet == HVC_NORMAL ) {
                    nRet = GetVersion(10000, outStatus, ver);
                }

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }
                if ( mCallback != null ) {
                    mCallback.onPostGetVersion(nRet, outStatus[0]);
                }
            }
        };

        t.start();
        Log.d(TAG, "getVersion() : HVC_NORMAL");
        return HVC_NORMAL;
    }

    private final BleCallback gattCallback = new BleCallback() {
        // Notice of completion of device connection/disconnection
        @Override
        public void callbackMethod(String action) {
            //*********************//
            if (action.equals(BleDeviceService.ACTION_GATT_CONNECTED)) {
                Log.d(TAG, "UART_CONNECT_MSG");
                mStatus = STATE_CONNECTING;
            }
            
            //*********************//
            if (action.equals(BleDeviceService.ACTION_GATT_DISCONNECTED)) {
                Log.d(TAG, "UART_DISCONNECT_MSG");
                mService.close();
                mStatus = STATE_DISCONNECTED;
                if ( mCallback != null ) {
                    mCallback.onDisconnected();
                }
            }
            
            //*********************//
            if (action.equals(BleDeviceService.ACTION_GATT_SERVICES_DISCOVERED)) {
                Log.d(TAG, "UART_DISCOVERED_MSG");
                mStatus = STATE_CONNECTED;
                if ( mCallback != null ) {
                    mCallback.onConnected();
                }
            }

            //*********************//
            if (action.equals(BleDeviceService.DEVICE_DOES_NOT_SUPPORT_UART)){
                Log.d(TAG, "DEVICE_DOES_NOT_SUPPORT_UART");
                mService.disconnect();
                mStatus = STATE_DISCONNECTED;
            }
        }

        // Notice of data/device name reception 
        @Override
        public void callbackMethod(String action, byte[] byText) {
            //*********************//
            if (action.equals(BleDeviceService.ACTION_DATA_AVAILABLE+BleDeviceService.EXTRA_DATA)) {
                if ( byText != null ) {
                    String deviceInfo = "DATA_AVAILABLE: " + String.valueOf(byText.length) + " byte";
                    synchronized (mtxValue) {
                        for ( int i=0; i<byText.length; i++ ) {
                            mtxValue.add(byText[i]);
                            //deviceInfo += String.valueOf(byText[i]) + " ";
                        }
                    }
                    Log.d(TAG, deviceInfo);
                    sleep(1);
                }
            }

            //*********************//
            if (action.equals(BleDeviceService.ACTION_DATA_AVAILABLE+BleDeviceService.NAME_DATA)) {
                if ( byText != null && mtxName != null ) {
                    String deviceInfo = "NAME_AVAILABLE: " + String.valueOf(byText.length) + " byte";
                    synchronized (mtxName) {
                        for ( int i=0; i<byText.length; i++ ) {
                            mtxName[i] = byText[i];
                            //deviceInfo += String.valueOf(mtxName[i]) + " ";
                        }
                    }
                    Log.d(TAG, deviceInfo);
                    if ( mCallback != null ) {
                        mCallback.onPostGetDeviceName(mtxName);
                    }
                }
            }
        }
    };

    /**
     * Bluetooth send signal<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inData send signal data<br>
     * @return int send signal complete data number<br>
     */
    @Override
    protected int Send(byte[] inData)
    {
        do {
            if ( mStatus < STATE_CONNECTED ) {
                return 0;
            }
            synchronized(mtxValue){
                int readLength = mtxValue.size();
                if ( readLength <= 0 ) break;
                mtxValue.clear();
            }
        } while ( true );
        mService.writeTXCharacteristic(inData);

        String deviceInfo = "Send: " + inData.length + " byte";
        Log.d(TAG, deviceInfo);
        return inData.length;
    }

    /**
     * Bluetooth receive signal<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param inDataSize receive signal data size<br>
     * @param outResult receive signal data<br>
     * @return int receive signal complete data number<br>
     */
    @Override
    protected int Receive(int inTimeOutTime, int inDataSize, byte[] outResult)
    {
        long maxTimeMillis = System.currentTimeMillis() + inTimeOutTime;
        while (System.currentTimeMillis() < maxTimeMillis) {
            if ( mStatus < STATE_CONNECTED ) {
                return 0;
            }
            int readLength = mtxValue.size();
            if ( readLength >= inDataSize ) {
                break;
            } else {
                sleep(1);
            }
        }
        int readLength = 0;
        synchronized(mtxValue){
            readLength = Math.min(mtxValue.size(),inDataSize);
            for ( int i=0; i<readLength; i++ ) {
                outResult[i] = mtxValue.get(i);
            }
            for ( int i=0; i<readLength; i++ ) {
                mtxValue.remove(0);
            }
        }
        String deviceInfo = "Receive: " + String.valueOf(readLength) + " byte";
        Log.d(TAG, deviceInfo);
        return readLength;
    }

    protected synchronized void sleep(long msec) {
        // Method to stop execution after set number of msec
        try {
            wait(msec);
        } catch(InterruptedException e){}
    }

    public void setCallBack(HVCBleCallback hvcCallback) {
        mCallback = hvcCallback;
        Log.d(TAG, "Set CallBack");
    }

    /**
     * HVC_BLE connect<br>
     * [Description]<br>
     * Connect with HVC_BLE device<br>
     */
    @Override
    public void connect(Context context, BluetoothDevice device) {
        mStatus = STATE_DISCONNECTED;
        if ( mService != null ) {
            Log.d(TAG, "DisConnect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
            mService.close();
        }

        mBtDevice = device;
        if ( mBtDevice == null ) {
            return;
        }

        mService = new BleDeviceService(gattCallback);

        mService.connect(context, mBtDevice);
        Log.d(TAG, "Connect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
    }

    /**
     * HVC_BLE disconnect<br>
     * [Description]<br>
     * Disconnect HVC_BLE device<br>
     */
    @Override
    public void disconnect() {
        mStatus = STATE_DISCONNECTED;
        if ( mService != null ) {
            Log.d(TAG, "DisConnect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
            mService.close();
        }
        //mService = null;
        if ( mCallback != null ) {
            mCallback.onDisconnected();
        }
    }

    /**
     * Set HVC Device name<br>
     * [Description]<br>
     * Set HVC Device name.<br>
     * @param value setting device name<br>
     * @return int execution result error code <br>
     */
    @Override
    public int setDeviceName(byte[] value) {
        if ( mBtDevice == null ) {
            Log.d(TAG, "setDeviceName() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "setDeviceName() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "setDeviceName() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mService.writeNameCharacteristic(value);
        Log.d(TAG, "setDeviceName() : HVC_NORMAL");
        return HVC_NORMAL;
    }

    /**
     * Get HVC Device name<br>
     * [Description]<br>
     * Get HVC Device name. Store name in value<br>
     * @param value device name stored<br>
     * @return int execution result error code <br>
     */
    @Override
    public int getDeviceName(byte[] value) {
        if ( mBtDevice == null ) {
            Log.d(TAG, "getDeviceName() : HVC_ERROR_NODEVICES");
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            Log.d(TAG, "getDeviceName() : HVC_ERROR_DISCONNECTED");
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            Log.d(TAG, "getDeviceName() : HVC_ERROR_BUSY");
            return HVC_ERROR_BUSY;
        }

        mtxName = value;
        mService.readNameCharacteristic();
        Log.d(TAG, "getDeviceName() : HVC_NORMAL");
        return HVC_NORMAL;
    }
}
