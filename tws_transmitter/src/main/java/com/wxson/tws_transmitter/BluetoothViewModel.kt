package com.wxson.tws_transmitter

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName
    private val app : Application
    private val bluetoothAdapter : BluetoothAdapter?
    private val deviceWithStatusList: MutableList<BluetoothDeviceWithStatus> = ArrayList()
    private lateinit var currentBluetoothDevice : BluetoothDevice

    //region for LiveData
    private var deviceListLiveData = MutableLiveData<MutableList<BluetoothDeviceWithStatus>>()
    fun getDeviceList() : LiveData<MutableList<BluetoothDeviceWithStatus>> {
        return deviceListLiveData
    }

    private var msgLiveData = MutableLiveData<String>()
    fun getMsg(): LiveData<String> {
        return msgLiveData
    }
    //endregion

    init {
        Log.i(TAG, "init")
        app = application
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
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                //请求用户开启蓝牙
                val intent = Intent()
                intent.action = BluetoothAdapter.ACTION_REQUEST_ENABLE
                currActivity.startActivityForResult(intent, Constants.BluetoothRequestCode)
            } else {
                msgLiveData.postValue("蓝牙已开启")
                deviceListLiveData.postValue(deviceWithStatusList)
            }
        }
    }

    fun makeDiscoverable(currActivity: Activity) {
        if (bluetoothAdapter != null) {
            //请求用户设置蓝牙可被发现
            val intent = Intent()
            intent.action = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200)
            currActivity.startActivity(intent)
        }
    }

    fun closeBluetooth(){
        if (bluetoothAdapter != null){
            bluetoothAdapter.disable()
            deviceWithStatusList.clear()
            deviceListLiveData.postValue(deviceWithStatusList)
        }
    }

    fun searchBluetooth(){
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
    }

    //执行蓝牙设备绑定
    fun createBond(position: Int) {
        currentBluetoothDevice = deviceWithStatusList[position].bluetoothDevice
        val msg : String
        try {
            when (currentBluetoothDevice.bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    //使用A2DP协议连接设备
                    bluetoothAdapter?.getProfileProxy(
                        app.baseContext,
                        profileServiceListener,
                        BluetoothProfile.A2DP
                    )
                }
                BluetoothDevice.BOND_BONDING -> {
                }
                BluetoothDevice.BOND_NONE -> {
                    // 如果未配对，实施配对绑定
//                    val createBond = BluetoothDevice::class.java.getMethod("createBond")
//                    createBond.invoke(currentBluetoothDevice)
                    BluetoothUtils.makePair(currentBluetoothDevice)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        TODO("Not yet implemented")
    }

    //解除蓝牙设备绑定
    fun releaseBond(position: Int) : Boolean {
        currentBluetoothDevice = deviceWithStatusList[position].bluetoothDevice
        when (currentBluetoothDevice.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                showDialog("是否取消" + currentBluetoothDevice.name.toString() + "配对？",
                    DialogInterface.OnClickListener { _, _ ->
                        BluetoothUtils.breakPair(currentBluetoothDevice)
                    })
            }
        }
        return false
    }

    //获取所有已经绑定的蓝牙设备
    private fun getBondedDevices() {
        if (deviceWithStatusList.isNotEmpty()) deviceWithStatusList.clear()
        val deviceSet = bluetoothAdapter?.bondedDevices
        if (deviceSet != null) {
            for (device in deviceSet) {
                val bluetoothDevice = BluetoothDeviceWithStatus(device)
                bluetoothDevice.isPaired = true
                deviceWithStatusList.add(bluetoothDevice)
            }
        }
        deviceListLiveData.postValue(deviceWithStatusList)
    }

    //连接蓝牙设备（通过监听蓝牙协议的服务，在连接服务的时候使用BluetoothA2dp协议）
    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            // nothing to do
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            try {
                if (profile == BluetoothProfile.HEADSET) {
                    // nothing to do
                } else if(profile == BluetoothProfile.A2DP) {
                    //使用A2DP的协议连接蓝牙设备
                    val a2dp = proxy as BluetoothA2dp
                    if (a2dp.getConnectionState(currentBluetoothDevice) != BluetoothProfile.STATE_CONNECTED) {
                        a2dp.javaClass
                            .getMethod("connect", BluetoothDevice::class.java)
                            .invoke(a2dp, currentBluetoothDevice)
                        msgLiveData.postValue("请播放音乐")
//                        getBondedDevices()
                    }
                    TODO("Not yet implemented")
                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun showDialog(msg: String?, listener: DialogInterface.OnClickListener?) {
        val alertDialog : AlertDialog = AlertDialog.Builder(app).create()
        alertDialog.setMessage(msg)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "取消",
            DialogInterface.OnClickListener { _, _ -> alertDialog.dismiss() })
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确认", listener)
        alertDialog.show()
    }


    private inner class BluetoothReceiver  : BroadcastReceiver(){
        private val TAG = this.javaClass.simpleName
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    //发现设备
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.bondState != BluetoothDevice.BOND_BONDED) {
                        val bluetoothDevice = BluetoothDeviceWithStatus(device)
                        bluetoothDevice.isPaired = false
                        deviceWithStatusList.add(bluetoothDevice)
                        deviceListLiveData.postValue(deviceWithStatusList)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    //扫描结束
                    deviceListLiveData.postValue(deviceWithStatusList)
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    //状态发生改变（监听设备连接状态）
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device?.bondState) {
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