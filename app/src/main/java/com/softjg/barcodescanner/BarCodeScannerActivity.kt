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
import android.widget.TextView
import com.camerakit.CameraKitView
import com.google.firebase.FirebaseApp
import java.lang.IllegalStateException


@SuppressLint("MissingPermission")
class BarCodeScannerActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_PERMISSION_REQUEST_REQUEST_CODE = 1
    }

    private val cameraKitView: CameraKitView by lazy {
        findViewById<CameraKitView>(R.id.cameraKitView_barcodeScannerActivity)
    }
    private val cameraContainer by lazy {
        findViewById<View>(R.id.frameLayout_barcodeScannerActivity_cameraContainer)
    }
    private val barcodeTextView by lazy {
        findViewById<TextView>(R.id.textView_barcodeScannerActivity_barcode)
    }
    private val progressIndicator by lazy {
        findViewById<View>(R.id.progressBar_barcodeScannerActivity_loading)
    }

    private val viewModelFactory = BarcodeScannerViewModelFactory()
    private val viewModel: BarCodeScannerViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BarCodeScannerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_scanner)

        FirebaseApp.initializeApp(this)

        setUpView()
        observeStateChanges()
        initViewModelWithPermissionStatus()
    }

    private fun setUpView() {
        findViewById<Button>(R.id.button_barcodeScannerActivity_grantPermission)
                .setOnClickListener { viewModel.onGrantPermissionButtonClicked() }

        findViewById<Button>(R.id.button_barcodeScannerActivity_capture)
                .setOnClickListener { viewModel.onTakePictureButtonClicked() }
    }

    private fun observeStateChanges() {
        viewModel.viewState().observe(this, Observer { state ->
            if (state == null) throw IllegalStateException("State can't be null")
            renderState(state)
        })
    }

    private fun initViewModelWithPermissionStatus() {
        viewModel.onInit(isCameraPermissionGranted())
    }

    private fun renderState(state: State) {
        val requestPermissionContainer = findViewById<View>(R.id
                .lineraLayout_barcodeScannerActivity_requestPermissionContainer)

        //TODO We should make this exhaustive (when is not forcing to consider all cases)
        when (state) {
            is State.RequestingCameraPermission -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.GONE
                requestCameraPermission()
            }
            is State.PermissionNotGranted -> {
                requestPermissionContainer.visibility = View.VISIBLE
                cameraContainer.visibility = View.GONE
            }
            is State.PermissionGranted -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                barcodeTextView.visibility = View.GONE
                progressIndicator.visibility = View.GONE
                cameraKitView.onStart()
            }
            is State.Camera -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                barcodeTextView.visibility = View.GONE
                progressIndicator.visibility = View.GONE
            }
            is State.TakingPicture -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                barcodeTextView.visibility = View.GONE
                progressIndicator.visibility = View.VISIBLE
                cameraKitView.captureImage { _, bytes ->
                    viewModel.onImageCaptured(bytes)
                }
            }
            is State.ProcessingPicture -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                progressIndicator.visibility = View.VISIBLE
                barcodeTextView.visibility = View.GONE
            }

            is State.BarCodeNotFound -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                progressIndicator.visibility = View.GONE
                barcodeTextView.visibility = View.VISIBLE
                barcodeTextView.text = getString(R.string.barcode_not_recognized_message)
            }
            is State.BarCodeFound -> {
                requestPermissionContainer.visibility = View.GONE
                cameraContainer.visibility = View.VISIBLE
                barcodeTextView.visibility = View.VISIBLE
                progressIndicator.visibility = View.GONE
                barcodeTextView.text = state.barCode
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


    override fun onStart() {
        super.onStart()
        cameraKitView.onStart()
    }

    override fun onResume() {
        super.onResume()
        cameraKitView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraKitView.onPause()
    }

    override fun onStop() {
        super.onStop()
        cameraKitView.onStop()
    }
}
