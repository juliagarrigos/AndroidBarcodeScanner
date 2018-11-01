package com.softjg.barcodescanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Single

class BytesToBitmapUseCase {
    fun byteArrayToBitmap(bytes: ByteArray): Single<Bitmap> = Single.fromCallable {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}