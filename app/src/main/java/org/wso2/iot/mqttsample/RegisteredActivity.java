/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.iot.mqttsample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.iot.mqttsample.mqtt.transport.TransportHandlerException;
import org.wso2.iot.mqttsample.services.DeviceManagementService;
import org.wso2.iot.mqttsample.util.LocalRegistry;

public class RegisteredActivity extends Activity {

    private DeviceManagementService deviceManagementService;
    private boolean isBound;

    private ServiceConnection deviceManagmentServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DeviceManagementService.DeviceManagementBinder binder = (DeviceManagementService.DeviceManagementBinder) iBinder;
            deviceManagementService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        Intent serviceIntent = new Intent(this, DeviceManagementService.class);
        startService(serviceIntent);
        Intent intent = new Intent(this, DeviceManagementService.class);
        bindService(intent, deviceManagmentServiceConnection, Context.BIND_AUTO_CREATE);

        final Button unregisterBtn = findViewById(R.id.button);
        unregisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregister();
            }
        });
        final Button paymentBtn = findViewById(R.id.Bpayment);
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish();

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean unregister() {
        if (!LocalRegistry.isExist(getApplicationContext())) {
            Intent activity = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(activity);
        }
        LocalRegistry.removeUsername(getApplicationContext());
        LocalRegistry.removeDeviceId(getApplicationContext());
        LocalRegistry.removeServerURL(getApplicationContext());
        LocalRegistry.removeAccessToken(getApplicationContext());
        LocalRegistry.removeRefreshToken(getApplicationContext());
        LocalRegistry.removeMqttEndpoint(getApplicationContext());
        LocalRegistry.setExist(false);

        //Stop current running background services.
        Intent myService = new Intent(this, DeviceManagementService.class);
        stopService(myService);

        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
        registerActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(registerActivity);
        finish();
        return true;
    }
    private void publish() {
        CheckBox ch1,ch2;

        //Create JSONObject here
        ch1= findViewById(R.id.checkbox_Item1);
        ch2= findViewById(R.id.checkBox_item2);


        JSONObject jsonObject = new JSONObject();
        try {
            if (ch1.isChecked()) {
                jsonObject.put("Item1","Item1");
            }
            if (ch2.isChecked()) {
                jsonObject.put("Item2", "Item2");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isBound) {
            try {
                String payload = jsonObject.toString();
                deviceManagementService.publishMessage(payload);
            } catch (TransportHandlerException e) {
                e.printStackTrace();
            }
        }

    }
}
