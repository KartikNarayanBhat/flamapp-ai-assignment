package com.kartik.flamappai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

class OpenCvCameraActivity : CameraActivity(),
    CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var cameraView: JavaCameraView
    private var rgba: Mat? = null
    private val nativeBridge = NativeBridge()   // <-- add this

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val TAG = "OpenCvCamera"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv_camera)

        cameraView = findViewById(R.id.camera_view)
        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            cameraView.setCameraPermissionGranted()
            startOpenCvAndCamera()
        }
    }

    private fun startOpenCvAndCamera() {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV loaded")
            if (::cameraView.isInitialized) {
                cameraView.enableView()
            }
        } else {
            Log.e(TAG, "OpenCV init failed")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            cameraView.setCameraPermissionGranted()
            startOpenCvAndCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startOpenCvAndCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::cameraView.isInitialized) cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraView.isInitialized) cameraView.disableView()
        rgba?.release()
    }

    override fun getCameraViewList(): MutableList<CameraBridgeViewBase> =
        mutableListOf(cameraView)

    override fun onCameraViewStarted(width: Int, height: Int) {
        rgba = Mat()
    }

    override fun onCameraViewStopped() {
        rgba?.release()
        rgba = null
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        nativeBridge.processFrame(frame.nativeObjAddr)
        return frame
    }
}
