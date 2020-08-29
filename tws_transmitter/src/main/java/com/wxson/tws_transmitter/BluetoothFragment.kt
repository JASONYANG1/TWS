package com.wxson.tws_transmitter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.EasyPermissions


class BluetoothFragment : Fragment(), EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private val runningTag = this.javaClass.simpleName
    private var btnOpen : Button? = null
    private var btnClose : Button? = null
    private var btnSearch : Button? = null
    private var btnDiscoverable : Button? = null
    private var btnPlay : Button? = null
    private var btnStop : Button? = null
    private var listView : ListView? = null
    private val deviceListAdapter = DeviceListAdapter()
    private lateinit var snackbar : Snackbar

//    companion object {
//        fun newInstance() = BluetoothFragment()
//    }

    private lateinit var viewModel: BluetoothViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.i(runningTag, "onActivityCreated")
        //获取控件
        btnOpen = activity?.findViewById(R.id.btn_open)
        btnOpen?.setOnClickListener(this)
        btnClose = activity?.findViewById(R.id.btn_close)
        btnClose?.setOnClickListener(this)
        btnSearch = activity?.findViewById(R.id.btn_search)
        btnSearch?.setOnClickListener(this)
        btnDiscoverable = activity?.findViewById(R.id.btn_discoverable)
        btnDiscoverable?.setOnClickListener(this)
        btnPlay = activity?.findViewById(R.id.btn_play)
        btnPlay?.setOnClickListener(this)
        btnStop = activity?.findViewById(R.id.btn_stop)
        btnStop?.setOnClickListener(this)
        listView = activity?.findViewById(R.id.list_view)
        listView?.adapter = deviceListAdapter
        //获取ViewModel
        viewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        if (!viewModel.hasBluetoothAdapter()) {
            showMsg("不支持蓝牙 系统退出")
            activity?.finish()
        }
        //listView项目监听器
        listView?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            viewModel.createBond(position)
        }

        listView?.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
            viewModel.releaseBond(position)
        }

        //定义ViewModel数据变化观察者
        val bluetoothDeviceListObserver: Observer<MutableList<BluetoothDeviceWithStatus>> =
            Observer { deviceList -> deviceListAdapter.refresh(deviceList) }
        viewModel.getDeviceList().observe(viewLifecycleOwner, bluetoothDeviceListObserver)
        val msgObserver: Observer<String> = Observer { localMsg -> showMsg(localMsg.toString()) }
        viewModel.getMsg().observe(viewLifecycleOwner, msgObserver)
        val showProgressObserver : Observer<Boolean> = Observer { isShowProgress -> showProgress(isShowProgress) }
        viewModel.getShowProgress().observe(viewLifecycleOwner, showProgressObserver)

        //申请权限
        requestLocationPermission()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_open -> {
                activity?.let {viewModel.openBluetooth(it)}
            }
            R.id.btn_close -> {
                viewModel.closeBluetooth()
            }
            R.id.btn_search -> {
                viewModel.searchBluetooth()
            }
            R.id.btn_discoverable -> {
                activity?.let {viewModel.makeDiscoverable(it)}
            }
            R.id.btn_play -> {
                AudioUtils.INSTANCE.playMedia(activity)
            }
            R.id.btn_stop -> {
                AudioUtils.INSTANCE.stopPlay()
            }
        }
    }

    //申请蓝牙所需位置权限
    private fun requestLocationPermission() {
        Log.i(runningTag, "requestLocationPermission")
        val perms = Manifest.permission.ACCESS_COARSE_LOCATION
        if (EasyPermissions.hasPermissions(requireContext(), perms)) {
            Log.i(runningTag, "已获取ACCESS_COARSE_LOCATION权限")
            // Already have permission, do the thing
        } else {
            Log.i(runningTag, "申请ACCESS_COARSE_LOCATION权限")
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, getString(R.string.position_rationale), 1, perms
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i(runningTag, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.i(runningTag, "onPermissionsDenied")
        Log.i(runningTag, "获取权限失败，退出当前页面$perms")
        showMsg("获取权限失败")
        activity?.finish()  //退出当前页面
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.i(runningTag, "onPermissionsGranted")
        Log.i(runningTag, "获取权限成功$perms")
        showMsg("获取权限成功")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            Constants.BluetoothRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(runningTag, "开启蓝牙成功")
                    showMsg("开启蓝牙成功")
                    viewModel.getBondedDevicesFromBluetoothAdapter()
                }
                else {
                    Log.i(runningTag, "开启蓝牙失败")
                    showMsg("开启蓝牙失败")
                }
            }
        }
    }

    private fun showMsg(msg: String){
        Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showProgress(isShow : Boolean) {
        if (isShow) {
            snackbar = Snackbar.make(this.requireView(), "正在搜索...", Snackbar.LENGTH_INDEFINITE)
            snackbar.show()
        } else {
            snackbar.dismiss()
        }
    }
}