package com.kartik.flamappai

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object OpenCvProcessor {

    fun canny(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)

        // Smooth to reduce noise before edge detection
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 1.5)

        val edges = Mat()
        // Higher thresholds -> cleaner, sharper edges
        Imgproc.Canny(gray, edges, 150.0, 300.0)

        val edgesColor = Mat()
        Imgproc.cvtColor(edges, edgesColor, Imgproc.COLOR_GRAY2RGBA)

        val out = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(edgesColor, out)

        src.release()
        gray.release()
        edges.release()
        edgesColor.release()

        return out
    }
}
