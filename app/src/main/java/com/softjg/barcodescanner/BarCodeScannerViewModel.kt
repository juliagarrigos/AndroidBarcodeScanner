package com.softjg.barcodescanner

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


sealed class State {
    object PermissionNotGranted : State()
    object RequestingCameraPermission : State()
    object PermissionGranted : State()
    object Camera : State()
    object TakingPicture : State()
    object ProcessingPicture : State()
    data class BarCodeFound(val barCode: String) : State()
    object BarCodeNotFound : State()
}

class BarCodeScannerViewModel(private val barcodeFromBitmapUseCase: GetBarcodeFromBitmapUseCase,
                              private val bytesToBitmapUseCase: BytesToBitmapUseCase
) : ViewModel() {

    private val viewState: MutableLiveData<State> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    fun viewState(): LiveData<State> = viewState

    fun onInit(permissionGranted: Boolean) {
        postState(if (permissionGranted) State.Camera else State.RequestingCameraPermission)
    }

    fun onCameraPermissionNotGranted() {
        postState(State.PermissionNotGranted)
    }

    fun onCameraPermissionGranted() {
        postState(State.PermissionGranted)
    }

    fun onGrantPermissionButtonClicked() {
        postState(State.RequestingCameraPermission)
    }

    fun onTakePictureButtonClicked() {
        postState(State.TakingPicture)
    }

    fun onImageCaptured(bytes: ByteArray) {
        postState(State.ProcessingPicture)

        compositeDisposable.add(bytesToBitmapUseCase.byteArrayToBitmap(bytes)
                .flatMap(barcodeFromBitmapUseCase::getBarcodeFromBitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("Barcode", "Barcode found: $it")
                    postState(State.BarCodeFound(it))
                }, {
                    Log.i("Barcode", "Error: $it")
                    postState(State.BarCodeNotFound)
                }))
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun postState(state: State) {
        viewState.value = state
    }
}

class BarcodeScannerViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BarCodeScannerViewModel(GetBarcodeFromBitmapUseCase(FirebaseVision.getInstance(),
                FirebaseVisionBarcodeDetectorOptions.Builder()),
                BytesToBitmapUseCase()) as T
    }
}