package com.wxson.tws_receiver

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel


class FirstViewModel(application: Application)  : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName
    private val app = application
    private val bluetoothAdapter : BluetoothAdapter?

    init {
        Log.i(TAG, "init")
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    //Triggered when ViewModel's owner finishes
    override fun onCleared() {
        Log.i(TAG, "onCleared")
        super.onCleared()
    }

    //检测设备是否支持蓝牙
    fun hasBluetoothAdapter() : Boolean{
        return (bluetoothAdapter!=null)
    }

    //开启蓝牙
    fun openBluetoothAdapter(currActivity: Activity) {
        Log.i(TAG, "openBluetoothAdapter")
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                //请求用户开启蓝牙
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                currActivity.startActivityForResult(intent, app.resources.getInteger(R.integer.BluetoothRequestCode))
            }
            else {
                //蓝牙已开启，获取设备清单
//                getDeviceList()

            }
        }
    }

    //获取已绑定的蓝牙设备
    fun showBondDevice(){
        Log.i(TAG, "showBondDevice")
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled){
                val deviceSet = bluetoothAdapter.bondedDevices
                //更新绑定设备表
//                deviceList.clear();
                for (device in deviceSet){
//                    deviceList.add(device);
                }

            }
        }
    }

    //设置蓝牙可见性
    fun setDiscoverable(currActivity: Activity){
        Log.i(TAG, "setDiscoverable")
        if (bluetoothAdapter != null){
            if (bluetoothAdapter.isEnabled){
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)  //可见时间300秒
                currActivity.startActivityForResult(intent, app.resources.getInteger(R.integer.BluetoothRequestCode))
            }
        }

    }
}