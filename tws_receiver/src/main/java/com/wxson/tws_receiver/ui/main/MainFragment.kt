package com.wxson.tws_receiver.ui.main

import android.Manifest
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wxson.tws_receiver.R
import pub.devrel.easypermissions.EasyPermissions

class MainFragment() : Fragment(), EasyPermissions.PermissionCallbacks {
    private val runningTag = this.javaClass.simpleName
    private var listView : ListView? = null
    private val deviceListAdapter = DeviceListAdapter()

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.context = this.context
        if (!viewModel.hasBluetoothAdapter()) {
            showMsg("不支持蓝牙 系统退出")
            activity?.finish()
        }

        //申请权限
        requestLocationPermission()

        listView = activity?.findViewById(R.id.list_view)
        listView?.adapter = deviceListAdapter
        //listView项目监听器
        listView?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            viewModel.createBond(position)
        }

        listView?.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
            viewModel.releaseBond(position)
            true
        }

        //定义ViewModel数据变化观察者
        val msgObserver: Observer<String> = Observer { localMsg -> showMsg(localMsg.toString()) }
        viewModel.getMsg().observe(viewLifecycleOwner, msgObserver)
        val bluetoothDeviceListObserver: Observer<MutableList<BluetoothDeviceItem>> =
            Observer { deviceList -> deviceListAdapter.refresh(deviceList) }
        viewModel.getDeviceList().observe(viewLifecycleOwner, bluetoothDeviceListObserver)

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.i(runningTag, "onPermissionsGranted")
        Log.i(runningTag, "获取权限成功$perms")
        showMsg("获取权限成功")
        viewModel.initBlueTooth()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.i(runningTag, "onPermissionsGranted")
        Log.i(runningTag, "获取权限失败，退出当前页面$perms")
        showMsg("获取权限失败，退出当前页面")
        activity?.finish()  //退出当前页面
    }

    //申请蓝牙所需位置权限
    private fun requestLocationPermission() {
        Log.i(runningTag, "requestLocationPermission")
        val perms = Manifest.permission.ACCESS_COARSE_LOCATION
        if (EasyPermissions.hasPermissions(requireContext(), perms)) {
            Log.i(runningTag, "已获取ACCESS_COARSE_LOCATION权限")
            // Already have permission, do the thing
            viewModel.initBlueTooth()
        } else {
            Log.i(runningTag, "申请ACCESS_COARSE_LOCATION权限")
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, getString(R.string.position_rationale), 1, perms
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(runningTag, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    private fun showMsg(msg: String){
        Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
    }

}
