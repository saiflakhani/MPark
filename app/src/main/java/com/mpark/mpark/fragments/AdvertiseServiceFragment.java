/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package com.mpark.mpark.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mpark.mpark.R;
import com.mpark.mpark.activities.StartParkingActivity;

import java.util.UUID;


public class AdvertiseServiceFragment extends ServiceFragment {

//  UUID uuid;
  //private static final UUID uuid = UUID
         // .fromString("343697e0-e0e9-11e7-bb40-6f6e6c696e65");
  private static final UUID uuid = UUID
          .fromString("2120970a-e0e9-11e7-ba4b-6f8975959200");
  private static final UUID characteristics_uuid = UUID
          .fromString("34369a6a-e0e9-11e7-ba4b-6f6e6c696e65");
  private static final String BT_DESCRIPTION = "Test description";

  private ServiceFragmentDelegate mDelegate;
  private final OnClickListener mNotifyButtonListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mDelegate.sendNotificationToDevices(mBatteryLevelCharacteristic);
    }
  };

  // GATT
  private BluetoothGattService mBatteryService;
  private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

  public AdvertiseServiceFragment() {
    mBatteryLevelCharacteristic =
        new BluetoothGattCharacteristic(characteristics_uuid,
            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ);

    mBatteryLevelCharacteristic.addDescriptor(
        StartParkingActivity.getClientCharacteristicConfigurationDescriptor());

    mBatteryLevelCharacteristic.addDescriptor(
        StartParkingActivity.getCharacteristicUserDescriptionDescriptor(BT_DESCRIPTION));


    mBatteryService = new BluetoothGattService(uuid,
        BluetoothGattService.SERVICE_TYPE_PRIMARY);
    mBatteryService.addCharacteristic(mBatteryLevelCharacteristic);
  }

  // Lifecycle callbacks
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_battery, container, false);

    String deviceId = "A";
//    uuid = UUID.nameUUIDFromBytes(deviceId.getBytes());

    Button notifyButton = (Button) view.findViewById(R.id.button_batteryLevelNotify);
    notifyButton.setOnClickListener(mNotifyButtonListener);

    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mDelegate = (ServiceFragmentDelegate) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement ServiceFragmentDelegate");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mDelegate = null;
  }

  public BluetoothGattService getBluetoothGattService() {
    return mBatteryService;
  }

  @Override
  public ParcelUuid getServiceUUID() {
    return new ParcelUuid(uuid);
  }

  @Override
  public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
    if (characteristic.getUuid() != characteristics_uuid) {
      return;
    }
    if (indicate) {
      return;
    }
    /*getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getActivity(), R.string.notificationsEnabled, Toast.LENGTH_SHORT)
            .show();
      }
    });*/
  }

  @Override
  public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
    if (characteristic.getUuid() != characteristics_uuid) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getActivity(), R.string.notificationsNotEnabled, Toast.LENGTH_SHORT)
            .show();
      }
    });
  }
}
