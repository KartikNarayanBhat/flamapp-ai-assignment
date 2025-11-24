#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_kartik_flamappai_NativeBridge_processFrame(
        JNIEnv* env,
        jobject /* thisObj */,   // instance method -> jobject
        jlong matAddrRgba) {

    Mat& rgba = *(Mat*) matAddrRgba;

    Mat gray;
    cvtColor(rgba, gray, COLOR_RGBA2GRAY);

// Stronger blur to reduce noise
    cvtColor(rgba, gray, COLOR_RGBA2GRAY);

// Light blur: keep details, reduce noise a bit
    GaussianBlur(gray, gray, Size(3, 3), 0.8);

// Higher thresholds for thinner, cleaner text strokes
    Canny(gray, gray, 80, 160);

    cvtColor(gray, rgba, COLOR_GRAY2RGBA);



}

