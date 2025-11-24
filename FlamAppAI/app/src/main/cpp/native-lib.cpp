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

// Slight blur: keep detail but reduce noise
    GaussianBlur(gray, gray, Size(3, 3), 1.0);

// Tighter thresholds for thinner, cleaner edges
    Canny(gray, gray, 70, 130);

    cvtColor(gray, rgba, COLOR_GRAY2RGBA);


}

