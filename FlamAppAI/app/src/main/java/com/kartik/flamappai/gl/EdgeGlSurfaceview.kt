package com.kartik.flamappai.gl



import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class EdgeGlSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: EdgeRenderer

    init {
        // Use OpenGL ES 2.0
        setEGLContextClientVersion(2)

        renderer = EdgeRenderer()
        setRenderer(renderer)

        // Render continuously for now
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
