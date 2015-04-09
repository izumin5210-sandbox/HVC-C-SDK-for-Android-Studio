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

interface BleCallbackInterface {
    abstract void onConnected();
    abstract void onDisconnected();
    abstract void onPostGetDeviceName(byte[] value);
}

public class HVCBleCallback extends HVCCallback implements BleCallbackInterface {
    /**
     * Return SetParam execution result and error code <br>
     * @param nRet execution result error code <br>
     * @param outStatus response code<br>
     */
    @Override
    public void onPostSetParam(int nRet, byte outStatus) {
    }

    /**
     * Return GetParam execution result and error code <br>
     * @param nRet execution result error code <br>
     * @param outStatus response code<br>
     */
    @Override
    public void onPostGetParam(int nRet, byte outStatus) {
    }

    /**
     * Return GetVersion execution result and error code <br>
     * @param nRet execution result error code <br>
     * @param outStatus response code<br>
     */
    @Override
    public void onPostGetVersion(int nRet, byte outStatus) {
    }

    /**
     * Return Execute execution result and error code <br>
     * @param nRet execution result error code <br>
     * @param outStatus response code<br>
     */
    @Override
    public void onPostExecute(int nRet, byte outStatus) {
    }

    // Notice of completion of connection 
    @Override
    public void onConnected() {
    }

    // Notice of completion of disconnection
    @Override
    public void onDisconnected() {
    }

    // Notice of device name reception
    @Override
    public void onPostGetDeviceName(byte[] value) {
    }
}
