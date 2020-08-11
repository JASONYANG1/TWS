package com.wxson.tws_receiver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import pub.devrel.easypermissions.EasyPermissions

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: FirstViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        viewModel = ViewModelProvider(this).get(FirstViewModel::class.java)
        requestLocationPermission()
    }

    //申请蓝牙所需位置权限
    private fun requestLocationPermission() {
        Log.i(TAG, "requestLocationPermission")
        val perms = Manifest.permission.ACCESS_COARSE_LOCATION
        if (EasyPermissions.hasPermissions(requireContext(), perms)) {
            Log.i(TAG, "已获取ACCESS_COARSE_LOCATION权限")
            // Already have permission, do the thing
            // 打开蓝牙
            activity?.let { viewModel.openBluetoothAdapter(it) }
        } else {
            Log.i(TAG, "申请ACCESS_COARSE_LOCATION权限")
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.position_rationale),
                1,
                perms
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.i(TAG, "onPermissionsDenied")
        Log.i(TAG, "获取权限失败，退出当前页面$perms")
        showMsg("获取权限失败")
        activity?.finish()  //退出当前页面
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.i(TAG, "onPermissionsGranted")
        Log.i(TAG, "获取权限成功$perms")
        showMsg("获取权限成功")
        // 打开蓝牙
        activity?.let { viewModel.openBluetoothAdapter(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            resources.getInteger(R.integer.BluetoothRequestCode) ->{
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "开启蓝牙成功")
                    showMsg("开启蓝牙成功")
                    }
                else {
                    Log.i(TAG, "开启蓝牙失败")
                    showMsg("开启蓝牙失败")
                }
            }
        }
    }

    private fun showMsg(msg: String){
        Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
    }
}
