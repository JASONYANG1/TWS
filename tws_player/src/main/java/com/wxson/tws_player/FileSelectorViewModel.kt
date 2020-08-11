package com.wxson.tws_player

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import java.io.FileInputStream
import java.io.InputStream

class FileSelectorViewModel(application: Application)  : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName
    private val app = application

    init {
        Log.i(TAG, "init")
    }

    override fun onCleared() {
        Log.i(TAG, "onCleared")
        super.onCleared()
    }

    fun selectFile(currActivity: Activity) {
        Log.i(TAG, "selectFile")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/wav"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        currActivity.startActivityForResult(intent, Constants.REQUEST_CHOOSEFILE)
    }

    fun openFile(uri : Uri) {
        val filePath = getFilePath(uri)
        if (filePath != null) {
            val fileInputStream : FileInputStream = FileInputStream(filePath)



        }
    }

    private fun getFilePath(uri : Uri) : String? {
        Log.i(TAG, "getFilePath")
        var filePath : String? = null
        if ("file".equals(uri.scheme, true)) {
            filePath = uri.path
            if (filePath == null) {
                Log.w(TAG, "selectFile URI is invalid")
            }
        }
        return filePath
    }
}