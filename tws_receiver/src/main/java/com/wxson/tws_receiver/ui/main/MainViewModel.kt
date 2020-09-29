package com.wxson.tws_receiver.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.concurrent.schedule


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val runningTag = this.javaClass.simpleName
    private val app : Application
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var deviceItemList = mutableListOf<BluetoothDeviceItem>()
    private var a2dpProfile : BluetoothProfile? = null
    //要操作的设备
//    private lateinit var connectDevice: BluetoothDevice
    private var connectDevice : BluetoothDevice? = null
    var context : Context? = null

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                //A2DP播放状态改变
                BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothA2dp.EXTRA_STATE,
                        BluetoothA2dp.STATE_NOT_PLAYING
                    )
                    Log.i(runningTag, "ACTION_PLAYING_STATE_CHANGED--connect state=$state")
                }

                /**
                 * 搜索蓝牙的广播
                 */
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
//                    val device: BluetoothDevice =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    connectDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (connectDevice != null)
                    // 重复查询
                        if (deviceItemList.find { itemInList: BluetoothDeviceItem -> itemInList.device.address == connectDevice!!.address } == null) {
                            val deviceName = connectDevice!!.name
                            val deviceHardwareAddress = connectDevice!!.address // MAC address
                            val deviceItem =
                                BluetoothDeviceItem(connectDevice!!, Constants.BluetoothUnconnected)
                            deviceItemList.add(deviceItem)
                            deviceListLiveData.postValue(deviceItemList)
                            Log.i(
                                runningTag,
                                "搜索到的蓝牙设备信息：deviceName：$deviceName，deviceHardwareAddress：$deviceHardwareAddress"
                            )
                        }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(runningTag, "搜索完成，当前周围有设备：${deviceItemList.size}台")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(runningTag, "正在扫描..........")
                }
                "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED" -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.i(runningTag, "当前的a2dp的链接状态：${a2dpProfile?.getConnectionState(device)}")
                }
            }
        }
    }

    //region for LiveData
    private var deviceListLiveData = MutableLiveData<MutableList<BluetoothDeviceItem>>()
    fun getDeviceList() : LiveData<MutableList<BluetoothDeviceItem>> {
        return deviceListLiveData
    }
    private var msgLiveData = MutableLiveData<String>()
    fun getMsg(): LiveData<String> {
        return msgLiveData
    }
    //endregion

    init {
        Log.i(runningTag, "init")
        app = application
        deviceItemList.clear()
//        val filter = IntentFilter()
//        filter.apply {
//            addAction("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED")
//            //找到设备的广播
//            addAction(BluetoothDevice.ACTION_FOUND)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            // 搜索完成的广播
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
//            priority = IntentFilter.SYSTEM_HIGH_PRIORITY
//        }
//        context?.registerReceiver(receiver, filter)
    }

    override fun onCleared() {
        Log.i(runningTag, "onCleared")
        context?.unregisterReceiver(receiver)
        bluetoothAdapter?.cancelDiscovery()
        super.onCleared()
    }

    //检测设备是否支持蓝牙
    fun hasBluetoothAdapter(): Boolean {
        return (bluetoothAdapter != null)
    }

    /**
     * 初始化蓝牙
     */
    fun initBlueTooth() {
        Log.i(runningTag, "initBlueTooth")
        if (bluetoothAdapter == null) {
            msgLiveData.postValue("设备不支持蓝牙")
        } else {
            //启动蓝牙
            if (!bluetoothAdapter.isEnabled) {
                //如果没有开启蓝牙 的话 就强行启动....
                bluetoothAdapter.enable()
            }
            Log.i(runningTag, "蓝牙已启动")

            val filter = IntentFilter()
            filter.apply {
                addAction("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED")
                //找到设备的广播
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                // 搜索完成的广播
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                priority = IntentFilter.SYSTEM_HIGH_PRIORITY
            }
            context?.registerReceiver(receiver, filter)

            searchDevices()
        }
    }

    /**
     * 搜索设备
     */
    private fun searchDevices() {
        if (bluetoothAdapter?.isDiscovering!!) {
            bluetoothAdapter.cancelDiscovery()
        } else {
            //开始搜索
            val findThreadSuccess = bluetoothAdapter.startDiscovery()
            Log.i(runningTag, "搜索设备的进程是否已成功启动:$findThreadSuccess")
        }
    }

    fun createBond(position: Int){
        Log.i(runningTag, "createBond")
        //连接前先关闭搜索
        bluetoothAdapter?.cancelDiscovery()
        //关闭bluetooth source服务
        stopA2dpService()
        //开启bluetooth sink服务
        startA2dpSinkService()

        connectDevice = deviceItemList[position].device
        //Perform a service discovery on the remote device to get the UUIDs supported.
        connectDevice!!.fetchUuidsWithSdp()
        connectDevice!!.createBond()
        Log.i(runningTag, "当前点击的设备名称：${connectDevice!!.name}，MAC:${connectDevice!!.address}")

        Timer().schedule(3000){
            if (bluetoothAdapter!!.getProfileProxy(app, profileServiceListener, 11)) {
                //A2DP_SINK
                Log.i(runningTag, "getProfileProxy ok")
            }
        }
    }

    //解除蓝牙设备绑定
    fun releaseBond(position: Int) {
        Log.i(runningTag, "releaseBond")
        connectDevice = deviceItemList[position].device
        when (connectDevice!!.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                breakPair(connectDevice!!)
            }
        }
    }

    /**
     * 通过监听回调来获取a2dp对象
     */
    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = null
                Log.i(runningTag, "ok==========连接失败")
            }
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == 11) {                       //A2DP_SINK
                a2dpProfile = proxy  //转换
                connectA2dp(connectDevice!!)
            }
        }
    }

    /**
     * A2DP连接
     */
    @SuppressLint("PrivateApi")
    private fun connectA2dp(device: BluetoothDevice) {
        try {
            val a2dpSinkClass =
                ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothA2dpSink")
            val connectMethod = a2dpSinkClass.getMethod("connect", BluetoothDevice::class.java)

            connectMethod.invoke(a2dpProfile, device)
            Log.i(runningTag, "connectA2dp: a2dpSinkClass:${a2dpSinkClass.name}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    /**
//     * 查询已配对设备
//     * 注意：执行设备发现将消耗蓝牙适配器的大量资源。在找到要连接的设备后，请务必使用 cancelDiscovery() 停止发现，然后再尝试连接。
//     * 此外，您不应在连接到设备的情况下执行设备发现，因为发现过程会大幅减少可供任何现有连接使用的带宽
//     */
//    private fun getBoundDevice() {
//        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
//        pairedDevices?.forEach { device ->
//            val deviceName = device.name
//            val deviceHardwareAddress = device.address // MAC address
//            Log.i(runningTag, "已配对设备的name:$deviceName，MAC address :$deviceHardwareAddress")
//        }
//    }

//    /**
//     * 设置优先级
//     */
//    fun setPriority(device: BluetoothDevice?, priority: Int) {
//        if (a2dpProfile == null) return
//        try { //通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
//            val connectMethod: Method = BluetoothA2dp::class.java.getMethod(
//                "setPriority",
//                BluetoothDevice::class.java, Int::class.javaPrimitiveType
//            )
//            connectMethod.invoke(a2dpProfile, device, priority)
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//    }

    private fun breakPair(bluetoothDevice: BluetoothDevice) {
        try {
            val removeBond = BluetoothDevice::class.java.getMethod("removeBond")
            removeBond.invoke(bluetoothDevice)
        } catch (e: Exception) {
            Log.e(runningTag, e.message ?: "")
        }
    }

    private fun stopA2dpService() {
        val intent = Intent()
        intent.action = "com.android.bluetooth/.a2dp.A2dpService"
        intent.setPackage("com.android.bluetooth")
        intent.putExtra("action", "com.android.bluetooth.btservice.action.STATE_CHANGED")
        intent.putExtra(BluetoothAdapter.EXTRA_STATE, 10)
        app.startService(intent)
        Log.i(runningTag, "stopA2dpService")
    }

    private fun startA2dpSinkService () {
        val intent = Intent()
        intent.action = "com.android.bluetooth/.a2dpsink.A2dpSinkService"
        intent.setPackage("com.android.bluetooth")
        intent.putExtra("action", "com.android.bluetooth.btservice.action.STATE_CHANGED")
        intent.putExtra(BluetoothAdapter.EXTRA_STATE, 12)
        app.startService(intent)
        Log.i(runningTag, "startA2dpSinkService")
    }
}
