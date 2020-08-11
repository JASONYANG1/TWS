package com.wxson.tws_transmitter

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName
    private val bluetoothAdapter : BluetoothAdapter?
    private val deviceList: MutableList<BluetoothDevice> = ArrayList()

    //region for LiveData
    private var deviceListLiveData = MutableLiveData<MutableList<BluetoothDevice>>()
    fun getDeviceList() : LiveData<MutableList<BluetoothDevice>> {
        return deviceListLiveData
    }
    //endregion


    init {
        Log.i(TAG, "init")
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val bluetoothReceiver = BluetoothReceiver(deviceList)
        application.registerReceiver(bluetoothReceiver, filter)
    }

    override fun onCleared() {
        Log.i(TAG, "onCleared")
        super.onCleared()
    }

    //检测设备是否支持蓝牙
    fun hasBluetoothAdapter(): Boolean {
        return (bluetoothAdapter != null)
    }

    fun openBluetooth(currActivity: Activity) {
        if (bluetoothAdapter != null){
            if (!bluetoothAdapter.isEnabled) {
                //请求用户开启蓝牙
                val intent = Intent()
                intent.action = BluetoothAdapter.ACTION_REQUEST_ENABLE
//                intent.action = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
                intent.categories.add(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200)
                currActivity.startActivityForResult(intent, Constants.BluetoothRequestCode)
            }
        }
    }

    fun closeBluetooth(deviceDeviceListAdapter : DeviceListAdapter<BluetoothDevice>){
        if (bluetoothAdapter != null){
            bluetoothAdapter.disable()
            deviceList.clear()
//            deviceDeviceListAdapter.refresh(deviceList)
        }
    }

    fun searchBluetooth(){
        bluetoothAdapter?.startDiscovery()
    }

}