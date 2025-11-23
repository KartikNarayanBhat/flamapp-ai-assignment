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
