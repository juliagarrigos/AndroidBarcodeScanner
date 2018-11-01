package com.softjg.barcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import java.lang.IllegalStateException

class BarCodeScannerActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_PERMISSION_REQUEST_REQUEST_CODE = 1
    }

    private val viewModel: BarCodeScannerViewModel by lazy {
        ViewModelProviders.of(this).get(BarCodeScannerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_scanner)

        setUpView()
        observeStateChanges()
        initViewModelWithPermissionStatus()
    }

    private fun setUpView() {
        findViewById<Button>(R.id.button_barcodeScannerActivity_grantPermission)
                .setOnClickListener { viewModel.onGrantPermissionButtonClicked() }
    }

    private fun observeStateChanges() {
        viewModel.viewState().observe(this, Observer { state ->
            if (state == null) throw IllegalStateException("State null.")
            renderState(state)
        })
    }

    private fun initViewModelWithPermissionStatus() {
        viewModel.onInit(isCameraPermissionGranted())
    }

    @SuppressLint("MissingPermission")
    private fun renderState(state: State) {
        val requestPermissionContainer = findViewById<View>(R.id
                .lineraLayout_barcodeScannerActivity_requestPermissionContainer)
        when (state) {
            State.Camera -> {
                requestPermissionContainer.visibility = View.GONE
                //TODO start barcode detection
            }
            State.RequestCameraPermission -> {
                requestPermissionContainer.visibility = View.GONE
                requestCameraPermission()
            }
            State.PermissionNotGranted -> {
                requestPermissionContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.onCameraPermissionGranted()
            } else {
                viewModel.onCameraPermissionNotGranted()
            }
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_REQUEST_CODE)
    }

    private fun isCameraPermissionGranted() = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}
