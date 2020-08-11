package com.wxson.tws_player

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FileSelectorFragment : Fragment() {

    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel : FileSelectorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_FileSelectorFragment_to_BluetoothFragment)
        }

        viewModel = ViewModelProvider(this).get(FileSelectorViewModel::class.java)
        activity?.let { viewModel.selectFile(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CHOOSEFILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri: Uri? = data?.data
                    if (uri == null) {
                        Log.w(TAG, "onActivityResult uri is null")
                    } else {
                        viewModel.openFile(uri)
                    }
                }
            }
        }
    }
}
