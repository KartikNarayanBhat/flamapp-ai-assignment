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
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class OpenCvCameraActivity : CameraActivity(),
    CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var cameraView: JavaCameraView
    private var rgba: Mat? = null

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

        // Ask for camera permission if needed
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

    // Handle runtime permission result
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
            startOpenCvAndCamera()   // start immediately after user taps Allow
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

    // CvCameraViewListener2 methods
    override fun onCameraViewStarted(width: Int, height: Int) {
        rgba = Mat()
    }

    override fun onCameraViewStopped() {
        rgba?.release()
        rgba = null
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val gray = Mat()
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 1.5)
        val edges = Mat()
        Imgproc.Canny(gray, edges, 150.0, 300.0)
        Imgproc.cvtColor(edges, frame, Imgproc.COLOR_GRAY2RGBA)
        gray.release()
        edges.release()
        return frame
    }
}
