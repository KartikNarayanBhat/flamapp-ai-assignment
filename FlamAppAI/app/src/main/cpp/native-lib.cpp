#include <jni.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "FlamAppAI-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_kartik_flamappai_NativeBridge_initNative(JNIEnv* env, jobject thiz) {
    LOGI("Native init called");

    Mat testImg(100, 100, CV_8UC1, Scalar(0));
    line(testImg, Point(10, 10), Point(90, 90), Scalar(255), 2);
    Mat edges;
    Canny(testImg, edges, 80, 150);
    LOGI("OpenCV OK: edges size = %d x %d", edges.cols, edges.rows);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_kartik_flamappai_NativeBridge_processFrame(
        JNIEnv* env,
        jobject /*thiz*/,
        jint width,
        jint height,
        jbyteArray yuvNv21) {

    jbyte* yuvPtr = env->GetByteArrayElements(yuvNv21, nullptr);
    const int yuvLen = env->GetArrayLength(yuvNv21);

    // NV21: Y plane (h*w) + interleaved VU (h/2*w)
    Mat nv21(height + height / 2, width, CV_8UC1,
             reinterpret_cast<unsigned char*>(yuvPtr));

    Mat rgba;
    // Convert NV21 (YUV420sp) to RGBA
    cvtColor(nv21, rgba, COLOR_YUV2RGBA_NV21);  // OpenCV color conversion code[web:151][web:153]

    // Run Canny on grayscale version
    Mat gray;
    cvtColor(rgba, gray, COLOR_RGBA2GRAY);
    Mat edges;
    Canny(gray, edges, 80, 150);                // Canny edge detection[web:152]

    // Put edges back into RGBA (white edges on black)
    Mat edgesRgba;
    cvtColor(edges, edgesRgba, COLOR_GRAY2RGBA);

    const int outSize = width * height * 4;
    jbyteArray out = env->NewByteArray(outSize);
    env->SetByteArrayRegion(out, 0, outSize,
                            reinterpret_cast<jbyte*>(edgesRgba.data));

    env->ReleaseByteArrayElements(yuvNv21, yuvPtr, JNI_ABORT);

    LOGI("processFrame done: inLen=%d, outLen=%d", yuvLen, outSize);
    return out;
}
