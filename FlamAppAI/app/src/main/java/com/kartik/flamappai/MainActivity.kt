package com.kartik.flamappai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.kartik.flamappai.ui.theme.FlamAppAITheme
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "FlamCamera"
        const val REQUEST_CAMERA_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ok = OpenCVLoader.initDebug()
        Log.i(TAG, "OpenCV init = $ok")

        enableEdgeToEdge()
        setContent {
            FlamAppAITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                recreate()
            }
        }
    }
}

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hasCameraPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermissionState.value && context is ComponentActivity) {
            context.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                MainActivity.REQUEST_CAMERA_PERMISSION
            )
        }
    }

    if (!hasCameraPermissionState.value) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { TextureView(it) }
        )
        return
    }

    val frameBitmap = FrameState.lastFrameBitmap.value

    if (frameBitmap == null) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                val textureView = TextureView(ctx)
                textureView.surfaceTextureListener =
                    object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            startCameraPreview(ctx, textureView)
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) = Unit

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
                textureView
            }
        )
    } else {
        Image(
            modifier = modifier,
            bitmap = frameBitmap.asImageBitmap(),
            contentDescription = "Canny edges",
            contentScale = ContentScale.Fit
        )
    }
}

private fun startCameraPreview(context: Context, textureView: TextureView) {
    val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    val cameraId = cameraManager.cameraIdList.first { id ->
        val chars = cameraManager.getCameraCharacteristics(id)
        chars.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_BACK
    }

    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
    val streamConfig =
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: return

    val previewSize = streamConfig
        .getOutputSizes(SurfaceTexture::class.java)
        .first { it.width <= 1280 && it.height <= 720 }

    val imageSize = streamConfig
        .getOutputSizes(ImageFormat.YUV_420_888)
        .first { it.width <= 1280 && it.height <= 720 }

    val imageReader = ImageReader.newInstance(
        imageSize.width,
        imageSize.height,
        ImageFormat.YUV_420_888,
        2
    )

    imageReader.setOnImageAvailableListener(
        { reader ->
            val image: Image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            handleYuvImage(image)
            image.close()
        },
        null
    )

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    cameraManager.openCamera(
        cameraId,
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                val texture = textureView.surfaceTexture ?: return
                texture.setDefaultBufferSize(previewSize.width, previewSize.height)
                val previewSurface = Surface(texture)
                val imageSurface = imageReader.surface

                val requestBuilder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                        addTarget(previewSurface)
                        addTarget(imageSurface)
                        set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON
                        )
                    }

                camera.createCaptureSession(
                    listOf(previewSurface, imageSurface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(
                                requestBuilder.build(),
                                null,
                                null
                            )
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) = Unit
                    },
                    null
                )
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        },
        null
    )
}

private fun handleYuvImage(image: Image) {
    if (image.format != ImageFormat.YUV_420_888) return

    val width = image.width
    val height = image.height

    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yBuffer: ByteBuffer = yPlane.buffer
    val uBuffer: ByteBuffer = uPlane.buffer
    val vBuffer: ByteBuffer = vPlane.buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    Log.i(MainActivity.TAG, "NV21 size=${nv21.size}, w=$width h=$height")

    // NV21 -> RGBA using OpenCV
    val nv21Mat = Mat(height + height / 2, width, CvType.CV_8UC1)
    nv21Mat.put(0, 0, nv21)

    val rgba = Mat()
    Imgproc.cvtColor(nv21Mat, rgba, Imgproc.COLOR_YUV2RGBA_NV21)

    // Canny on grayscale
    val gray = Mat()
    Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
    val edges = Mat()
    Imgproc.Canny(gray, edges, 80.0, 150.0)

    val edgesColor = Mat()
    Imgproc.cvtColor(edges, edgesColor, Imgproc.COLOR_GRAY2RGBA)

    val outBitmap =
        android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(edgesColor, outBitmap)

    FrameState.lastFrameBitmap.value = outBitmap
}
