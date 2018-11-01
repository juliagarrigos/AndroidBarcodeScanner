package com.softjg.barcodescanner

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

sealed class State {
    object Camera : State()
    object RequestCameraPermission : State()
    object PermissionNotGranted : State()
}

class BarCodeScannerViewModel : ViewModel() {

    private val viewState: MutableLiveData<State> = MutableLiveData()

    fun viewState(): LiveData<State> = viewState

    fun onInit(permissionGranted: Boolean) {
        viewState.value = if (permissionGranted) State.Camera else State.RequestCameraPermission
    }

    fun onCameraPermissionNotGranted() {
        viewState.value = State.PermissionNotGranted
    }

    fun onCameraPermissionGranted() {
        viewState.value = State.Camera
    }

    fun onGrantPermissionButtonClicked() {
        viewState.value = State.RequestCameraPermission
    }

}