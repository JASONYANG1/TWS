package com.wxson.tws_transmitter

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
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
        val bluetoothReceiver = BluetoothReceiver()
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
                intent.categories.add(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200)
                currActivity.startActivityForResult(intent, Constants.BluetoothRequestCode)
            }
        }
    }

    fun closeBluetooth(){
        if (bluetoothAdapter != null){
            bluetoothAdapter.disable()
            deviceList.clear()
            deviceListLiveData.postValue(deviceList)
        }
    }

    fun searchBluetooth(){
        bluetoothAdapter?.startDiscovery()
    }

    //执行蓝牙设备绑定
    fun createBond(position : Int) {
        val bluetoothDevice = deviceList[position]
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            try {
                val createBond = BluetoothDevice::class.java.getMethod("createBond")
                createBond.invoke(bluetoothDevice)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private inner class BluetoothReceiver  : BroadcastReceiver(){
        private val TAG = this.javaClass.simpleName
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    //发现设备
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null){
                        deviceList.add(device)
                        deviceListLiveData.postValue(deviceList)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    //扫描结束
                    deviceListLiveData.postValue(deviceList)
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    //状态发生改变（监听设备连接状态）
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device?.bondState){
                        BluetoothDevice.BOND_NONE -> {
                            Log.i(TAG, "没有设备")
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            Log.i(TAG, "正在匹配中")
                        }
                        BluetoothDevice.BOND_BONDED -> {
                            Log.i(TAG, "匹配成功")
                            ConnectionManager(device)
                        }
                    }
                }
            }
        }
    }
}