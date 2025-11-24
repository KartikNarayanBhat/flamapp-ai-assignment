package com.kartik.flamappai

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf

object FrameState {
    val lastFrameBitmap = mutableStateOf<Bitmap?>(null)
}
