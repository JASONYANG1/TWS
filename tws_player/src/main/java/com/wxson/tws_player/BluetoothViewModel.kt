package com.wxson.tws_player

import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.*
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = this.javaClass.simpleName
    private val app : Application
    private val bluetoothAdapter : BluetoothAdapter?
    private val deviceWithStatusList: MutableList<BluetoothDeviceWithStatus> = ArrayList()
    private lateinit var currentBluetoothDevice : BluetoothDevice
    private lateinit var bluetoothA2dp : BluetoothA2dp
    private val bluetoothReceiver : BroadcastReceiver

    var context : Context? = null

    //region for LiveData
    private var deviceListLiveData = MutableLiveData<MutableList<BluetoothDeviceWithStatus>>()
    fun getDeviceList() : LiveData<MutableList<BluetoothDeviceWithStatus>> {
        return deviceListLiveData
    }

    private var msgLiveData = MutableLiveData<String>()
    fun getMsg(): LiveData<String> {
        return msgLiveData
    }

    private var showProgressLiveData = MutableLiveData<Boolean>()
    fun getShowProgress() : LiveData<Boolean> {
        return showProgressLiveData
    }
    //endregion

    //连接蓝牙设备（通过监听蓝牙协议的服务，在连接服务的时候使用BluetoothA2dp协议）
    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            Log.i(tag, "onServiceDisconnected")
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            Log.i(tag, "onServiceConnected")
            try {
                if (profile == BluetoothProfile.HEADSET) {
                    Log.i(tag, "BluetoothProfile.HEADSET")
                } else if (profile == BluetoothProfile.A2DP) {
                    //使用A2DP的协议连接蓝牙设备
                    Log.i(tag, "BluetoothProfile.A2DP")
                    bluetoothA2dp = proxy as BluetoothA2dp
                    if (bluetoothA2dp.getConnectionState(currentBluetoothDevice) != BluetoothProfile.STATE_CONNECTED) {
                        bluetoothA2dp.javaClass
                            .getMethod("connect", BluetoothDevice::class.java)
                            .invoke(bluetoothA2dp, currentBluetoothDevice)
                        msgLiveData.postValue("请播放音乐")
                        getBondedDevicesFromBluetoothAdapter()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        Log.i(tag, "init")
        app = application
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        bluetoothReceiver = BluetoothReceiver()
        app.registerReceiver(bluetoothReceiver, filter)
        getBondedDevicesFromBluetoothAdapter()
    }

    override fun onCleared() {
        Log.i(tag, "onCleared")
        app.unregisterReceiver(bluetoothReceiver)
        super.onCleared()
    }

    //检测设备是否支持蓝牙
    fun hasBluetoothAdapter(): Boolean {
        return (bluetoothAdapter != null)
    }

    fun openBluetooth(fragment: Fragment) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                //请求用户开启蓝牙
                val intent = Intent()
                intent.action = BluetoothAdapter.ACTION_REQUEST_ENABLE
                fragment.startActivityForResult(intent, Constants.BluetoothRequestCode)
            } else {
                msgLiveData.postValue("蓝牙已开启")
                getBondedDevicesFromBluetoothAdapter()
            }
        }
    }

    fun makeDiscoverable(fragment: Fragment) {
        if (bluetoothAdapter != null) {
            //请求用户设置蓝牙可被发现
            val intent = Intent()
            intent.action = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200)
            fragment.startActivity(intent)
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
        showProgressLiveData.postValue(true)

    }

    //执行蓝牙设备绑定
    fun createBond(position: Int) {
        currentBluetoothDevice = deviceWithStatusList[position].bluetoothDevice
        val msg : String
        try {
            when (currentBluetoothDevice.bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    msg= "是否与设备" + currentBluetoothDevice.name + "连接？"
                    showDialog(msg) { _, _ ->
                        //使用A2DP协议连接设备
                        connectA2dpDevice()
                    }
                }
//                BluetoothDevice.BOND_BONDING -> {
//                    msgLiveData.postValue("设备正在绑定中")
//                }
                BluetoothDevice.BOND_NONE -> {
                    msg="是否与设备" + currentBluetoothDevice.name + "配对并连接？"
                    showDialog(msg) { _, _ ->
                        // 如果未配对，实施配对绑定
                        BluetoothUtils.makePair(currentBluetoothDevice)
                    }
                }
            }
            showProgressLiveData.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //解除蓝牙设备绑定
    fun releaseBond(position: Int) : Boolean {
        currentBluetoothDevice = deviceWithStatusList[position].bluetoothDevice
        when (currentBluetoothDevice.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                showDialog("是否取消" + currentBluetoothDevice.name.toString() + "配对？" ) {
                        _, _ -> BluetoothUtils.breakPair(currentBluetoothDevice)
                }
            }
        }
        return false
    }

    //获取所有已经绑定的蓝牙设备
    fun getBondedDevicesFromBluetoothAdapter() {
        if (deviceWithStatusList.isNotEmpty()) deviceWithStatusList.clear()
        val deviceSet = bluetoothAdapter?.bondedDevices
        if (deviceSet != null) {
            for (device in deviceSet) {
                val bluetoothDevice = BluetoothDeviceWithStatus(device)
                bluetoothDevice.status = Constants.BluetoothBonded
                deviceWithStatusList.add(bluetoothDevice)
            }
            deviceListLiveData.postValue(deviceWithStatusList)
        }
    }

    //连接A2DP蓝牙设备
    private fun connectA2dpDevice() {
        bluetoothAdapter?.getProfileProxy(
            app.baseContext,
            profileServiceListener,
            BluetoothProfile.A2DP
        )
    }

    private fun showDialog(msg: String?, listener: DialogInterface.OnClickListener?) {
        val alertDialog : AlertDialog = AlertDialog.Builder(context).create()
        alertDialog.setMessage(msg)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "取消"
        ) { _, _ -> alertDialog.dismiss() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确认", listener)
        alertDialog.show()
    }

    private inner class BluetoothReceiver : BroadcastReceiver() {
        private val tag = this.javaClass.simpleName
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    //发现设备
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    //搜索到的不是已经配对的蓝牙设备
                    if (device != null && device.bondState != BluetoothDevice.BOND_BONDED) {
                        val bluetoothDeviceWithStatus = BluetoothDeviceWithStatus(device)
                        //如果设备表中不存在，则添加其中
                        if (deviceWithStatusList.find { deviceInList: BluetoothDeviceWithStatus ->
                                deviceInList.bluetoothDevice.address == device.address
                            } == null) {
                            bluetoothDeviceWithStatus.status = Constants.BluetoothNoBond
                            deviceWithStatusList.add(bluetoothDeviceWithStatus)
                            deviceListLiveData.postValue(deviceWithStatusList)
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                        val position = deviceWithStatusList.indexOfFirst {
                                deviceInList: BluetoothDeviceWithStatus ->
                            deviceInList.bluetoothDevice.address == device.address
                        }
                        if (position >= 0) {
                            deviceWithStatusList[position].status = Constants.BluetoothConnected
                            deviceListLiveData.postValue(deviceWithStatusList)
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                        val position = deviceWithStatusList.indexOfFirst {
                                deviceInList: BluetoothDeviceWithStatus ->
                            deviceInList.bluetoothDevice.address == device.address
                        }
                        if (position >= 0) {
                            deviceWithStatusList[position].status = Constants.BluetoothBonded
                            deviceListLiveData.postValue(deviceWithStatusList)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    //扫描结束
                    showProgressLiveData.postValue(false)
                    Log.i(tag, "搜索完成")
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    //状态发生改变（监听设备连接状态）
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device?.bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            Log.i(tag, "没有设备")
                            getBondedDevicesFromBluetoothAdapter()
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            Log.i(tag, "正在匹配中")
                        }
                        BluetoothDevice.BOND_BONDED -> {
                            Log.i(tag, "匹配成功")
                            showProgressLiveData.postValue(false)
                            connectA2dpDevice()
                        }
                    }
                }
            }
        }
    }
}