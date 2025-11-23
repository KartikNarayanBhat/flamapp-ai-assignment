#include <jni.h>
#include <android/log.h>

#define LOG_TAG "FlamAppAI-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_kartik_flamappai_NativeBridge_initNative(JNIEnv* env, jobject thiz) {
LOGI("Native init called");
}
