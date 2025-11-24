package com.kartik.flamappai

class NativeBridge {

    init {
        System.loadLibrary("flamapp_ai_native")
    }

    external fun processFrame(matAddrRgba: Long)
}
