package com.wxson.tws_receiver.ui.main

import android.bluetooth.BluetoothDevice

class BluetoothDeviceItem (val device : BluetoothDevice,  var connectState : Int = Constants.BluetoothUnconnected) {
}