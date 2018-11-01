package com.softjg.barcodescanner

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import io.reactivex.Single

class GetBarcodeFromBitmapUseCase(
        private val firebaseVision: FirebaseVision,
        private val barcodeDetectorOptionsBuilder: FirebaseVisionBarcodeDetectorOptions.Builder
) {

    fun getBarcodeFromBitmap(bitmap: Bitmap): Single<String> = Single.create { source ->
        val detector = getFirebaseVisionBarcodeDetector(getFirebaseVisionBarcodeDetectorOptions())
        detector.detectInImage(FirebaseVisionImage.fromBitmap(bitmap))
                .addOnSuccessListener { barCodes ->
                    barCodes?.firstOrNull()?.displayValue?.let {
                        source.onSuccess(it)
                    } ?: source.onError(Throwable("Barcodes received are empty"))
                }
                .addOnFailureListener {
                    source.onError(Throwable("No barcode found in picture"))
                }
    }

    private fun getFirebaseVisionBarcodeDetector(options: FirebaseVisionBarcodeDetectorOptions) =
            firebaseVision.getVisionBarcodeDetector(options)

    private fun getFirebaseVisionBarcodeDetectorOptions(): FirebaseVisionBarcodeDetectorOptions {
        return barcodeDetectorOptionsBuilder.setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_UPC_A,
                FirebaseVisionBarcode.FORMAT_UPC_E,
                FirebaseVisionBarcode.FORMAT_EAN_8,
                FirebaseVisionBarcode.FORMAT_EAN_13)
                .build()
    }
}