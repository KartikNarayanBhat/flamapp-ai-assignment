cmake_minimum_required(VERSION 3.18.1)

project("flamapp_ai_native")

add_library(
        flamapp_ai_native
        SHARED
        native-lib.cpp)

set(OpenCV_DIR "C:/Users/karti/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

find_library(
        log-lib
        log)

target_link_libraries(
        flamapp_ai_native
        ${OpenCV_LIBS}
        ${log-lib})
