package com.kartik.flamappai

object NativeBridge {
    init {
        System.loadLibrary("flamapp_ai_native")
    }

    external fun initNative()

    external fun processFrame(
        width: Int,
        height: Int,
        yuvNv21: ByteArray
    ): ByteArray
}
