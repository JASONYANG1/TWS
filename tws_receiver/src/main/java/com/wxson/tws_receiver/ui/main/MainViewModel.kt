package com.wxson.tws_receiver.ui.main

import androidx.lifecycle.ViewModel

//import android.bluetooth.BluetoothA2dpSink

class MainViewModel : ViewModel() {
    private var A2dpSinkClass : Class<*> = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothA2dpSink")
    // TODO: Implement the ViewModel
}
